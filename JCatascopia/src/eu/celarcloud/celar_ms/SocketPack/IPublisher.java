package eu.celarcloud.celar_ms.SocketPack;

public interface IPublisher {
	public enum ConnectType {BIND,CONNECT};
	
	public String getPort();
	public void setPort(String port);
	public String getIPAddress();
	public void setIPAddress(String ip);
	public String getProtocol();
	public void setProtocol(String protocol);
	public void close();
	
	public void send(String msg);
//	public void sendMore();
}
