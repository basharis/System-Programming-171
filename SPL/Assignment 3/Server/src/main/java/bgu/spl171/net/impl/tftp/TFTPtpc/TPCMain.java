package bgu.spl171.net.impl.tftp.TFTPtpc;

import bgu.spl171.net.impl.tftp.TFTPBidiMessagingProtocolImpl;
import bgu.spl171.net.impl.tftp.TFTPPacketEncoderDecoder;
import bgu.spl171.net.srv.Server;

public class TPCMain {
//////////////////////// PORT IS ARGS 0
	public static void main(String[] args){
      Server.threadPerClient(
    		  //Integer.parseInt(args[0]),
    		  8400, //port
              () -> new TFTPBidiMessagingProtocolImpl(), //protocol factory
              () ->  new TFTPPacketEncoderDecoder()  //message encoder decoder factory
      ).serve();
	}
}
