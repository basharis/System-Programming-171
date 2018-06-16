#pragma once

#include <string>
#include <vector>

class TFTPPacket
{

public:
	static constexpr short TFTP_OPCODE_RRQ = 1;
	static constexpr short TFTP_OPCODE_WRQ = 2;
	static constexpr short TFTP_OPCODE_DATA = 3;
	static constexpr short TFTP_OPCODE_ACK = 4;
	static constexpr short TFTP_OPCODE_ERROR = 5;
	static constexpr short TFTP_OPCODE_DIRQ = 6;
	static constexpr short TFTP_OPCODE_LOGRQ = 7;
	static constexpr short TFTP_OPCODE_DELRQ = 8;
	static constexpr short TFTP_OPCODE_BCAST = 9;
	static constexpr short TFTP_OPCODE_DISC = 10;
	static constexpr short TFTP_OPCODE_ILLEGALPACKET = 11;

	static constexpr short TFTP_ERROR_NOT_DEFINED = 0;
	static constexpr short TFTP_ERROR_FILE_NOT_FOUND = 1;
	static constexpr short TFTP_ERROR_ACCESS_DENIED = 2;
	static constexpr short TFTP_ERROR_DISK_FULL = 3;
	static constexpr short TFTP_ERROR_BAD_OPERATION = 4;
	static constexpr short TFTP_ERROR_FILE_EXISTS = 5;
	static constexpr short TFTP_ERROR_NOT_LOGGED_IN = 6;
	static constexpr short TFTP_ERROR_ALREADY_LOGGED_IN = 7;

private:
	short opcode = -1;
	short errorCode = 0;
	std::string errorMessage;
	std::vector<char> data;
	std::string filename;
	std::string username;
	short packetSize = 0;
	short blockNumber = 0;
	char fileActionBCAST = 0; // 0 for deleted, 1 for added


public:
	TFTPPacket();
	virtual void createIllegal();
	virtual void createRRQ(const std::string &filename);
	virtual void createLOGRQ(const std::string &username);
	virtual void createDISC();
	virtual void createDELRQ(const std::string &filename);
	virtual void createWRQ(const std::string &filename);
	virtual void createDIRQ();
	virtual void createDATA(short packetSize, short blockNumber, std::vector<char> &data);
	virtual void createACK(short blockNumber);
	virtual void createBCAST(char fileActionBCAST, const std::string &filename);
	virtual void createERROR(short errorCode, const std::string &errorMessage);
	virtual short getOpcode();
	virtual short getBlockNumber();
	virtual char getActionBCAST();
	virtual std::string getFilename();
	virtual std::string getUsername();
	virtual short getErrorCode();
	virtual std::string getErrorMessage();
	virtual short getPacketSize();
	virtual std::vector<char> getData();


};