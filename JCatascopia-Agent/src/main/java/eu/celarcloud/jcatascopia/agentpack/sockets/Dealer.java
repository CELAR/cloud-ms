/*******************************************************************************
 * Copyright 2014, Laboratory of Internet Computing (LInC), Department of Computer Science, University of Cyprus
 * 
 * For any information relevant to JCatascopia Monitoring System,
 * please contact Demetris Trihinas, trihinas{at}cs.ucy.ac.cy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.celarcloud.jcatascopia.agentpack.sockets;

import org.jeromq.ZFrame;
import org.jeromq.ZMQ;
import org.jeromq.ZMsg;


public class Dealer implements ISocket{
	private final static int SOCKET_TYPE = ZMQ.DEALER;
	private String port;
	private String ip;
	private ZMQ.Context context;
	private ZMQ.Socket dealer;
	
	public Dealer(String ip, String port, String identity){
		this.port = port;
		this.ip = ip;
	    this.initDealer(identity);
	}
	
	private void initDealer(String identity){
		//Create Context and Socket to talk to server
		this.context = ZMQ.context(1);
		this.dealer = context.socket(Dealer.SOCKET_TYPE);
		this.dealer.setLinger(0);
		this.dealer.setHWM(8);
		this.dealer.setIdentity(identity.getBytes());
		String fullAddress = "tcp://"+this.ip+":" + this.port;
		this.dealer.connect(fullAddress);
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
		//disconnect is needed because a server may never reply to our request and the
		//socket will never close with open requests
		this.dealer.close();
	    this.context.term();
	}
	
	/**
	 * blocking receive. Blocks forever or until it receives a message.
	 * Returns a String Array with length equal to 3. First element is the
	 * sender address, second element is the message type and the
	 * third element is the content. 
	 */
	public String[] receive(){
		ZMsg msg = ZMsg.recvMsg(this.dealer);
		String[] recvbuf = parseRecvMsg(msg);
        return recvbuf;	
	}
	
	/**
	 * non-blocking receive. if nothing to receive it returns NULL
	 */
	public String[] receiveNonBlocking(){
		ZMsg msg = ZMsg.recvMsg(this.dealer,ZMQ.NOBLOCK);
		if (msg == null){
			return null; //nothing to receive
		}
		
		String[] recvbuf = parseRecvMsg(msg);
        return recvbuf;	
	}
	
	/**
	 * blocking receive with timeout. Blocks until it receives a message
	 * or timeout has expired. if timeout has expired then null is returned.
	 */
	public String[] receive(long timeout){
		String[] recvbuf;
		int i = 0;
		while(i<timeout){
			recvbuf = this.receiveNonBlocking();
			if(recvbuf != null)
				return recvbuf;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i+=100;
		}
		return null;
	}
	
	/*
	 * null msg here indicate erroneous msg that does not follow JCatascopia standard 
	 * of 3 part messages 
	 */
	private String[] parseRecvMsg(ZMsg msg){
		ZFrame frame = msg.pop(); //message type
     
        if(frame == null)
        	return null;
        String msg_type = frame.toString();
        
        frame = msg.pop(); //content
        if(frame == null)
        	return null;
        String content = frame.toString(); 
        
        msg.destroy();
		String[] recvbuf = new String[2];
        recvbuf[0] = msg_type;
        recvbuf[1] = content;
        
        return recvbuf;	
	}
	
	public void send(String addr,String msg_type, String content){
		this.dealer.send(msg_type,ZMQ.SNDMORE);
		this.dealer.send(content, 0);
	}

	public void send(String msg){
		this.send("", "", msg);
	}
}
