#ifndef TFTPClient_
#define TFTPClient_

#include <string>
#include <iostream>
#include <queue>
#include <fstream>
#include <sstream>
#include <vector>
#include <boost/asio.hpp>
#include "TFTPPacket.h"
#include "TFTPPacketEncoderDecoder.h"
#include "ProcessUserInput.h"


class TFTPClientHelper {
private:
	std::vector<char> receivedData = std::vector<char>();
	short lastBlockNumberSent;
	short lastBlockNumberReceived;
	std::queue<TFTPPacket*> dataBlocks;
	ConnectionHandler* connectionHandler;
	ProcessUserInput* readUserInputTask;
	std::vector<char> fileByteData;
	bool shouldTerminate_ = false;
	bool sendingData = false;
	short lastBlockRceived;
	TFTPPacketEncoderDecoder encdec = TFTPPacketEncoderDecoder();
	TFTPPacket* received;
	void processERROR(TFTPPacket*);
	void processACK(TFTPPacket*);
	void processDATA(TFTPPacket*);
	void processBCAST(TFTPPacket*);
	void processIllegal(TFTPPacket*);
	void sendData(std::string);
	void breakDataIntoPacketsAndSendFirstBlock(std::vector<char>*);
	void sendPacket(TFTPPacket*);
	void processDIRQ_DATA(TFTPPacket*);
	void processRRQ_DATA(TFTPPacket*);




public:
	TFTPClientHelper(ProcessUserInput*, ConnectionHandler*);
	void process(TFTPPacket*);
	bool shouldTerminate();


};
#endif