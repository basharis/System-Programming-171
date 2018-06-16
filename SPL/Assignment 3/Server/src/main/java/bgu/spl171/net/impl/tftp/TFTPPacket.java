package bgu.spl171.net.impl.tftp;

public class TFTPPacket {

	static final short TFTP_OPCODE_RRQ = 1;
	static final short TFTP_OPCODE_WRQ = 2;
	static final short TFTP_OPCODE_DATA = 3;
	static final short TFTP_OPCODE_ACK = 4;
	static final short TFTP_OPCODE_ERROR = 5;
	static final short TFTP_OPCODE_DIRQ = 6;
	static final short TFTP_OPCODE_LOGRQ = 7;
	static final short TFTP_OPCODE_DELRQ = 8;
	static final short TFTP_OPCODE_BCAST = 9;
	static final short TFTP_OPCODE_DISC = 10;
	static final short TFTP_OPCODE_ILLEGALPACKET = 11; 
	
	static final short TFTP_ERROR_NOT_DEFINED = 0;
	static final short TFTP_ERROR_FILE_NOT_FOUND = 1;
	static final short TFTP_ERROR_ACCESS_DENIED = 2;
	static final short TFTP_ERROR_DISK_FULL = 3;
	static final short TFTP_ERROR_BAD_OPERATION = 4;
	static final short TFTP_ERROR_FILE_EXISTS = 5;
	static final short TFTP_ERROR_NOT_LOGGED_IN = 6;
	static final short TFTP_ERROR_ALREADY_LOGGED_IN = 7;

	private short opcode=-1;
	private short errorCode;
	private String errorMessage;
	private byte[] data;
	private String filename;
	private String username;
	private short packetSize;
	private short blockNumber;
	private byte fileActionBCAST; // 0 for deleted, 1 for added


	public void createIllegal(){
		this.opcode = TFTP_OPCODE_ILLEGALPACKET;
	}

	void createRRQ(String filename){
		this.opcode = TFTP_OPCODE_RRQ;
		this.filename = filename;
	}

	public void createLOGRQ(String username) {
		this.opcode = TFTP_OPCODE_LOGRQ;
		this.username = username;
	}

	public void createDISC() {
		this.opcode = TFTP_OPCODE_DISC;

	}

	public void createDELRQ(String filename) {
		this.opcode = TFTP_OPCODE_DELRQ;
		this.filename = filename;
	}

	public void createWRQ(String filename) {
		this.opcode = TFTP_OPCODE_WRQ;
		this.filename = filename;

	}

	public void createDIRQ() {
		this.opcode = TFTP_OPCODE_DIRQ;

	}

	public void createDATA(short packetSize, short blockNumber, byte[] data) {
		this.opcode = TFTP_OPCODE_DATA;
		this.packetSize = packetSize;
		this.blockNumber = blockNumber;
		this.data = data;
	}

	public void createACK(short blockNumber) {
		this.opcode = TFTP_OPCODE_ACK;
		this.blockNumber = blockNumber;
	}

	public void createBCAST(byte fileActionBCAST, String filename) {
		this.opcode = TFTP_OPCODE_BCAST;
		this.fileActionBCAST = fileActionBCAST;
		this.filename = filename;

	}

	public void createERROR(short errorCode, String errorMessage) {
		this.opcode = TFTP_OPCODE_ERROR;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;

	}

	public short getOpcode() {
		return opcode;
	}

	public short getBlockNumber() {
		return blockNumber;
	}
	
	public byte getActionBCAST() {
		return fileActionBCAST;
	}
	public String getFilename() {
		return filename;
	}

	public String getUsername() {
		return username;
	}

	public short getErrorCode() {
		return errorCode;
	}
	
	public String getErrorMessage(){
		return errorMessage;
	}

	public short getPacketSize() {
		return packetSize;
	}

	public byte[] getData() {
		return data;
	}


}
