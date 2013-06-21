package eu.celarcloud.celar_ms.AgentPack;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import eu.celarcloud.celar_ms.SocketPack.IPublisher;
import eu.celarcloud.celar_ms.SocketPack.Publisher;

public class Distributor extends Thread implements IDistributor{
	
	private DistributorStatus distributorStatus;
	private boolean firstFlag;
	
	private Publisher publisher;
	private LinkedBlockingQueue<String> msgQueue;

	public Distributor(String ipAddr, String port, String protocol, long hwm, LinkedBlockingQueue<String> queue){
		super("Distributor-Thread");
		this.publisher = new Publisher(ipAddr,port,protocol,hwm,IPublisher.ConnectType.CONNECT);

		this.distributorStatus = DistributorStatus.INACTIVE;
		this.firstFlag = true;
		
		this.msgQueue = queue;
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
			String msg;
			while(this.distributorStatus != DistributorStatus.DYING){
				if(this.distributorStatus == DistributorStatus.ACTIVE){
					try{
						//if no queue is attached to distributor NUllPointerException is thrown
						msg = this.msgQueue.poll(500, TimeUnit.MILLISECONDS);
						if (msg != null){
							this.publisher.send(msg);
						}
						else
							Thread.sleep(2000);
					}
					catch(NullPointerException e){
						if (this.msgQueue == null) System.out.println("No message queue attached to Distributor");
						Thread.sleep(3000);
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
			e.printStackTrace();
		}
		finally{
			this.publisher.close();
		}	
	}
}