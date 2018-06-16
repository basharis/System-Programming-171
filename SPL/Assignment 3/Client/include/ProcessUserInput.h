#ifndef ProcessUserInput_
#define ProcessUserInput_

#include <stdlib.h>
#include <fstream>
#include <thread>
#include <chrono>
#include <iostream>  
#include <boost/thread.hpp>
#include "TFTPPacket.h"
#include "TFTPPacketEncoderDecoder.h"
#include "connectionHandler.h"

class ProcessUserInput {

private:
	std::string userCommand = "";
	bool shouldTerminate = false;
	ConnectionHandler* connection;
	TFTPPacketEncoderDecoder encdec = TFTPPacketEncoderDecoder();
	short lastCommandOpcode;
	std::string commandArgs = "";
	bool sentDISCandWaiting = false;

public:
	ProcessUserInput(ConnectionHandler&);
	void waitingForDISCACK(bool);
	void setShouldTerminate(bool);
	std::string getReadString();
	void reinitialize();
	void operator()();
	TFTPPacket* processUserCommand(std::string);
	short getLastCommandOpcode();
	std::string getLastCommandArgs();

};

#endif