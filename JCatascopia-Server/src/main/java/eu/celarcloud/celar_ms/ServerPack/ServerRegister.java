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

import java.util.logging.Level;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.SocketPack.ISocket;


/* CONNECT
 * {
 *    "serverID" : "a953262dfee44b748453389ba7bfb18a"
 *    "serverIP" : "10.16.21.42"
 * }
 */
public class ServerRegister implements Runnable{
	public enum Status {OK,ERROR,SYNTAX_ERROR,NOT_FOUND,WARNING,CONFLICT};
	
	private String[] msg;
	private ISocket router;
	private MonitoringServer server;
	
	//msg[0] address
	//msg[1] message type
	//msg[2] content
	public ServerRegister(String[] msg, ISocket router, MonitoringServer server){
		this.msg = msg;
		this.router = router;
		this.server = server;
	}
	
	public void run(){
		if (this.server.inDebugMode())
			System.out.println("\nServerRegister>> processing the following message...\n"+msg[0]+" "+msg[1]+"\n"+msg[2]);	
		try {
			JSONParser parser = new JSONParser();
			JSONObject json;

			json = (JSONObject) parser.parse(msg[2]); //parse content
			
			if(msg[1].equals("SERVER.CONNECT"))
				connect(json);
			//else if (msg[1].equals("SERVER.REGISTER"))
				//metrics(json);	
			else
				this.response(Status.ERROR, msg[1]+" request does not exist");
		}	
		catch (NullPointerException e){
			this.server.writeToLog(Level.SEVERE, e);
		}
		catch (Exception e){
			this.server.writeToLog(Level.SEVERE, e);
		}
	}
	
	private void connect(JSONObject json){
		String serverIP = (String) json.get("serverIP");
		String serverID = (String) json.get("serverID");
		if(serverID == null || serverIP == null)
			this.response(Status.SYNTAX_ERROR,"Server details are INVALID");					
		else{
			this.response(Status.OK,"");		
			this.server.writeToLog(Level.INFO, "New intermediate Server added, with ID: "+serverID+" and IP: "+serverIP);
		}
	}
	
	private void response(Status status,String body){
		try{
			String obj = ((body.equals("")) ? status.toString() : (status+"|"+body));
			this.router.send(msg[0], msg[1], obj);
		} 
		catch (CatascopiaException e){
			this.server.writeToLog(Level.SEVERE, e);
		}
	}
}
