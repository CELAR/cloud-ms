package eu.celarcloud.celar_ms.SocketPack;

import org.jeromq.ZMQ;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;

public class Publisher implements ISocket{
	private final static int SOCKET_TYPE = ZMQ.PUB;
	private String port;
	private String ipAddress; //IP address - can be localhost
	private String protocol; //TCP, UDP, etc.
	private long hwm;  //High WaterMark, default 1000 for zmq which is too big
	private ConnectType connectType;
	private ZMQ.Context context;
	private ZMQ.Socket publisher;

	public Publisher(String ipAddr, String port, String protocol, long hwm,ConnectType ctype){
		this.port = port;
		this.ipAddress = ipAddr;
		this.protocol = protocol;
	    this.hwm = hwm;
	    this.connectType = ctype;
		this.initPublisher();
	}
	
	public Publisher(String ipAddr, String port, String protocol, long hwm){
		this(ipAddr,port,protocol,hwm,ConnectType.BIND);
	}
	
	private void initPublisher(){
		//Create Context and Socket to talk to server
		this.context = ZMQ.context(1);
	    this.publisher = context.socket(Publisher.SOCKET_TYPE);
	    this.publisher.setHWM(this.hwm); //HWM must be set before binding
        this.publisher.setLinger(0);    //time to leave messages in queue after disconnect
		String fullAdress = this.protocol+"://"+this.ipAddress+":" + this.port;
	    if (this.connectType == ConnectType.BIND)
	    	this.publisher.bind(fullAdress); 
	    else 
	    	this.publisher.connect(fullAdress);
	}
	
	public String getPort(){
		return this.port;
	}
	
	public void setPort(String port){
		this.port = port;
	}
	
	public String getIPAddress(){
		return this.ipAddress;
	}
	
	public void setIPAddress(String ip){
		this.ipAddress = ip;
	}
	
	public String getProtocol(){
		return this.protocol;
	}
	
	public void setProtocol(String protocol){
		this.protocol = protocol;
	}

	/**
	 * close socket connection by terminating context and closing socket
	 */
	public void close(){
		this.publisher.close();
	    this.context.term();
	}

	public String[] receive() throws CatascopiaException {
		throw new CatascopiaException("receive method not available for PUB sockets",CatascopiaException.ExceptionType.TYPE);	
	}

	public String[] receiveNonBlocking() throws CatascopiaException {
		throw new CatascopiaException("receive method not available for PUB sockets",CatascopiaException.ExceptionType.TYPE);	
	}

	public void send(String msg) throws CatascopiaException {
		//System.out.println("Sending: " + msg);
		this.publisher.send(msg,0);
	}

	public void send(String addr, String msg_type, String content) throws CatascopiaException {
		this.publisher.send(content);
	}

}
