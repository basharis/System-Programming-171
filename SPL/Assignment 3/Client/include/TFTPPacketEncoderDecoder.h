#pragma once

#include <string>
#include <vector>
#include <iostream>

#include <atomic>
#include "TFTPPacket.h"

class TFTPPacketEncoderDecoder
{

private:
	std::vector<char> bytes;
	int len = 0;
	short currentBlockNumber = 0;
	short shortOpcode = 0;
	short currentPacketSize = 0;
	std::vector<char> data;
	short errorCode = 0;
	char fileActionBCAST = 0;


	void pushByte(char nextByte);
	TFTPPacket *popPacket();
	std::vector<char> encodeACK(TFTPPacket *packet);
	std::vector<char> encodeERROR(TFTPPacket *packet);
	std::vector<char> encodeDATA(TFTPPacket *packet);
	std::vector<char> encodeLOGRQ(TFTPPacket *packet);
	std::vector<char> encodeRRQ(TFTPPacket *packet);
	std::vector<char> encodeWRQ(TFTPPacket *packet);
	std::vector<char> encodeDIRQ(TFTPPacket *packet);
	std::vector<char> encodeDELRQ(TFTPPacket *packet);
	std::vector<char> encodeDISC(TFTPPacket *packet);
	std::vector<char> mergeEncodedParts(std::vector<char> &encodedOpcode, std::vector<char> &encodedPacketSize, std::vector<char> &encodedBlockNumber, std::vector<char> &data, std::vector<char> &encodedPacket);
	void addErrorCodeToEncodedPacket(std::vector<char> &encodedErrorCode, std::vector<char> &encodedPacket);
	void addOpcodeToEncodedPacket(std::vector<char> &encodedOpcode, std::vector<char> &encodedPacket);
	void addEncStringToEncPacket(std::vector<char> &encodedString, std::vector<char> &encodedPacket, int offset);
	void addEndingZeroByte(std::vector<char> &encodedPacket);
	short decodeBytesToShort(std::vector<char> &bytesArr, int offset);
	std::vector<char> shortToBytes(short num);
	std::vector<char> stringToBytes(const std::string &filename);

public:
	TFTPPacketEncoderDecoder();
	virtual TFTPPacket* decodeNextByte(char nextByte);
	virtual std::vector<char> encode(TFTPPacket *packet);
	virtual ~TFTPPacketEncoderDecoder() {};
	void init();
	static std::vector<char> arrayCopy(std::vector<char> src, int srcPos, std::vector<char> dest, int destPos, int length);
	static std::vector<char> arrayCopyForDataBreak(std::vector<char> src, int srcPos, std::vector<char> dest, int destPos, int length);

	



};