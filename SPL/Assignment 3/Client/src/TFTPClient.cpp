#include <stdlib.h>
#include "connectionHandler.h"
#include <iostream>  
#include <boost/thread.hpp>
#include "ProcessUserInput.h"
#include "TFTPClientHelper.h"
#include "TFTPPacket.h"


int main(int argc, char *argv[]) {

	TFTPPacketEncoderDecoder encdec = TFTPPacketEncoderDecoder();
	TFTPPacket* received;
	if (argc < 3) {
		std::cout << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
		return -1;
	}
	std::string host = argv[1];
	short port = atoi(argv[2]);

	ConnectionHandler connectionHandler(host, port);
	if (!connectionHandler.connect()) {
		std::cout << "Cannot connect to " << host << ":" << port << std::endl;
		return 1;
	}
	ProcessUserInput* readUserInputTask = new ProcessUserInput(connectionHandler);
	boost::thread userInputThread(boost::ref(*readUserInputTask));
	bool shouldTerminate = false;
	TFTPClientHelper helper = TFTPClientHelper(readUserInputTask, &connectionHandler);
	char nextByte;
	while (!helper.shouldTerminate() && connectionHandler.getByte(&nextByte)) {
		received = encdec.decodeNextByte(nextByte);
		if (received != nullptr) {
			helper.process(received);
			encdec.init();
			delete received;
		}
	}
	connectionHandler.close();
	delete readUserInputTask;
	std::cin.get();
	return 0;
}
