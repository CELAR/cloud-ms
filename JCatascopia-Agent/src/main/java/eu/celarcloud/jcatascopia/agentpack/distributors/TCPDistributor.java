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
