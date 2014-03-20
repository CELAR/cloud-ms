package eu.celarcloud.jcatascopia.agentpack.sockets;

import org.jeromq.ZFrame;
import org.jeromq.ZMQ;
import org.jeromq.ZMsg;

public class Router implements ISocket{
	private final static int SOCKET_TYPE = ZMQ.ROUTER;
	private String port;
	private String ip;
	private ZMQ.Context context;
	private ZMQ.Socket router;
	
	public Router(String ip, String port){
		this.port = port;
		this.ip = ip;
	    this.initRouter();
	}
	
	private void initRouter(){
		 //Create Context and Socket to talk to server
		this.context = ZMQ.context(1);
		this.router = context.socket(Router.SOCKET_TYPE);
		this.router.setLinger(0);
		this.router.setHWM(16);
		String fullAddress = "tcp://"+this.ip+":" + this.port;
		this.router.bind(fullAddress);
	}
	
	public String getPort(){
		return this.port;
	}
	
	public void setPort(String port){
		this.port = port;
	}
	
	public String getIPAddress(){
		return this.ip;
	}
	
	public void setIPAddress(String ip){
		this.ip = ip;
	}
	
	/**
	 * close socket connection by terminating context and closing socket
	 */
	public void close(){
		this.router.close();
	    this.context.term();
	}
	
	/**
	 * blocking receive. Blocks forever or until it receives a message.
	 * Returns a String Array with length equal to 3. First element is the
	 * sender address, second element is the message type and the
	 * third element is the content. 
	 */
	public String[] receive(){
		ZMsg msg = ZMsg.recvMsg(this.router);
		
		String[] recvbuf = parseRecvMsg(msg);
        return recvbuf;	
	}
	
	/**
	 * non-blocking receive. if nothing to receive it returns NULL
	 */
	public String[] receiveNonBlocking(){
		ZMsg msg = ZMsg.recvMsg(this.router,ZMQ.NOBLOCK);
		if (msg == null){
			return null; //nothing to receive
		}
		
		String[] recvbuf = parseRecvMsg(msg);
        return recvbuf;	
	}
	
	/*
	 * null msg here indicate erroneous msg that does not follow Catascopia standard 
	 * of 3 part messages
	 */
	private String[] parseRecvMsg(ZMsg msg){
		ZFrame frame = msg.pop(); //address
        if(frame == null)
        	return null;
        String addr = frame.toString();
        
        frame = msg.pop(); //message type
        if(frame == null)
        	return null;
        String msg_type = frame.toString();
        
        frame = msg.pop(); //content
        if(frame == null)
        	return null;
        String content = frame.toString(); 
        
        msg.destroy();
		String[] recvbuf = new String[3];
        recvbuf[0] = addr;
        recvbuf[1] = msg_type;
        recvbuf[2] = content;
        
        return recvbuf;	
	}
	
	public void send(String addr,String msg_type, String content){
		this.router.send(addr,ZMQ.SNDMORE);
		this.router.send(msg_type,ZMQ.SNDMORE);
		this.router.send(content, 0);
	}

	public void send(String msg){
		this.send("", "", msg);
	}
}
