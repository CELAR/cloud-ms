package eu.celarcloud.celar_ms.ServerPack;

import java.util.Map.Entry;
import java.util.logging.Level;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.ServerPack.Beans.AgentObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.AgentObj.AgentStatus;
import eu.celarcloud.celar_ms.ServerPack.Database.AgentDAO;

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
										AgentDAO.updateAgent(this.server.dbHandler.getConnection(), curAgent.getAgentID(), AgentObj.AgentStatus.DOWN.name());
									} 
				    			    catch (CatascopiaException e) {
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
			for(String met:agent.getMetricList())
				this.server.metricMap.remove(met);
			this.server.agentMap.remove(host);
			if(this.server.getDatabaseFlag())
				//AgentDAO.updateAgent(this.server.dbHandler.getConnection(), agent.getAgentID(), AgentObj.AgentStatus.DEAD.name());
				AgentDAO.deleteAgent(this.server.dbHandler.getConnection(), agent.getAgentID());
		
		}
		catch(Exception e){
			this.server.writeToLog(Level.SEVERE, e);
		}
	}
}
