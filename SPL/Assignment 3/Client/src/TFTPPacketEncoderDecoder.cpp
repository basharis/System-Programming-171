#include "TFTPPacketEncoderDecoder.h"


TFTPPacketEncoderDecoder::TFTPPacketEncoderDecoder()
{
	bytes = std::vector<char>(0);
}

TFTPPacket* TFTPPacketEncoderDecoder::decodeNextByte(char nextByte)
{
	pushByte(nextByte);
	if (len == 2)
	{
		shortOpcode = decodeBytesToShort(bytes, 0);
		switch (shortOpcode)
		{
		case TFTPPacket::TFTP_OPCODE_DIRQ: return popPacket();
		case TFTPPacket::TFTP_OPCODE_DISC: return popPacket();
		case TFTPPacket::TFTP_OPCODE_ACK:break;
		case TFTPPacket::TFTP_OPCODE_BCAST:break;
		case TFTPPacket::TFTP_OPCODE_DATA:break;
		case TFTPPacket::TFTP_OPCODE_DELRQ:break;
		case TFTPPacket::TFTP_OPCODE_ERROR:break;
		case TFTPPacket::TFTP_OPCODE_LOGRQ:break;
		case TFTPPacket::TFTP_OPCODE_RRQ:break;
		case TFTPPacket::TFTP_OPCODE_WRQ:break;
		default:return popPacket();
		}
	}
	if (len > 2)
	{
		switch (shortOpcode)
		{
		case TFTPPacket::TFTP_OPCODE_LOGRQ:if (nextByte == '\0') { return popPacket(); } break;
		case TFTPPacket::TFTP_OPCODE_DELRQ:if (nextByte == '\0') { return popPacket(); } break;
		case TFTPPacket::TFTP_OPCODE_RRQ:if (nextByte == '\0') { return popPacket(); } break;
		case TFTPPacket::TFTP_OPCODE_WRQ:if (nextByte == '\0') { return popPacket(); } break;
		default:break;
		}
	}
	if (len == 3)
	{
		if (shortOpcode == TFTPPacket::TFTP_OPCODE_BCAST)
		{
			fileActionBCAST = bytes[len - 1];
		}
	}
	if (len > 3)
	{
		if (shortOpcode == TFTPPacket::TFTP_OPCODE_BCAST) {
			if (nextByte == '\0') { return popPacket(); }
		}

	}
	if (len == 4)
	{
		switch (shortOpcode)
		{
		case TFTPPacket::TFTP_OPCODE_ACK:currentBlockNumber = decodeBytesToShort(bytes, 2); return popPacket();
		case TFTPPacket::TFTP_OPCODE_DATA:currentPacketSize = decodeBytesToShort(bytes, 2); break;
		case TFTPPacket::TFTP_OPCODE_ERROR:errorCode = decodeBytesToShort(bytes, 2); break;
		default:break;
		}
	}
	if (len > 4) {
				if (shortOpcode == TFTPPacket::TFTP_OPCODE_ERROR)
					if (nextByte == '\0') return popPacket();

	}
	if (len == 6)
	{
		if (shortOpcode == TFTPPacket::TFTP_OPCODE_DATA)
		{
			currentBlockNumber = decodeBytesToShort(bytes, 4);
		}
	}
	if (len >= 6)
	{
		if (shortOpcode == TFTPPacket::TFTP_OPCODE_DATA)
		{
			if (currentPacketSize == len - 6)
			{
				data = arrayCopy(bytes, 6, data, 0, currentPacketSize);
				return popPacket();
			}
		}
	}
	return nullptr;
}

void TFTPPacketEncoderDecoder::pushByte(char nextByte)
{
	bytes.push_back(nextByte);
	len++;
}

TFTPPacket* TFTPPacketEncoderDecoder::popPacket()
{
	TFTPPacket* returnPacket = new TFTPPacket();
	switch (shortOpcode)
	{
	case TFTPPacket::TFTP_OPCODE_LOGRQ:returnPacket->createLOGRQ(std::string(bytes.begin() + 2, bytes.end() - 1)); len = 0; break;
	case TFTPPacket::TFTP_OPCODE_DELRQ:returnPacket->createDELRQ(std::string(bytes.begin() + 2, bytes.end() - 1)); len = 0; break;
	case TFTPPacket::TFTP_OPCODE_RRQ:returnPacket->createRRQ(std::string(bytes.begin() + 2, bytes.end() - 1)); len = 0; break;
	case TFTPPacket::TFTP_OPCODE_WRQ:returnPacket->createWRQ(std::string(bytes.begin() + 2, bytes.end() - 1)); len = 0; break;
	case TFTPPacket::TFTP_OPCODE_DIRQ:returnPacket->createDIRQ(); len = 0; len = 0; break;
	case TFTPPacket::TFTP_OPCODE_DATA:returnPacket->createDATA(currentPacketSize, currentBlockNumber, data); len = 0; break;
	case TFTPPacket::TFTP_OPCODE_ACK:returnPacket->createACK(currentBlockNumber); len = 0; break;
	case TFTPPacket::TFTP_OPCODE_ERROR:returnPacket->createERROR(errorCode, std::string(bytes.begin() + 4, bytes.end() - 1)); len = 0; break;
	case TFTPPacket::TFTP_OPCODE_DISC:returnPacket->createDISC(); len = 0; break;
	case TFTPPacket::TFTP_OPCODE_BCAST:returnPacket->createBCAST(fileActionBCAST, std::string(bytes.begin() + 3, bytes.end() - 1)); len = 0; break;
	default:returnPacket->createIllegal(); len = 0; ; break;
	}
	return returnPacket;
}

std::vector<char> TFTPPacketEncoderDecoder::encode(TFTPPacket* packet)
{
	short packetType = packet->getOpcode();
	switch (packetType)
	{
	case TFTPPacket::TFTP_OPCODE_ACK: return encodeACK(packet);
	case TFTPPacket::TFTP_OPCODE_ERROR: return encodeERROR(packet);
	case TFTPPacket::TFTP_OPCODE_DATA: return encodeDATA(packet);
	case TFTPPacket::TFTP_OPCODE_LOGRQ: return encodeLOGRQ(packet);
	case TFTPPacket::TFTP_OPCODE_RRQ: return encodeRRQ(packet);
	case TFTPPacket::TFTP_OPCODE_WRQ: return encodeWRQ(packet);
	case TFTPPacket::TFTP_OPCODE_DIRQ: return encodeDIRQ(packet);
	case TFTPPacket::TFTP_OPCODE_DELRQ: return encodeDELRQ(packet);
	case TFTPPacket::TFTP_OPCODE_DISC: return encodeDISC(packet);

	}
}

std::vector<char> TFTPPacketEncoderDecoder::encodeACK(TFTPPacket* packet)
{
	std::vector<char> encodedPacket(4);
	std::vector<char> encodedOpcode = shortToBytes(packet->getOpcode());
	std::vector<char> encodedBlockNumber = shortToBytes(packet->getBlockNumber());
	addOpcodeToEncodedPacket(encodedOpcode, encodedPacket);
	encodedPacket[2] = encodedBlockNumber[0];
	encodedPacket[3] = encodedBlockNumber[1];

	return encodedPacket;

}

std::vector<char> TFTPPacketEncoderDecoder::encodeERROR(TFTPPacket* packet)
{
	std::vector<char> encodedPacket;
	std::vector<char> encodedOpcode = shortToBytes(packet->getOpcode());
	std::vector<char> encodedErrorCode = shortToBytes(packet->getErrorCode());
	std::vector<char> encodedErrorMessage = stringToBytes(packet->getErrorMessage());
	encodedPacket = std::vector<char>(encodedErrorMessage.size() + 2 + 2 + 1); // ErrorMessage + Opcode + Errorcode + zerobyte
	addOpcodeToEncodedPacket(encodedOpcode, encodedPacket);
	addErrorCodeToEncodedPacket(encodedErrorCode, encodedPacket);
	addEncStringToEncPacket(encodedErrorMessage, encodedPacket, 4);

	return encodedPacket;
}

std::vector<char> TFTPPacketEncoderDecoder::encodeDATA(TFTPPacket* packet)
{
	std::vector<char> encodedPacket;
	std::vector<char> encodedOpcode = shortToBytes(packet->getOpcode());
	std::vector<char> encodedBlockNumber = shortToBytes(packet->getBlockNumber());
	std::vector<char> encodedPacketSize = shortToBytes(packet->getPacketSize());
	std::vector<char> data = packet->getData();
	encodedPacket = mergeEncodedParts(encodedOpcode, encodedPacketSize, encodedBlockNumber, data, encodedPacket);

	return encodedPacket;
}

std::vector<char> TFTPPacketEncoderDecoder::encodeLOGRQ(TFTPPacket* packet)
{
	std::vector<char> encodedPacket;
	std::vector<char> encodedOpcode = shortToBytes(packet->getOpcode());
	std::vector<char> encodedUsername = stringToBytes(packet->getUsername());
	encodedPacket = std::vector<char>(encodedUsername.size() + 2 + 1); // Username + Opcode + zerobyte
	addOpcodeToEncodedPacket(encodedOpcode, encodedPacket);
	addEncStringToEncPacket(encodedUsername, encodedPacket, 2);

	return encodedPacket;
}

std::vector<char> TFTPPacketEncoderDecoder::encodeRRQ(TFTPPacket* packet)
{
	std::vector<char> encodedPacket;
	std::vector<char> encodedOpcode = shortToBytes(packet->getOpcode());
	std::vector<char> encodedFilename = stringToBytes(packet->getFilename());
	encodedPacket = std::vector<char>(encodedFilename.size() + 2 + 1); // Filename + Opcode + zerobyte
	addOpcodeToEncodedPacket(encodedOpcode, encodedPacket);
	addEncStringToEncPacket(encodedFilename, encodedPacket, 2);

	return encodedPacket;
}

std::vector<char> TFTPPacketEncoderDecoder::encodeWRQ(TFTPPacket* packet)
{
	std::vector<char> encodedPacket;
	std::vector<char> encodedOpcode = shortToBytes(packet->getOpcode());
	std::vector<char> encodedFilename = stringToBytes(packet->getFilename());
	encodedPacket = std::vector<char>(encodedFilename.size() + 2 + 1); // Filename + Opcode + zerobyte
	addOpcodeToEncodedPacket(encodedOpcode, encodedPacket);
	addEncStringToEncPacket(encodedFilename, encodedPacket, 2);

	return encodedPacket;
}

std::vector<char> TFTPPacketEncoderDecoder::encodeDIRQ(TFTPPacket* packet)
{
	std::vector<char> encodedPacket;
	std::vector<char> encodedOpcode = shortToBytes(packet->getOpcode());
	encodedPacket = std::vector<char>(2); // Opcode
	addOpcodeToEncodedPacket(encodedOpcode, encodedPacket);
	return encodedPacket;
}

std::vector<char> TFTPPacketEncoderDecoder::encodeDELRQ(TFTPPacket* packet)
{
	std::vector<char> encodedPacket;
	std::vector<char> encodedOpcode = shortToBytes(packet->getOpcode());
	std::vector<char> encodedFilename = stringToBytes(packet->getFilename());
	encodedPacket = std::vector<char>(encodedFilename.size() + 2 + 1); // Filename + Opcode + zerobyte
	addOpcodeToEncodedPacket(encodedOpcode, encodedPacket);
	addEncStringToEncPacket(encodedFilename, encodedPacket, 2);

	return encodedPacket;
}

std::vector<char> TFTPPacketEncoderDecoder::encodeDISC(TFTPPacket* packet)
{
	std::vector<char> encodedPacket;
	std::vector<char> encodedOpcode = shortToBytes(packet->getOpcode());
	encodedPacket = std::vector<char>(2); // Opcode
	addOpcodeToEncodedPacket(encodedOpcode, encodedPacket);
	return encodedPacket;
}

std::vector<char> TFTPPacketEncoderDecoder::mergeEncodedParts(std::vector<char> &encodedOpcode, std::vector<char> &encodedPacketSize, std::vector<char> &encodedBlockNumber, std::vector<char> &data, std::vector<char> &encodedPacket)
{
	encodedPacket = std::vector<char>(0); // data size + (short) packetsize + blockNumber + Opcode
	encodedPacket = arrayCopy(encodedOpcode, 0, encodedPacket, 0, 2);
	encodedPacket = arrayCopy(encodedPacketSize, 0, encodedPacket, 2, 2);
	encodedPacket = arrayCopy(encodedBlockNumber, 0, encodedPacket, 4, 2);
	encodedPacket = arrayCopy(data, 0, encodedPacket, 6, decodeBytesToShort(encodedPacketSize, 0));

	return encodedPacket;
}

void TFTPPacketEncoderDecoder::addErrorCodeToEncodedPacket(std::vector<char> &encodedErrorCode, std::vector<char> &encodedPacket)
{
	encodedPacket[2] = encodedErrorCode[0];
	encodedPacket[3] = encodedErrorCode[1];
}

void TFTPPacketEncoderDecoder::addOpcodeToEncodedPacket(std::vector<char> &encodedOpcode, std::vector<char> &encodedPacket)
{
	encodedPacket[0] = encodedOpcode[0];
	encodedPacket[1] = encodedOpcode[1];
}

void TFTPPacketEncoderDecoder::addEncStringToEncPacket(std::vector<char> &encodedString, std::vector<char> &encodedPacket, int offset)
{
	for (size_t i = 0; i < encodedString.size(); i++)
	{
		encodedPacket[i + offset] = encodedString[i];
	}
	addEndingZeroByte(encodedPacket);
}

void TFTPPacketEncoderDecoder::addEndingZeroByte(std::vector<char> &encodedPacket)
{
	encodedPacket[encodedPacket.size() - 1] = '\0';
}

short TFTPPacketEncoderDecoder::decodeBytesToShort(std::vector<char> &bytesArr, int offset)
{
	short result = (short)((bytesArr[0 + offset] & 0xff) << 8);
	result += (short)(bytesArr[1 + offset] & 0xff);
	return result;
}



std::vector<char> TFTPPacketEncoderDecoder::shortToBytes(short num)
{
	std::vector<char> bytesArr(2);
	bytesArr[0] = ((num >> 8) & 0xFF);
	bytesArr[1] = (num & 0xFF);
	return bytesArr;
}

std::vector<char> TFTPPacketEncoderDecoder::stringToBytes(const std::string &filename)
{
	std::vector<char> filenameVec(filename.begin(), filename.end());
	return filenameVec;
}

void TFTPPacketEncoderDecoder::init()
{
	bytes = std::vector<char>();
	data = std::vector<char>();
}

std::vector<char> TFTPPacketEncoderDecoder::arrayCopy(std::vector<char> src, int srcPos, std::vector<char> dest, int destPos, int length)
{
	std::vector<char> newDest = std::vector<char>(src.size() - srcPos + dest.size() );
	for (size_t i = 0; i < dest.size(); i++)
		newDest[i] = dest[i];
	for (size_t i = 0; i < length; i++) {
		newDest[(size_t)destPos + i] = src[(size_t)srcPos + i];
	}
	return newDest;
}


