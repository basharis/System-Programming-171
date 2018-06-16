package bgu.spl171.net.impl.TFTPreactor;

import bgu.spl171.net.impl.tftp.TFTPBidiMessagingProtocolImpl;
import bgu.spl171.net.impl.tftp.TFTPPacketEncoderDecoder;
import bgu.spl171.net.srv.Server;

public class ReactorMain {
	public static void main(String[] args){
		Server.reactor(
				//Integer.parseInt(args[0]),
				4, //nthreads
				Integer.parseInt(args[1]), //port
				() -> new TFTPBidiMessagingProtocolImpl(), //protocol factory
				() ->  new TFTPPacketEncoderDecoder()  //message encoder decoder factory
				).serve();
	}
}