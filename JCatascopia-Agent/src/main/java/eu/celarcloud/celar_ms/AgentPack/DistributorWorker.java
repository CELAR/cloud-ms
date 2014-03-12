package eu.celarcloud.celar_ms.AgentPack;

import java.util.logging.Level;

import eu.celarcloud.celar_ms.AgentPack.aggregators.IAggregator;
import eu.celarcloud.celar_ms.AgentPack.distributors.IDistributor;
import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;

public class DistributorWorker extends Thread{
	/*
	 * INACTIVE - running but distributing messages is paused
	 * ACTIVE   - running and distributing messages
	 * DYING    - in the process of terminating
	 */
	public enum DistributorStatus{INACTIVE,ACTIVE,DYING};
	private DistributorStatus distributorStatus;
	
	private boolean firstFlag;
	private IDistributor distributor;
	private IAggregator aggregator;
	
	//aggregator settings
	private long INTERVAL;
	private int BUF_SIZE;
	
	private IJCatascopiaAgent agent;
	
	public DistributorWorker(IDistributor distributor,IAggregator aggregator,long interval,int buf_size, IJCatascopiaAgent agent){
		super("Distributor-Thread");
		this.distributor = distributor;

		this.distributorStatus = DistributorStatus.INACTIVE;
		this.firstFlag = true;
		
		this.aggregator = aggregator;
		this.INTERVAL = interval;
		this.BUF_SIZE = buf_size;
		
		this.agent = agent;
	}
	
	@Override
	public void start(){
		this.activate();
	}
	
	public synchronized void activate(){
		if (this.distributorStatus == DistributorStatus.INACTIVE){
			if (this.firstFlag){
				super.start();
				this.firstFlag = false;
			}
			else this.notify();
			this.distributorStatus = DistributorStatus.ACTIVE;	
		}	
	}
	
	public void deactivate(){
		this.distributorStatus = DistributorStatus.INACTIVE;
	}
	
	public synchronized void terminate(){
		this.distributor.terminate();
		this.distributorStatus = DistributorStatus.DYING;
		this.notify();
	}
	
	@Override
	public void run(){
		try{
			long interval = 0; long period = 2000;
			while(this.distributorStatus != DistributorStatus.DYING){
				if(this.distributorStatus == DistributorStatus.ACTIVE){
					try{
						if(this.aggregator.length()>0){
							if (interval>INTERVAL || aggregator.length()>BUF_SIZE){
								this.distributor.send(aggregator.toMessage());
								interval = 0;
								this.aggregator.clear();
								if (this.agent.inDebugMode())
									System.out.println("DistributorWorker>> Message sent to JCatascopia Monitoring Server");
							}
							else interval += period;
						}
						Thread.sleep(period);
					}
					catch(CatascopiaException e){
						this.agent.writeToLog(Level.SEVERE, e);
						continue;
					}
					catch(Exception e){
						this.agent.writeToLog(Level.SEVERE, e);
						Thread.sleep(5000);
						this.aggregator.clear();
					}
				}
				else 
					synchronized(this){
						while(this.distributorStatus == DistributorStatus.INACTIVE)
							this.wait();
					}
			}
		}
		catch (InterruptedException e){
			this.agent.writeToLog(Level.SEVERE, e);
		}
		finally{
			this.distributor.terminate();
		}	
	}
}