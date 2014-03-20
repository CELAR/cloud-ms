package eu.celarcloud.jcatascopia.agentpack.distributors;

import eu.celarcloud.jcatascopia.agentpack.sockets.ISocket;
import eu.celarcloud.jcatascopia.agentpack.sockets.Publisher;
import eu.celarcloud.jcatascopia.exceptions.CatascopiaException;

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
