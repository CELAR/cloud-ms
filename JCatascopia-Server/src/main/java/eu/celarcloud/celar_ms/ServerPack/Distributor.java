package eu.celarcloud.celar_ms.ServerPack;

import java.util.logging.Level;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.SocketPack.Publisher;
import eu.celarcloud.celar_ms.SocketPack.ISocket;;

public class Distributor extends Thread{
	/*
	 * INACTIVE - running but distributing messages is paused
	 * ACTIVE   - running and distributing messages
	 * DYING    - in the process of terminating
	 */
	public enum DistributorStatus{INACTIVE,ACTIVE,DYING};
	private DistributorStatus distributorStatus;
	
	private boolean firstFlag;
	private Publisher publisher;
	private Aggregator aggregator;
	//aggregator settings
	private long INTERVAL;
	private IJCatascopiaServer server;
	
	public Distributor(String ipAddr, String port, String protocol, long hwm,
			            Aggregator aggregator,long interval, IJCatascopiaServer server){
		super("ReDistributor-Thread");
		this.publisher = new Publisher(ipAddr,port,protocol,hwm,ISocket.ConnectType.CONNECT);

		this.distributorStatus = DistributorStatus.INACTIVE;
		this.firstFlag = true;
		
		this.aggregator = aggregator;
		this.INTERVAL = interval;
		
		this.server = server;
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
		this.publisher.close();
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
						if (interval > INTERVAL){
							String s = aggregator.createMessage();
//							System.out.println(s);
							if (aggregator.length()>0)
								this.publisher.send(s);
							interval = 0;
							this.aggregator.clear();
							if (this.server.inDebugMode())
								System.out.println("ReDistributor>> Message sent to MS Server...\n");
						}
						else interval += period;
						Thread.sleep(period);
					}
					catch(CatascopiaException e){
						this.server.writeToLog(Level.SEVERE, e);
					}
					catch(Exception e){
						this.server.writeToLog(Level.SEVERE, e);
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
			this.server.writeToLog(Level.SEVERE, e);
		}
		finally{
			this.publisher.close();
		}	
	}
}