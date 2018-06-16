package bgu.spl171.net.srv;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.impl.tftp.TFTPPacket;
import bgu.spl171.net.srv.bidi.ConnectionHandler;

public class ConnectionsImpl<T> implements Connections<T> {

	ConcurrentHashMap<Integer, ConnectionHandler<T>> connections = new ConcurrentHashMap<Integer, ConnectionHandler<T>>();
	AtomicInteger size = new AtomicInteger(0);


	public Integer addConnection(ConnectionHandler<T> chToAdd){ // returns the given ID
		connections.put(size.get(), chToAdd);
		return new Integer(size.getAndIncrement());
	}


	public boolean isEmpty(){
		return connections.isEmpty();
	}

	public ConnectionHandler<T> getConnectionHandler(Integer connectionID){
		for (Map.Entry<Integer, ConnectionHandler<T>> entry : connections.entrySet()) {
		    System.out.println(entry.getKey()+" : "+entry.getValue());
		}
		return connections.get(connectionID);
	}

	@Override
	public boolean send(int connectionId, T msg) {
		ConnectionHandler<T> curr = connections.get(connectionId);
		if (curr != null){
			curr.send(msg);
			return true;
		}
		return false;
	}

	@Override
	public void broadcast(T msg) {
		connections.forEach((id, curr) -> {send(id, msg);});
	}

	@Override
	public void disconnect(int connectionId) {
		try {connections.get(connectionId).close();}
		catch (IOException e) {e.printStackTrace();}
		connections.remove(connectionId);
	}

}
