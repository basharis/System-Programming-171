# All Targets
all: makeclient



# Tool invocations
# Executable "hello" depends on the files hello.o and run.o.
makeclient: bin/TFTPPacketEncoderDecoder.o bin/TFTPPacket.o bin/TFTPClientHelper.o bin/TFTPClient.o bin/ProcessUserInput.o bin/connectionHandler.o
	g++ -o bin/TFTPclient -L/usr/lib bin/TFTPPacketEncoderDecoder.o bin/TFTPPacket.o bin/TFTPClientHelper.o bin/TFTPClient.o bin/ProcessUserInput.o bin/connectionHandler.o -lboost_system -lboost_thread -lboost_filesystem

# Depends on the source and header files
bin/TFTPPacketEncoderDecoder.o: src/TFTPPacketEncoderDecoder.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/TFTPPacketEncoderDecoder.o src/TFTPPacketEncoderDecoder.cpp

# Depends on the source and header files
bin/TFTPPacket.o: src/TFTPPacket.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/TFTPPacket.o src/TFTPPacket.cpp

# Depends on the source and header files
bin/TFTPClientHelper.o: src/TFTPClientHelper.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/TFTPClientHelper.o src/TFTPClientHelper.cpp

# Depends on the source and header files
bin/TFTPClient.o: src/TFTPClient.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/TFTPClient.o src/TFTPClient.cpp

# Depends on the source and header files
bin/ProcessUserInput.o: src/ProcessUserInput.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/ProcessUserInput.o src/ProcessUserInput.cpp

# Depends on the source and header files
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ -g -Wall -Weffc++ -std=c++11 -c -Iinclude -o bin/connectionHandler.o src/connectionHandler.cpp

#Clean the build directory
clean: 
	rm -f bin/*

#-L~/2usr/lib
#lboo2st_system
#-I~/2usr/lib