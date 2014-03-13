package eu.celarcloud.celar_ms.AgentPack.distributors;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.SocketPack.ISocket;
import eu.celarcloud.celar_ms.SocketPack.Publisher;

public class TCPDistributor implements IDistributor{

	private Publisher publisher;
	
	public TCPDistributor(String ip, String port, String url){
		this.publisher = new Publisher(ip, port, ISocket.ConnectType.CONNECT);
	}
	
	public void send(String msg) throws CatascopiaException {
		this.publisher.send(msg);
	}

	public void terminate() {
		this.publisher.close();
	}

}
