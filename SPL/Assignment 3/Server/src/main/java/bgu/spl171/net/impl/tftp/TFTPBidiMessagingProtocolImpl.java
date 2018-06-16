package bgu.spl171.net.impl.tftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.srv.ConnectionsImpl;
import bgu.spl171.net.srv.bidi.ConnectionHandler;

public class TFTPBidiMessagingProtocolImpl implements BidiMessagingProtocol<TFTPPacket> {

	static ConcurrentHashMap<String, Integer> usernames = new ConcurrentHashMap<String, Integer>(); 
	static ConcurrentHashMap<String, Integer> filesCurrentlyUploading = new ConcurrentHashMap<String, Integer>();	

	private boolean shouldTerminate = false;
	private boolean hasWriteAccess = false;
	private final short MAX_PACKET_SIZE = 512;
	private byte[] receivedData = new byte[0];
	private final File filesFolder = new File("Files");
	private int lastBlockNumberReceived = 0;
	private Integer connectionID;
	private String myUsername = "";
	private ConnectionsImpl<TFTPPacket> connections;
	private String fileCurrentlyUploading = "";
	private ConcurrentLinkedQueue<TFTPPacket> dataBlocks = new ConcurrentLinkedQueue<TFTPPacket>();



	@Override
	public void start(int connectionID, Connections<TFTPPacket> connections) {
		this.connectionID = new Integer(connectionID);
		this.connections = (ConnectionsImpl<TFTPPacket>) connections;

	}

	@Override
	public void process(TFTPPacket packet) {
		switch(packet.getOpcode()){
		case TFTPPacket.TFTP_OPCODE_DISC: processDISC(packet); break;
		case TFTPPacket.TFTP_OPCODE_LOGRQ: processLOGRQ(packet); break;
		case TFTPPacket.TFTP_OPCODE_DELRQ: processDELRQ(packet); break;
		case TFTPPacket.TFTP_OPCODE_RRQ: processRRQ(packet); break;
		case TFTPPacket.TFTP_OPCODE_WRQ: processWRQ(packet); break;
		case TFTPPacket.TFTP_OPCODE_DIRQ: processDIRQ(packet); break;
		case TFTPPacket.TFTP_OPCODE_DATA: processDATA(packet); break;
		case TFTPPacket.TFTP_OPCODE_ERROR: processERROR(packet); break;
		case TFTPPacket.TFTP_OPCODE_ACK: processACK(packet); break;
		case TFTPPacket.TFTP_OPCODE_ILLEGALPACKET: processIllegalOpcode(); break;
		default: break;
		}
	}

	private void processDATA(TFTPPacket packet) {
		TFTPPacket response = new TFTPPacket();
		if (isConnectionIDLoggedIn(connectionID))
		{
			if (hasWriteAccess){
				if (packet.getBlockNumber() == lastBlockNumberReceived+1)
				{
					if (packet.getPacketSize() == 512){
						byte[] packetData = packet.getData();
						receivedData = Arrays.copyOf(receivedData, receivedData.length+packetData.length);
						System.arraycopy(packetData, 0, receivedData, lastBlockNumberReceived*512 , packetData.length);
						response.createACK(packet.getBlockNumber()); sendPacket(response);
						response = new TFTPPacket();
						lastBlockNumberReceived++;
					}
					else if (packet.getPacketSize() < 512){
						byte[] packetData = packet.getData();
						receivedData = Arrays.copyOf(receivedData, receivedData.length+packetData.length);
						System.arraycopy(packetData, 0, receivedData, lastBlockNumberReceived*512 , packetData.length);
						writeDataToDisk();
						response.createACK(packet.getBlockNumber()); sendPacket(response);
						response = new TFTPPacket();
						broadcast((byte) 1, fileCurrentlyUploading);
						lastBlockNumberReceived++;
						clearUploadValues();
					}
					else{ //bigger than 512
						response.createERROR(TFTPPacket.TFTP_ERROR_NOT_DEFINED, "Packet size exceeds protocol standards.");
						sendPacket(response);
					}
					
				}
				else{
					response.createERROR(TFTPPacket.TFTP_ERROR_NOT_DEFINED, "Block order mismatch.");
					sendPacket(response);
				}
			}
			else{
				response.createERROR(TFTPPacket.TFTP_ERROR_ACCESS_DENIED, "User did not send RRQ prior to DATA.");
				sendPacket(response);
			}
		}
		else{
			response.createERROR(TFTPPacket.TFTP_ERROR_NOT_LOGGED_IN, "Only logged-in users can send DATA packets.");
			sendPacket(response);
		}
	}

	private void clearUploadValues() {
		receivedData = new byte[0];
		hasWriteAccess = false;
		lastBlockNumberReceived = 0;
		if (filesCurrentlyUploading.contains(fileCurrentlyUploading)){
			filesCurrentlyUploading.remove(fileCurrentlyUploading);
		}
		fileCurrentlyUploading = "";
	}

	private void writeDataToDisk() {
		try (FileOutputStream fos = new FileOutputStream(filesFolder.getPath()+"/"+fileCurrentlyUploading)){
			fos.write(receivedData);
		}
		catch (IOException e) {
			clearUploadValues();
			TFTPPacket response = new TFTPPacket();
			response.createERROR(TFTPPacket.TFTP_ERROR_ACCESS_DENIED, "Could not write file to disk.");
			sendPacket(response);
		}
	}

	private void processIllegalOpcode() {
		TFTPPacket response = new TFTPPacket();
		response.createERROR(TFTPPacket.TFTP_OPCODE_ERROR, "Illegal TFTP Opcode.");
		sendPacket(response);
	}

	private void processACK(TFTPPacket packet) {
		TFTPPacket response = new TFTPPacket();
		if (isConnectionIDLoggedIn(connectionID)){
			if (!dataBlocks.isEmpty()){
				if (packet.getBlockNumber() == dataBlocks.peek().getBlockNumber()-1){
					sendPacket(dataBlocks.remove());
				}
				else{
					response.createERROR(TFTPPacket.TFTP_ERROR_NOT_DEFINED, "Block order mismatch.");
					sendPacket(response);
					dataBlocks.clear();
				}
			}
		}
		else{
			response.createERROR(TFTPPacket.TFTP_ERROR_NOT_LOGGED_IN, "Only logged-in users can request directory listing from the server.");
			sendPacket(response);
		}
	}

	private void processERROR(TFTPPacket packet) {
		TFTPPacket response = new TFTPPacket();
		if (isConnectionIDLoggedIn(connectionID)){
			// stop RRQ
			dataBlocks.clear();
			// stop WRQ
			clearUploadValues();
		}
		else{
			response.createERROR(TFTPPacket.TFTP_ERROR_NOT_LOGGED_IN, "Only logged-in users can send ERROR packets.");
			sendPacket(response);
		}
	}

	private void processDISC(TFTPPacket packet) {
		TFTPPacket response = new TFTPPacket();
		if (isConnectionIDLoggedIn(connectionID)){
			shouldTerminate = true;
			response.createACK((short) 0);
			sendPacket(response);
			usernames.remove(myUsername);
			connections.disconnect(connectionID);
		}
		else{
			response.createERROR(TFTPPacket.TFTP_ERROR_NOT_LOGGED_IN, "Only logged-in users can send DISC packets.");
			sendPacket(response);
		}
	}

	private void processDELRQ(TFTPPacket packet) {
		TFTPPacket response = new TFTPPacket();
		String filenameToDelete = packet.getFilename();
		File fileToDelete = new File(filesFolder.getPath()+"/"+filenameToDelete);
		if (isConnectionIDLoggedIn(connectionID)){
			try{
				if (fileToDelete.exists()){
					if (fileToDelete.delete()){
						response.createACK((short) 0);
						sendPacket(response);
						broadcast((byte) 0, filenameToDelete);
					}
					else{
						response.createERROR(TFTPPacket.TFTP_ERROR_ACCESS_DENIED, "The file cannot be deleted.");
						sendPacket(response);}
				}
				else{
					response.createERROR(TFTPPacket.TFTP_ERROR_FILE_NOT_FOUND, "The file does not exist on this server.");
					sendPacket(response);}
			}
			catch(Exception e){
				response.createERROR(TFTPPacket.TFTP_ERROR_ACCESS_DENIED, "The file cannot be deleted.");
				sendPacket(response);
				e.printStackTrace();
			}
		}
		else{
			response.createERROR(TFTPPacket.TFTP_ERROR_NOT_LOGGED_IN, "Only logged-in users can request deletion of a file from the server.");
			sendPacket(response);
		}
	}


	private void processDIRQ(TFTPPacket packet) {
		byte[] dirListData = new byte[0];
		if (isConnectionIDLoggedIn(connectionID)){
			for (final File fileEntry : filesFolder.listFiles()){
				byte[] arrayToAdd = fileEntry.getName().getBytes();
				int originalLength = dirListData.length;
				dirListData = Arrays.copyOf(dirListData, dirListData.length + arrayToAdd.length + 1);
				System.arraycopy(arrayToAdd, 0, dirListData, originalLength, arrayToAdd.length);
				dirListData[dirListData.length-1] = '\0';

			}
			breakDataIntoPacketstAndSendFirstBlock(dirListData);
		}
		else{
			TFTPPacket response = new TFTPPacket();
			response.createERROR(TFTPPacket.TFTP_ERROR_NOT_LOGGED_IN, "Only logged-in users can request directory listing from the server.");
			sendPacket(response);
		}
	}

	private void processWRQ(TFTPPacket packet) {
		TFTPPacket response = new TFTPPacket();
		String filenameToWrite = packet.getFilename();
		File fileToUpload = new File(filesFolder.getPath()+"/"+filenameToWrite);
		if (isConnectionIDLoggedIn(connectionID)){
			if (fileToUpload.exists()){
				response.createERROR(TFTPPacket.TFTP_ERROR_FILE_EXISTS, "A file with the same name already exists on the server.");
				sendPacket(response);
			}
			else if (filesCurrentlyUploading.contains(filenameToWrite)){
				if(!isConnectionIDLoggedIn(filesCurrentlyUploading.get(filenameToWrite))){
					filesCurrentlyUploading.remove(filenameToWrite);
					hasWriteAccess = true;
					fileCurrentlyUploading = filenameToWrite;
					filesCurrentlyUploading.put(filenameToWrite, connectionID);
					response.createACK((short) 0);
					sendPacket(response);
				}
				else{
					response.createERROR(TFTPPacket.TFTP_ERROR_FILE_EXISTS, "Someone else is trying to upload a file with the same name.");
					sendPacket(response);
				}
			}
			else{
				hasWriteAccess = true;
				fileCurrentlyUploading = filenameToWrite;
				filesCurrentlyUploading.put(filenameToWrite, connectionID);
				response.createACK((short) 0);
				sendPacket(response);
				//broadcast((byte) 1, filenameToWrite);
			}
		}
		else{
			response.createERROR(TFTPPacket.TFTP_ERROR_NOT_LOGGED_IN, "Only logged-in users can write files to the server.");
			sendPacket(response);
		}

	}

	private void processRRQ(TFTPPacket packet) {
		TFTPPacket response = new TFTPPacket();
		String filenameToRead = packet.getFilename();
		File fileToDownload = new File(filesFolder.getPath()+"/"+filenameToRead);
		if (isConnectionIDLoggedIn(connectionID)){
			if (!fileToDownload.exists()){
				response.createERROR(TFTPPacket.TFTP_ERROR_FILE_NOT_FOUND, "This file does not exist on this server.");
				sendPacket(response);
			}
			else{
				Path path = Paths.get(fileToDownload.getPath());
				try {
					byte[] data = Files.readAllBytes(path);
					breakDataIntoPacketstAndSendFirstBlock(data);
				} 
				catch (IOException ignored) {ignored.printStackTrace();}
			}
		}
		else{
			response.createERROR(TFTPPacket.TFTP_ERROR_NOT_LOGGED_IN, "Only logged-in users can read files from the server.");
			sendPacket(response);
		}
	}

	private void processLOGRQ(TFTPPacket packet) {
		TFTPPacket response = new TFTPPacket();
		if (!isConnectionIDLoggedIn(connectionID)){
			if (isUsernameAvailable(packet.getUsername())){
				addLoggedUser(connectionID, packet.getUsername());
				myUsername = packet.getUsername();
				response.createACK((short)0);
				sendPacket(response);
			}
			else{
				response.createERROR(TFTPPacket.TFTP_ERROR_ALREADY_LOGGED_IN, "Username already in use.");
				sendPacket(response);
			}
		}
		else{
			response.createERROR(TFTPPacket.TFTP_ERROR_ALREADY_LOGGED_IN, "This connection ID is associated with another username.");
			sendPacket(response);		
		}

	}

	private void breakDataIntoPacketstAndSendFirstBlock(byte[] dirListData) {
		short numOfPacketsRequired = (short) (dirListData.length / 512 + 1);
		boolean needToSendExtraEmptyPacket = (dirListData.length % 512) == 0;
		for (short i = 1 ; i<numOfPacketsRequired ; i++){
			TFTPPacket packetToSend = new TFTPPacket();
			byte[] currentPacketData = new byte[512];
			System.arraycopy(dirListData, (i-1)*MAX_PACKET_SIZE, currentPacketData, 0, 512);
			packetToSend.createDATA(MAX_PACKET_SIZE, i, currentPacketData);
			dataBlocks.add(packetToSend);
		}
		TFTPPacket lastPacket = new TFTPPacket();
		if (needToSendExtraEmptyPacket){
			lastPacket.createDATA((short) 0, numOfPacketsRequired, new byte[0]);
			dataBlocks.add(lastPacket);
		}
		else{
			short lastPacketSize = (short) (dirListData.length - ((numOfPacketsRequired-1) * 512));
			byte[] lastPacketData = new byte[512];
			System.arraycopy(dirListData, (numOfPacketsRequired-1) * 512, lastPacketData, 0, lastPacketSize);
			lastPacket.createDATA(lastPacketSize, numOfPacketsRequired, lastPacketData);
			dataBlocks.add(lastPacket);
		}
		sendPacket(dataBlocks.remove());
	}

	private void sendPacket(TFTPPacket packetToSend){
		connections.send(connectionID, packetToSend);
	}

	private void broadcast(byte deletedOrAdded, String filename) {
		TFTPPacket broadcastPacket = new TFTPPacket();
		broadcastPacket.createBCAST(deletedOrAdded, filename);
		usernames.forEach((name, id) -> {
			connections.send(id, broadcastPacket);} );
	}		


	@Override
	public boolean shouldTerminate() {
		return shouldTerminate;
	}

	private boolean isUsernameAvailable(String username){
		return (!usernames.containsKey(username));
	}

	private boolean isConnectionIDLoggedIn(Integer connectionID){
		return (usernames.contains(connectionID));
	}

	private void addLoggedUser(Integer connectionID, String username){
		usernames.put(username, connectionID);
	}


}
