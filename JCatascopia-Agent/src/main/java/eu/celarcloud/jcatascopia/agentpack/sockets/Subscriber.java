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

import org.jeromq.ZMQ;

import eu.celarcloud.jcatascopia.exceptions.CatascopiaException;

public class Subscriber implements ISocket{
	private final static int SOCKET_TYPE = ZMQ.SUB;
	private String port;
	private String ip; 
	private ConnectType connectType;
	private ZMQ.Context context;
	private ZMQ.Socket subscriber;
	
	public Subscriber(String ip, String port, ConnectType ctype){
		this.port = port;
		this.ip = ip;
	    this.connectType = ctype;
		this.initSubscriber();
	}
	
	public Subscriber(String ip, String port, String protocol, long hwm){
		this(ip, port, ConnectType.CONNECT);
	}

	private void initSubscriber(){
        //Create Context and Socket to talk to server
		this.context = ZMQ.context(1);
	    this.subscriber = context.socket(Subscriber.SOCKET_TYPE);
	    this.subscriber.setHWM(32);
		this.subscriber.subscribe("".getBytes());
		String fullAdress = "tcp://"+this.ip+":" + this.port;
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
		return this.ip;
	}
	
	public void setIPAddress(String ip){
		this.ip = ip;
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
