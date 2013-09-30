package eu.celarcloud.celar_ms.SocketPack;

import org.jeromq.ZMQ;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;

public class Subscriber implements ISocket{
	private final static int SOCKET_TYPE = ZMQ.SUB;
	private String port;
	private String ipAddress; //IP address - can be localhost
	private String protocol; //TCP, UDP, etc.
	private long hwm;  //High WaterMark, default 1000 for zmq which is too big
	private ConnectType connectType;
	private ZMQ.Context context;
	private ZMQ.Socket subscriber;
	
	public Subscriber(String ipAddr, String port, String protocol, long hwm,ConnectType ctype){
		this.port = port;
		this.ipAddress = ipAddr;
		this.protocol = protocol;
	    this.hwm = hwm;
	    this.connectType = ctype;
		this.initSubscriber();
	}
	
	public Subscriber(String ipAddr, String port, String protocol, long hwm){
		this(ipAddr,port,protocol,hwm,ConnectType.CONNECT);
	}

	private void initSubscriber(){
        //Create Context and Socket to talk to server
		this.context = ZMQ.context(1);
	    this.subscriber = context.socket(Subscriber.SOCKET_TYPE);
	    this.subscriber.setHWM(this.hwm);
		this.subscriber.subscribe("".getBytes());
		String fullAdress = this.protocol+"://"+this.ipAddress+":" + this.port;
	    if (this.connectType == ConnectType.BIND)
	    	this.subscriber.bind(fullAdress); 
	    else 
	    	this.subscriber.connect(fullAdress);
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
	 * blocking receive. Blocks forever or until it receives a message.
	 * SUB messages are only one part messages
	 */
	public String[] receive(){
		String[] msg = new String[1];
		msg[0] = this.subscriber.recvStr(0);
		return msg;
	}
	
	/**
	 * non-blocking receive. if nothing to receive it returns NULL.
	 * SUB messages are only one part messages
	 */
	public String[] receiveNonBlocking(){
		String s = this.subscriber.recvStr(ZMQ.NOBLOCK);
		String[] msg = null;
		if (s != null){
			msg = new String[1];
			msg[0] = s;
		}
		return msg;
	}
	
	/**
	 * close socket connection by terminating context and closing socket
	 */
	public void close(){
		this.subscriber.close();
	    this.context.term();
	}

	public void send(String msg) throws CatascopiaException {
		throw new CatascopiaException("send method not available for SUB sockets",CatascopiaException.ExceptionType.TYPE);	
	}

	public void send(String addr, String msg_type, String content) throws CatascopiaException {
		throw new CatascopiaException("send method not available for SUB sockets",CatascopiaException.ExceptionType.TYPE);			
	}
}
