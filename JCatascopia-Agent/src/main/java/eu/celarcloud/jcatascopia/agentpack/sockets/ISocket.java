package eu.celarcloud.jcatascopia.agentpack.sockets;

import eu.celarcloud.jcatascopia.exceptions.CatascopiaException;

public interface ISocket{
	public enum ConnectType {BIND,CONNECT};
	
	public String getPort();
	public void setPort(String port);
	public String getIPAddress();
	public void setIPAddress(String ip);
	public void close();
	
	//for SUB and ROUTER sockets
	public String[] receive() throws CatascopiaException;
	public String[] receiveNonBlocking() throws CatascopiaException;
	
	//for PUB and ROUTER sockets
	public void send(String msg) throws CatascopiaException;
	public void send(String addr,String msg_type, String content) throws CatascopiaException;
}
