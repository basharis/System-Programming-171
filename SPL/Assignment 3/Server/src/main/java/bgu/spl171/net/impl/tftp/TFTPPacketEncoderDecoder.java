package bgu.spl171.net.impl.tftp;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import bgu.spl171.net.api.MessageEncoderDecoder;

public class TFTPPacketEncoderDecoder implements MessageEncoderDecoder<TFTPPacket> {

	private byte[] bytes = new byte[(1 << 9)+6]; 
	private AtomicInteger len = new AtomicInteger(0);
	private short currentBlockNumber;
	private short shortOpcode;
	private short currentPacketSize;
	private byte[] data;
	private short errorCode;
	private byte fileActionBCAST;

	@Override
	public TFTPPacket decodeNextByte(byte nextByte) {
		pushByte(nextByte);
		if (len.get() == 2){
			shortOpcode = decodeBytesToShort(bytes, 0);
			switch (shortOpcode){
			case TFTPPacket.TFTP_OPCODE_DIRQ: return popPacket();
			case TFTPPacket.TFTP_OPCODE_DISC: return popPacket();
			case TFTPPacket.TFTP_OPCODE_ACK : break;
			case TFTPPacket.TFTP_OPCODE_BCAST : break;
			case TFTPPacket.TFTP_OPCODE_DATA : break;
			case TFTPPacket.TFTP_OPCODE_DELRQ : break;
			case TFTPPacket.TFTP_OPCODE_ERROR : break;
			case TFTPPacket.TFTP_OPCODE_LOGRQ : break;
			case TFTPPacket.TFTP_OPCODE_RRQ : break;
			case TFTPPacket.TFTP_OPCODE_WRQ : break;
			default: return popPacket();
			}
		}
		if (len.get() > 2){
			switch (shortOpcode){
			case TFTPPacket.TFTP_OPCODE_LOGRQ: if (nextByte == '\0') {return popPacket(); } break;
			case TFTPPacket.TFTP_OPCODE_DELRQ: if (nextByte == '\0') {return popPacket(); } break;
			case TFTPPacket.TFTP_OPCODE_RRQ: if (nextByte == '\0') {return popPacket(); } break;
			case TFTPPacket.TFTP_OPCODE_WRQ: if (nextByte == '\0') {return popPacket(); } break;
			case TFTPPacket.TFTP_OPCODE_BCAST: if (nextByte == '\0') {return popPacket(); } break;
			case TFTPPacket.TFTP_OPCODE_ERROR: if (nextByte == '\0') {return popPacket(); } break;
			default: break;
			}
		}
		if (len.get() == 3){
			if (shortOpcode == TFTPPacket.TFTP_OPCODE_BCAST){
				fileActionBCAST = bytes[len.get()-1] ; return popPacket();
			}
		}
		if (len.get() == 4){
			switch (shortOpcode){
			case TFTPPacket.TFTP_OPCODE_ACK: currentBlockNumber = decodeBytesToShort(bytes, 2); return popPacket();
			case TFTPPacket.TFTP_OPCODE_DATA: 
				currentPacketSize = decodeBytesToShort(bytes, 2);  
				break;
			case TFTPPacket.TFTP_OPCODE_ERROR: errorCode = decodeBytesToShort(bytes, 2);  break;
			default: break;
			}
		}
		if (len.get() == 6){
			if (shortOpcode == TFTPPacket.TFTP_OPCODE_DATA){
				currentBlockNumber = decodeBytesToShort(bytes, 4);
			}
		}
		if (len.get() > 4){
			if (shortOpcode == TFTPPacket.TFTP_OPCODE_DATA){
				if (currentPacketSize == len.get()-6){
					data = new byte[currentPacketSize];
					System.arraycopy(bytes, 6, data, 0, currentPacketSize);

					return popPacket();
				}
			}
		}

		return null;
	}


	private void pushByte(byte nextByte) {
		bytes[len.getAndIncrement()] = nextByte;
	}

	private TFTPPacket popPacket() {
		TFTPPacket returnPacket = new TFTPPacket();
		switch (shortOpcode){
		case TFTPPacket.TFTP_OPCODE_LOGRQ: returnPacket.createLOGRQ(new String(bytes,2,len.getAndSet(0)-3)); break;
		case TFTPPacket.TFTP_OPCODE_DELRQ: returnPacket.createDELRQ(new String(bytes,2,len.getAndSet(0)-3)); break;
		case TFTPPacket.TFTP_OPCODE_RRQ: returnPacket.createRRQ(new String(bytes,2,len.getAndSet(0)-3)); break;
		case TFTPPacket.TFTP_OPCODE_WRQ: returnPacket.createWRQ(new String(bytes,2,len.getAndSet(0)-3)); break;
		case TFTPPacket.TFTP_OPCODE_DIRQ: returnPacket.createDIRQ(); len.set(0); break;
		case TFTPPacket.TFTP_OPCODE_DATA:  returnPacket.createDATA(currentPacketSize, currentBlockNumber, data); len.set(0); break;
		case TFTPPacket.TFTP_OPCODE_ACK: returnPacket.createACK(currentBlockNumber); len.set(0); break;
		case TFTPPacket.TFTP_OPCODE_ERROR: returnPacket.createERROR(errorCode, new String(bytes,4,len.getAndSet(0)-5)); break;
		case TFTPPacket.TFTP_OPCODE_DISC: returnPacket.createDISC(); len.set(0); break;
		case TFTPPacket.TFTP_OPCODE_BCAST: returnPacket.createBCAST(fileActionBCAST, new String(bytes,3,len.getAndSet(0)-4)); break;
		default: returnPacket.createIllegal(); len.set(0); break;
		}
		return returnPacket;
	}


	@Override
	public byte[] encode(TFTPPacket packet) {
		short packetType = packet.getOpcode();
		switch (packetType){
		case TFTPPacket.TFTP_OPCODE_ACK: return encodeACK(packet);
		case TFTPPacket.TFTP_OPCODE_ERROR: return encodeERROR(packet);
		case TFTPPacket.TFTP_OPCODE_DATA: return encodeDATA(packet);
		case TFTPPacket.TFTP_OPCODE_BCAST: return encodeBCAST(packet);
		default: return null;
		}
	}


	private byte[] encodeACK(TFTPPacket packet) {
		byte[] encodedPacket = new byte[4];
		byte[] encodedOpcode = shortToBytes(packet.getOpcode());
		byte[] encodedBlockNumber = shortToBytes(packet.getBlockNumber());
		addOpcodeToEncodedPacket(encodedOpcode, encodedPacket);
		encodedPacket[2] = encodedBlockNumber[0];
		encodedPacket[3] = encodedBlockNumber[1];

		return encodedPacket;

	}


	private byte[] encodeERROR(TFTPPacket packet){
		byte[] encodedPacket;
		byte[] encodedOpcode = shortToBytes(packet.getOpcode());
		byte[] encodedErrorCode = shortToBytes(packet.getErrorCode());
		byte[] encodedErrorMessage = stringToBytes(packet.getErrorMessage());
		encodedPacket = new byte[encodedErrorMessage.length+2+2+1]; // ErrorMessage + Opcode + Errorcode + zerobyte
		addOpcodeToEncodedPacket(encodedOpcode, encodedPacket);
		addErrorCodeToEncodedPacket(encodedErrorCode, encodedPacket);
		addEncStringToEncPacket(encodedErrorMessage, encodedPacket, 4);

		return encodedPacket;
	}


	private byte[] encodeDATA(TFTPPacket packet) {
		byte[] encodedPacket = null;
		byte[] encodedOpcode = shortToBytes(packet.getOpcode());
		byte[] encodedBlockNumber = shortToBytes(packet.getBlockNumber());
		byte[] encodedPacketSize = shortToBytes(packet.getPacketSize());
		byte[] data = packet.getData();
		encodedPacket = mergeEncodedParts(encodedOpcode, encodedPacketSize , encodedBlockNumber, data, encodedPacket);
		return encodedPacket;
	}


	private byte[] encodeBCAST(TFTPPacket packet) {
		byte[] encodedPacket;
		byte[] encodedOpcode = shortToBytes(packet.getOpcode());
		byte[] encodedFileName = stringToBytes(packet.getFilename());
		encodedPacket = new byte[encodedFileName.length+2+1+1]; // filename + action + opcode + zerobyte
		addOpcodeToEncodedPacket(encodedOpcode, encodedPacket);
		addEncStringToEncPacket(encodedFileName, encodedPacket, 3);
		encodedPacket[2] = packet.getActionBCAST();

		return encodedPacket;
	}


	private byte[] mergeEncodedParts(byte[] encodedOpcode, byte[] encodedPacketSize, byte[] encodedBlockNumber, byte[] data, byte[] encodedPacket) {	
		encodedPacket = new byte[decodeBytesToShort(encodedPacketSize, 0)+2+2+2]; // data size + (short) packetsize + blockNumber + Opcode
		System.arraycopy(encodedOpcode, 0, encodedPacket, 0, 2);
		System.arraycopy(encodedPacketSize, 0, encodedPacket, 2, 2);
		System.arraycopy(encodedBlockNumber, 0, encodedPacket, 4, 2);
		//System.arraycopy(data, 0, encodedPacket, 6, data.length);
		System.arraycopy(data, 0, encodedPacket, 6, decodeBytesToShort(encodedPacketSize, 0));
		return encodedPacket;
	}


	private void addErrorCodeToEncodedPacket(byte[] encodedErrorCode, byte[] encodedPacket) {
		encodedPacket[2] = encodedErrorCode[0];
		encodedPacket[3] = encodedErrorCode[1];
	}


	private void addOpcodeToEncodedPacket (byte[] encodedOpcode, byte[] encodedPacket) {
		encodedPacket[0] = encodedOpcode[0];
		encodedPacket[1] = encodedOpcode[1];
	}


	private void addEncStringToEncPacket(byte[] encodedString, byte[] encodedPacket, int offset){
		for (int i=0 ; i<encodedString.length ; i++)
			encodedPacket[i+offset] = encodedString[i];
		addEndingZeroByte(encodedPacket);
	}


	private void addEndingZeroByte(byte[] encodedPacket) {
		encodedPacket[encodedPacket.length-1] = '\0';
	}


	private short decodeBytesToShort(byte[] bytesArr, int offset) {	
		short result = (short)((bytesArr[0+offset] & 0xff) << 8);
		result += (short)(bytesArr[1+offset] & 0xff);
		return result;
	}


	private byte[] shortToBytes(short num){
		byte[] bytesArr = new byte[2];
		bytesArr[0] = (byte)((num >> 8) & 0xFF);
		bytesArr[1] = (byte)(num & 0xFF);
		return bytesArr;
	}


	private byte[] stringToBytes(String filename) {
		return filename.getBytes();
	}


}