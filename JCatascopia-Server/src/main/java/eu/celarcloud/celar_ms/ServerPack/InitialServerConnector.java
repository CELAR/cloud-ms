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
package eu.celarcloud.celar_ms.ServerPack;

import java.util.UUID;
import eu.celarcloud.celar_ms.SocketPack.Dealer;

public class InitialServerConnector{
	
	public static boolean connect(String ip, String port, String protocol,String serverID,String serverIP){
		Dealer dealer = new Dealer(ip, port, UUID.randomUUID().toString().replace("-", ""));

		int attempts = 0; 
		boolean connected = false;
    	String json_request;
    	String[] response = null;
    	try {
			json_request = "{\"serverID\":\""+serverID+"\",\"serverIP\":\""+serverIP+"\"}";
			
			while(((attempts++)<3) && (!connected)){
	    		dealer.send("","SERVER.CONNECT",json_request);
	            response = dealer.receive(12000L);
	            if (response != null){
	            	connected = (response[1].contains("OK")) ? true : false;
	            	break;
	            }
				else	
					Thread.sleep(3000);
	    	}
			dealer.close();
		} 
    	catch (InterruptedException e){
			;
		}  
    	return connected;
	}
}
