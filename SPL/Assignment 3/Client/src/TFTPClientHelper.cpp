#include "TFTPClientHelper.h"


TFTPClientHelper::TFTPClientHelper(ProcessUserInput* readUserInputTask, ConnectionHandler* connectionHandler)
{
	this->readUserInputTask = readUserInputTask;
	this->connectionHandler = connectionHandler;
}

void TFTPClientHelper::process(TFTPPacket* packet)
{
	switch (packet->getOpcode()) {
	case TFTPPacket::TFTP_OPCODE_ACK: processACK(packet); break;
	case TFTPPacket::TFTP_OPCODE_DATA: processDATA(packet); break;
	case TFTPPacket::TFTP_OPCODE_ERROR: processERROR(packet); break;
	case TFTPPacket::TFTP_OPCODE_BCAST: processBCAST(packet); break;
	case TFTPPacket::TFTP_OPCODE_ILLEGALPACKET: processIllegal(packet); break;
	}
}

bool TFTPClientHelper::shouldTerminate()
{
	return shouldTerminate_;
}


void TFTPClientHelper::processACK(TFTPPacket* packet)
{
	std::cout << "ACK " << packet->getBlockNumber() << std::endl;
	TFTPPacket responsedummy = TFTPPacket();
	TFTPPacket* response = &responsedummy;
	if (!sendingData) {
		if (packet->getBlockNumber() == 0) {
			short lastCommandOpcode = readUserInputTask->getLastCommandOpcode();
			switch (lastCommandOpcode) {
			case TFTPPacket::TFTP_OPCODE_LOGRQ: break;
			case TFTPPacket::TFTP_OPCODE_DELRQ: break;
			case TFTPPacket::TFTP_OPCODE_DISC: shouldTerminate_ = true; readUserInputTask->setShouldTerminate(true); readUserInputTask->waitingForDISCACK(false); break;
			case TFTPPacket::TFTP_OPCODE_WRQ: sendData(readUserInputTask->getLastCommandArgs()); break;
			}
		}
		else {
			response->createERROR(TFTPPacket::TFTP_ERROR_NOT_DEFINED, "ACK order mismatch");
			sendPacket(response);
		}
	}
	else {
		if (!dataBlocks.empty()) {
			if (packet->getBlockNumber() == lastBlockNumberSent) {
				lastBlockNumberSent = dataBlocks.front()->getBlockNumber();
				sendPacket(dataBlocks.front()); 
				delete dataBlocks.front();
				dataBlocks.pop();
			}
			else {
				response->createERROR(TFTPPacket::TFTP_ERROR_NOT_DEFINED, "ACK order mismatch");
				sendPacket(response);
				//initialize upload process
				dataBlocks = std::queue<TFTPPacket*>();
				sendingData = false;
			}
		}
		else {
			if (packet->getBlockNumber() == lastBlockNumberSent) {
				std::cout << "WRQ " << readUserInputTask->getLastCommandArgs() << " complete" << std::endl;
				sendingData = false;
			}

		}
	}
}

void TFTPClientHelper::processERROR(TFTPPacket* packet)
{
	std::cout << "Error " << packet->getErrorCode() << std::endl;
	sendingData = false;
	dataBlocks = std::queue<TFTPPacket*>();
}


void TFTPClientHelper::processDATA(TFTPPacket* packet)
{
	short lastCommandOpcode = readUserInputTask->getLastCommandOpcode();
	switch (lastCommandOpcode) {
	case TFTPPacket::TFTP_OPCODE_DIRQ: processDIRQ_DATA(packet); break;
	case TFTPPacket::TFTP_OPCODE_RRQ: processRRQ_DATA(packet); break;
	default: {
		TFTPPacket responsedummy = TFTPPacket();
		TFTPPacket* response = &responsedummy;
		response->createERROR(TFTPPacket::TFTP_ERROR_NOT_DEFINED, "Receiving uninvited data.");
		sendPacket(response);
	}
	}

}
void TFTPClientHelper::processBCAST(TFTPPacket* packet)
{
	std::string theAction;
	switch (packet->getActionBCAST()) {
	case 0: theAction = "BCAST del "; break;
	case 1: theAction = "BCAST add "; break;
	}
	std::cout << theAction << packet->getFilename() << std::endl;
}
void TFTPClientHelper::processDIRQ_DATA(TFTPPacket* packet)
{
	TFTPPacket responsedummy = TFTPPacket();
	TFTPPacket* response = &responsedummy;
	if (lastBlockNumberReceived == packet->getBlockNumber() - 1) {
		receivedData = TFTPPacketEncoderDecoder::arrayCopy(packet->getData(), 0, receivedData, receivedData.size(), packet->getPacketSize());
		if (packet->getPacketSize() < 512) {
			std::string listOfFiles(receivedData.begin(), receivedData.end());
			for (size_t i = 0; i < listOfFiles.length(); i++) {
				if (listOfFiles[i] == '\0')
					listOfFiles[i] = '\n';
			}
			std::cout << listOfFiles << std::endl;
			// initialize
			lastBlockNumberReceived = 0;
			encdec.init();
				receivedData = std::vector<char>();
		}
		else { // size equals 512
			lastBlockNumberReceived++;
		}
		response->createACK(packet->getBlockNumber());
		sendPacket(response);
	}
	else {
		response->createERROR(TFTPPacket::TFTP_ERROR_NOT_DEFINED, "ACK order mismatch");
		sendPacket(response);
		lastBlockNumberReceived = 0;
		receivedData = std::vector<char>();
	}
}

void TFTPClientHelper::processRRQ_DATA(TFTPPacket* packet)
{
	TFTPPacket responsedummy = TFTPPacket();
	TFTPPacket* response = &responsedummy;
	if (lastBlockNumberReceived == packet->getBlockNumber() - 1) {
		receivedData = TFTPPacketEncoderDecoder::arrayCopy(packet->getData(), 0, receivedData, receivedData.size(), packet->getPacketSize());
		if (packet->getPacketSize() < 512) {
			std::string path(readUserInputTask->getLastCommandArgs());
			std::ofstream outputFile(path, std::ios::out | std::ofstream::binary);
			std::copy(receivedData.begin(), receivedData.end(), std::ostreambuf_iterator<char>(outputFile));
			outputFile.flush();
			outputFile.close();
			std::cout << "RRQ " << readUserInputTask->getLastCommandArgs() << " complete" << std::endl;
			// initialize
			lastBlockNumberReceived = 0;
			receivedData = std::vector<char>();
		}
		else { // size equals 512
			lastBlockNumberReceived++;
		}
		response->createACK(packet->getBlockNumber());
		sendPacket(response);
	}
	else {
		response->createERROR(TFTPPacket::TFTP_ERROR_NOT_DEFINED, "ACK order mismatch");
		sendPacket(response);
		lastBlockNumberReceived = 0;
		receivedData = std::vector<char>();
	}
}

void TFTPClientHelper::processIllegal(TFTPPacket* packet)
{
	TFTPPacket responsedummy = TFTPPacket();
	TFTPPacket* response = &responsedummy;
	response->createERROR(TFTPPacket::TFTP_ERROR_BAD_OPERATION, "Illegal TFTP Opcode");
	sendPacket(response);
}

void TFTPClientHelper::sendData(std::string fileName)
{
	sendingData = true;
	std::ifstream file(fileName);
	if (!file.eof() && !file.fail())
	{
		file.seekg(0, std::ios_base::end);
		std::streampos fileSize = file.tellg();
		fileByteData.resize(fileSize);
		file.seekg(0, std::ios_base::beg);
		if (file.peek() != std::ifstream::traits_type::eof())
			file.read(&fileByteData[0], fileSize);
	}
	breakDataIntoPacketsAndSendFirstBlock(&fileByteData);
}

std::vector<char> TFTPPacketEncoderDecoder::arrayCopyForDataBreak(std::vector<char> src, int srcPos, std::vector<char> dest, int destPos, int length)
{
	std::vector<char> newDest = std::vector<char>(512);
	for (size_t i = 0; i < length; i++) {
		newDest[(size_t)destPos + i] = src[(size_t)srcPos + i];
	}
	return newDest;
}

void TFTPClientHelper::breakDataIntoPacketsAndSendFirstBlock(std::vector<char>* dirListData)
{
	short numOfPacketsRequired = static_cast<short>(dirListData->size() / 512 + 1);
	bool needToSendExtraEmptyPacket = (dirListData->size() % 512) == 0;
	for (short i = 1; i < numOfPacketsRequired; i++)
	{
		TFTPPacket* packetToSend = new TFTPPacket();
		std::vector<char> currentPacketData = std::vector<char>(512);
		currentPacketData = TFTPPacketEncoderDecoder::arrayCopyForDataBreak(*dirListData, (i - 1) * 512, currentPacketData, 0, 512);
		packetToSend->createDATA(static_cast<short>(512), i, currentPacketData);
		dataBlocks.push(packetToSend);
	}
	TFTPPacket *lastPacket = new TFTPPacket();
	if (needToSendExtraEmptyPacket)
	{
		std::vector<char> datadummy(0);
		lastPacket->createDATA(static_cast<short>(0), numOfPacketsRequired, datadummy);
		dataBlocks.push(lastPacket);
	}
	else
	{
		short lastPacketSize = static_cast<short>(dirListData->size() - ((numOfPacketsRequired - 1) * 512));
		std::vector<char> lastPacketDataDummy = std::vector<char>(0);
		std::vector<char>* lastPacketData = &lastPacketDataDummy;
		*lastPacketData = TFTPPacketEncoderDecoder::arrayCopy(*dirListData, (numOfPacketsRequired - 1) * 512, *lastPacketData, 0, lastPacketSize);
		lastPacket->createDATA(lastPacketSize, numOfPacketsRequired, *lastPacketData);
		dataBlocks.push(lastPacket);
	}
	lastBlockNumberSent = dataBlocks.front()->getBlockNumber();
	sendPacket(dataBlocks.front());
	delete dataBlocks.front();
	dataBlocks.pop();
}
void TFTPClientHelper::sendPacket(TFTPPacket* packet)
{
	connectionHandler->send(packet);
}




