#include "TFTPPacket.h"

//Constructor
TFTPPacket::TFTPPacket() {}

void TFTPPacket::createIllegal(){
	this->opcode = TFTP_OPCODE_ILLEGALPACKET;
}

void TFTPPacket::createRRQ(const std::string &filename){
	this->opcode = TFTP_OPCODE_RRQ;
	this->filename = filename;
}

void TFTPPacket::createLOGRQ(const std::string &username){
	this->opcode = TFTP_OPCODE_LOGRQ;
	this->username = username;
}

void TFTPPacket::createDISC(){
	this->opcode = TFTP_OPCODE_DISC;

}

void TFTPPacket::createDELRQ(const std::string &filename){
	this->opcode = TFTP_OPCODE_DELRQ;
	this->filename = filename;
}

void TFTPPacket::createWRQ(const std::string &filename){
	this->opcode = TFTP_OPCODE_WRQ;
	this->filename = filename;

}

void TFTPPacket::createDIRQ(){
	this->opcode = TFTP_OPCODE_DIRQ;

}

void TFTPPacket::createDATA(short packetSize, short blockNumber, std::vector<char> &data){
	this->opcode = TFTP_OPCODE_DATA;
	this->packetSize = packetSize;
	this->blockNumber = blockNumber;
	this->data = data;
}

void TFTPPacket::createACK(short blockNumber){
	this->opcode = TFTP_OPCODE_ACK;
	this->blockNumber = blockNumber;
}

void TFTPPacket::createBCAST(char fileActionBCAST, const std::string &filename){
	this->opcode = TFTP_OPCODE_BCAST;
	this->fileActionBCAST = fileActionBCAST;
	this->filename = filename;

}

void TFTPPacket::createERROR(short errorCode, const std::string &errorMessage){
	this->opcode = TFTP_OPCODE_ERROR;
	this->errorCode = errorCode;
	this->errorMessage = errorMessage;

}

short TFTPPacket::getOpcode(){
	return opcode;
}

short TFTPPacket::getBlockNumber(){
	return blockNumber;
}

char TFTPPacket::getActionBCAST()
{
	return fileActionBCAST;
}

std::string TFTPPacket::getFilename(){
	return filename;
}

std::string TFTPPacket::getUsername(){
	return username;
}

short TFTPPacket::getErrorCode(){
	return errorCode;
}

std::string TFTPPacket::getErrorMessage(){
	return errorMessage;
}

short TFTPPacket::getPacketSize(){
	return packetSize;
}

std::vector<char> TFTPPacket::getData(){
	return data;
}