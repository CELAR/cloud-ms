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

import java.util.Map.Entry;
import java.util.logging.Level;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.ServerPack.Beans.AgentObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.AgentObj.AgentStatus;

public class HeartBeatMonitor extends Thread{
	
	public enum HBMStatus{INACTIVE,ACTIVE,DYING};
	private HBMStatus hbmStatus;
	private boolean firstFlag;
	
	private int NUM_OF_RETRYS;
	private long PERIOD; //in seconds
	
	private MonitoringServer server;
	
	public HeartBeatMonitor(MonitoringServer server,int period,int retrys){
		super("HeartBeatMonitor-Thread");
		this.hbmStatus = HBMStatus.INACTIVE;
		this.firstFlag = true;
		
		this.server = server;
		
		this.NUM_OF_RETRYS = retrys;
		this.PERIOD = 1000 * period;
	}
	
	@Override
	public void start(){
		this.activate();
	}
	
	public synchronized void activate(){
		if (this.hbmStatus == HBMStatus.INACTIVE){
			if (this.firstFlag){
				super.start();
				this.firstFlag = false;
			}
			else this.notify();
			this.hbmStatus = HBMStatus.ACTIVE;	
		}	
	}
	
	public void deactivate(){
		this.hbmStatus = HBMStatus.INACTIVE;
	}
	
	public synchronized void terminate(){
		this.hbmStatus = HBMStatus.DYING;
		this.notify();
	}
	
	@Override
	public void run(){
		try{
			while(this.hbmStatus != HBMStatus.DYING){
				if(this.hbmStatus == HBMStatus.ACTIVE){
					if (this.server.inDebugMode())
						System.out.println("\nHeartBeatMonitoring>> checking...\n");
				    
					AgentObj curAgent;
					for (Entry<String,AgentObj> entry : this.server.agentMap.entrySet()){
				    	curAgent = entry.getValue();
				    	if (!curAgent.isRunning()){
				    		curAgent.incrementAttempts();
				    		if(curAgent.getAttempts() >= NUM_OF_RETRYS){
				    			removeAgent(entry.getKey(),curAgent);
				    			this.server.writeToLog(Level.WARNING, "Removed node: "+entry.getKey()+" due to inactivity");
				    		}
				    		else{
				    			if(this.server.getDatabaseFlag())
									try {
										this.server.dbHandler.updateAgent(curAgent.getAgentID(), AgentObj.AgentStatus.DOWN.name());
									} 
				    			    //catch (CatascopiaException e) {
				    			    catch (Exception e) {
										this.server.writeToLog(Level.SEVERE, e);
									}
				    		}				    			
				    	}
				    	curAgent.setStatus(AgentStatus.DOWN);
				    }
					Thread.sleep(PERIOD);
				}
				else 
					synchronized(this){
						while(this.hbmStatus == HBMStatus.INACTIVE)
							this.wait();
					}
			}
		}
		catch (InterruptedException e){
			this.server.writeToLog(Level.SEVERE, e);
		}
	}	
	
	private void removeAgent(String host,AgentObj agent){
		try{
			for(String met:agent.getMetricList()){
				this.server.metricMap.remove(met);
				if(this.server.getDatabaseFlag())
					this.server.dbHandler.deleteMetric(agent.getAgentID(), met);
			}
			this.server.agentMap.remove(host);
			if(this.server.getDatabaseFlag())
				//TODO add option in config file to delete or keep log of terminated agents
				//this.server.dbHandler.deleteAgent(agent.getAgentID());
				this.server.dbHandler.updateAgent(agent.getAgentID(), AgentObj.AgentStatus.TERMINATED.name());
		
		}
		catch(Exception e){
			this.server.writeToLog(Level.SEVERE, e);
		}
	}
}
