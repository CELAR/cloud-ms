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
