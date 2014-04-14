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

public class Publisher implements ISocket{
	private final static int SOCKET_TYPE = ZMQ.PUB;
	private String port;
	private String ip; 
	private ConnectType connectType;
	private ZMQ.Context context;
	private ZMQ.Socket publisher;

	public Publisher(String ip, String port, ConnectType ctype){
		this.port = port;
		this.ip = ip;
	    this.connectType = ctype;
		this.initPublisher();
	}
	
	public Publisher(String ip, String port){
		this(ip, port, ConnectType.BIND);
	}
	
	private void initPublisher(){
		//Create Context and Socket to talk to server
		this.context = ZMQ.context(1);
	    this.publisher = context.socket(Publisher.SOCKET_TYPE);
	    this.publisher.setHWM(16); 	     //High WaterMark, default ZMQ value is 1000 but it's too big
        this.publisher.setLinger(0);    //time to leave messages in queue after disconnect
		String fullAdress = "tcp://"+this.ip+":" + this.port;
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
		return this.ip;
	}
	
	public void setIPAddress(String ip){
		this.ip = ip;
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
		this.publisher.send(msg,0);
	}

	public void send(String addr, String msg_type, String content) throws CatascopiaException {
		this.publisher.send(content);
	}
}
