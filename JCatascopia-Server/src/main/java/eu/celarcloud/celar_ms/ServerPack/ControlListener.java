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

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;

public class ControlListener extends Listener{
	
	private MonitoringServer server;
	
	public ControlListener(String ip, String port, MonitoringServer server) throws CatascopiaException {
		super(ListenerType.ROUTER, ip, port, 2000L, server);
		
		this.server = server;
	}

	@Override
	public void listen(String[] msg){
		if (msg[1].contains("SUBSCRIPTION"))
			this.server.controlExecutor.process(new SubProcessor(msg,this.getListener(),this.server));		
		else if (msg[1].contains("AGENT"))
			this.server.controlExecutor.process(new AgentRegister(msg,this.getListener(),this.server));
		else if (msg[1].contains("SERVER"))
			this.server.controlExecutor.process(new ServerRegister(msg,this.getListener(),this.server));	
	}

}
