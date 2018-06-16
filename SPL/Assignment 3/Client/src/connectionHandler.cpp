#include "connectionHandler.h"

using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cout;
using std::endl;
using std::string;

ConnectionHandler::ConnectionHandler(string host, short port) : host_(host), port_(port), io_service_(), socket_(io_service_) {}

ConnectionHandler::~ConnectionHandler() {
	close();
}

bool ConnectionHandler::connect() {
	std::cout << "Starting connect to "
		<< host_ << ":" << port_ << std::endl;
	try {
		tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
		boost::system::error_code error;
		socket_.connect(endpoint, error);
		if (error)
			throw boost::system::system_error(error);
	}
	catch (std::exception& e) {
		std::cout << "Connection failed (Error: " << e.what() << ')' << std::endl;
		return false;
	}
	return true;
}

bool ConnectionHandler::getByte(char* byte) {
	boost::system::error_code error;
	try {
		socket_.read_some(boost::asio::buffer(byte, 1), error);
		if (error)
			throw boost::system::system_error(error); // return false; ##############
	}
	catch (std::exception& e) {
		std::cout << "recv failed (Error: " << e.what() << ')' << std::endl;
		return false;
	}
	return true;
}

bool ConnectionHandler::sendByte(char* toSend) {

	boost::system::error_code error;
	try {
		socket_.write_some(boost::asio::buffer(toSend, 1), error);
		if (error)
			throw boost::system::system_error(error);
	}
	catch (std::exception& e) {
		std::cout << "send failed (Error: " << e.what() << ')' << std::endl;
		return false;
	}
	return true;
}

bool ConnectionHandler::send(TFTPPacket* packetToSend)
{
	std::vector<char> toSend = encdec.encode(packetToSend);	
	for (size_t i = 0; i < toSend.size(); i++) {
		bool byteSent = sendByte(&toSend[i]);
		if (!byteSent)
			return false;
	}
	return true;
}

void ConnectionHandler::close()
{
	socket_.close();
}
