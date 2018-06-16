#ifndef CONNECTION_HANDLER__
#define CONNECTION_HANDLER__

#include <string>
#include <iostream>
#include <boost/asio.hpp>
#include "TFTPPacket.h"
#include "TFTPPacketEncoderDecoder.h"

using boost::asio::ip::tcp;

class ConnectionHandler {
private:
	const std::string host_;
	const short port_;
	boost::asio::io_service io_service_;   // Provides core I/O functionality
	tcp::socket socket_;
	TFTPPacketEncoderDecoder encdec = TFTPPacketEncoderDecoder();

public:
	ConnectionHandler(std::string host, short port);
	virtual ~ConnectionHandler();

	// Connect to the remote machine
	bool connect();

	// Read a fixed number of bytes from the server - blocking.
	// Returns false in case the connection is closed before bytesToRead bytes can be read.
	bool getByte(char* byte);

	// Send a fixed number of bytes from the client - blocking.
	// Returns false in case the connection is closed before all the data is sent.
	bool sendByte(char* byte);

	bool send(TFTPPacket* packetToSend);

	// Close down the connection properly.
	void close();

}; //class ConnectionHandler

#endif