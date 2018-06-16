package bgu.spl171.net.srv;

import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.api.MessagingProtocol;
import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.impl.tftp.TFTPPacket;
import bgu.spl171.net.srv.bidi.ConnectionHandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

	private final BidiMessagingProtocol<T> protocol;
	private final MessageEncoderDecoder<T> encdec;
	private volatile boolean connected = true;
	private final Socket sock;
	private BufferedInputStream in;
	private BufferedOutputStream out;
	private Integer myID; 
	private ConnectionsImpl<T> connections;
	
	public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol, ConnectionsImpl<T> connections) {
		this.sock = sock;
		this.encdec = reader;
		this.protocol = protocol;
		this.connections = connections;
	}

	@Override
	public void run() {
		protocol.start(myID, connections);
		try (Socket sock = this.sock) { //just for automatic closing
			int read;
			in = new BufferedInputStream(sock.getInputStream());
			out = new BufferedOutputStream(sock.getOutputStream());
            
			while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
				T nextMessage = encdec.decodeNextByte((byte) read);
				if (nextMessage != null) {
					protocol.process(nextMessage);
				}
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	@Override
	public void close() throws IOException {
		connected = false;
		sock.close();
	}

	@Override
	public void send(T msg) {
		try {
			out.write(encdec.encode(msg));
			out.flush();
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
	public void setID(Integer newID){
		this.myID = newID; 
	}
}
