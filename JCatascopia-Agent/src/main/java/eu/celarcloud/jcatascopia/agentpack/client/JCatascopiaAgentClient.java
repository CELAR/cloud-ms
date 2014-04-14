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
package eu.celarcloud.jcatascopia.agentpack.client;

import java.util.UUID;

import eu.celarcloud.jcatascopia.agentpack.sockets.Dealer;

/**
 * Client interface to communicate with JCatascopia Monitoring Agents
 * 
 * @author Demetris Trihinas
 *
 */
public class JCatascopiaAgentClient {
	
	public JCatascopiaAgentClient(){
		
	}
	
	public boolean deployProbe(String agentIP, String port, String probeClassContainer, String probeClass){
		Dealer dealer = new Dealer(agentIP, port, UUID.randomUUID().toString().replace("-", ""));
		String msg = "{\"container\":\""+probeClassContainer+"\",\"class\":\""+probeClass+"\"}";
		dealer.send("","AGENT.ADD_PROBE",msg);
		String[] response = dealer.receive(10000L);
		dealer.close();
		if (response != null)
			if (response[1].contains("OK"))
				return true;
		return false;
	}
}
