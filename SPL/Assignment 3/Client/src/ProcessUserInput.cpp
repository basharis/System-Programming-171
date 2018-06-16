#include "ProcessUserInput.h"
#include <iostream>
#include <string>
#include <boost/filesystem/fstream.hpp>
#include <boost/filesystem/operations.hpp>
#include <boost/filesystem/path.hpp>


ProcessUserInput::ProcessUserInput(ConnectionHandler& connection) {
	this->connection = &connection;
	lastCommandOpcode = -1;
	boost::filesystem::path full_path(boost::filesystem::current_path());
	std::cout << "Current path is : " << full_path << std::endl;
}
void ProcessUserInput::waitingForDISCACK(bool boolean) {
	sentDISCandWaiting = boolean;
}
void ProcessUserInput::setShouldTerminate(bool shouldTerminate) {
	this->shouldTerminate = shouldTerminate;
}

std::string ProcessUserInput::getReadString() {
	return userCommand;
}

void ProcessUserInput::reinitialize() {
	userCommand = "";
}

void ProcessUserInput::operator()() {
	while (!shouldTerminate) {
		const short bufsize = 1024;
		char buf[bufsize];
		std::cin.getline(buf, bufsize);
		std::string line(buf);
		userCommand = line;
		if (!userCommand.empty()) {
			while (userCommand.back() == '\n' || userCommand.back() == '\r') { userCommand.pop_back(); }
			TFTPPacket* packetToSend = processUserCommand(userCommand);
			if (packetToSend->getOpcode() != TFTPPacket::TFTP_OPCODE_ILLEGALPACKET) {
				connection->send(packetToSend);
				delete packetToSend;}
			else
				std::cout << "Illegal command, please try again." << std::endl;
			while (sentDISCandWaiting) {
				std::this_thread::sleep_for(std::chrono::milliseconds(20));
			}
		}
		else
			std::cout << "Illegal command, please try again." << std::endl;
	}
}
TFTPPacket* ProcessUserInput::processUserCommand(std::string userCommand) {
	TFTPPacket* response = new TFTPPacket();
	if (userCommand == "DIRQ") {
		response->createDIRQ();
		lastCommandOpcode = TFTPPacket::TFTP_OPCODE_DIRQ;
	}
	else if (userCommand == "DISC") {
		response->createDISC();
		lastCommandOpcode = TFTPPacket::TFTP_OPCODE_DISC;
		waitingForDISCACK(true);

	}
	else {
		size_t spacePos = userCommand.find_first_of(' ');
		std::string commandName = userCommand.substr(0, spacePos);
		commandArgs = userCommand.substr(spacePos + 1);
		if (spacePos != std::string::npos && commandArgs != "") {
			if (commandName == "LOGRQ") {
				response->createLOGRQ(commandArgs);
				lastCommandOpcode = TFTPPacket::TFTP_OPCODE_LOGRQ;
			}
			else if (commandName == "RRQ") {
				response->createRRQ(commandArgs);
				lastCommandOpcode = TFTPPacket::TFTP_OPCODE_RRQ;
			}
			else if (commandName == "WRQ") {
				if (boost::filesystem::exists(commandArgs)) {
					response->createWRQ(commandArgs);
					lastCommandOpcode = TFTPPacket::TFTP_OPCODE_WRQ;
				}
				else {
					response->createIllegal();
				}
			}
			else if (commandName == "DELRQ") {
				response->createDELRQ(commandArgs);
				lastCommandOpcode = TFTPPacket::TFTP_OPCODE_DELRQ;

			}
			else
				response->createIllegal();

		}
		else
			response->createIllegal();
		// std::cout << "Invalid command, please try again." << std::endl;
	}
	return response;
}

short ProcessUserInput::getLastCommandOpcode() {
	return lastCommandOpcode;
}

std::string ProcessUserInput::getLastCommandArgs() {
	return commandArgs;
}