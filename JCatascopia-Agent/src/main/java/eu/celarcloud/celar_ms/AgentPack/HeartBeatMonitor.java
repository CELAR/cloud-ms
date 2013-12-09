package eu.celarcloud.celar_ms.AgentPack;

import java.util.logging.Level;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;

public class HeartBeatMonitor extends Thread{
	
	public enum HBMStatus{INACTIVE,ACTIVE,DYING};
	private HBMStatus hbmStatus;
	private boolean firstFlag;
	
	private int NUM_OF_RETRYS;
	private long PERIOD; 
	
	public String serverIP;
	public String serverPort;
	
	private IJCatascopiaAgent agent;
	
	public HeartBeatMonitor(IJCatascopiaAgent agent,int retrys,int period,String ip, String port){
		super("HeartBeatMonitor-Thread");
		this.hbmStatus = HBMStatus.INACTIVE;
		this.firstFlag = true;
		
		this.agent = agent;
		
		this.NUM_OF_RETRYS = retrys;
		this.PERIOD = 1000 * period;
		
		this.serverIP = ip;
		this.serverPort = port;
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
			this.agent.writeToLog(Level.INFO, "HeartBeatMonitor activated");
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
		int attempts = 0;
		try{
			while(this.hbmStatus != HBMStatus.DYING){
				if(this.hbmStatus == HBMStatus.ACTIVE){
					if (this.agent.inDebugMode())
						System.out.println("\nHeartBeatMonitor>> checking...\n");
				    
					if (!ServerConnector.connect(serverIP,serverPort,"tcp",agent.getAgentID(),agent.getAgentIP(),agent.getProbeMap())){
						this.agent.writeToLog(Level.WARNING, "Monitoring Server at: "+serverIP+" is not responding");
						if((++attempts) > NUM_OF_RETRYS)
							throw new CatascopiaException("Completely lost connection with Monitoring Server: "+serverIP,CatascopiaException.ExceptionType.CONNECTION);
					}
					else attempts = 0;
					
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
			this.agent.writeToLog(Level.SEVERE, e);
		} 
		catch (CatascopiaException e) {
			this.agent.writeToLog(Level.SEVERE, e);
			System.exit(1); //need to exit gracefully when agent.terminate() is implemented
		}
	}	
}
