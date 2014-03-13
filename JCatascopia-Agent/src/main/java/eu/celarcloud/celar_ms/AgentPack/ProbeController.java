package eu.celarcloud.celar_ms.AgentPack;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


import eu.celarcloud.celar_ms.SocketPack.Router;

public class ProbeController extends Thread{
	/*
	 * INACTIVE - running but receiving messages is paused
	 * ACTIVE   - running and receiving messages
	 * DYING    - in the process of terminating
	 */
	public enum ListenerStatus{INACTIVE,ACTIVE,DYING};
	private Router router;
	private boolean firstFlag;
	private ListenerStatus listenerStatus;
	private LinkedBlockingQueue<String> metricQueue;
	private IJCatascopiaAgent agent;
	
	public ProbeController(String ip, String port, LinkedBlockingQueue<String> metricQueue, IJCatascopiaAgent agent){
		this.router = new Router(ip ,port);
		
		this.listenerStatus = ListenerStatus.INACTIVE; //start as INACTIVE and wait to be ACTIVATED
		this.firstFlag = true;	
		this.metricQueue = metricQueue;
		this.agent =agent;
	}
	
	@Override
	public void start(){
		this.activate();
	}
	
	public synchronized void activate(){
		if (this.listenerStatus == ListenerStatus.INACTIVE){
			if (this.firstFlag){
				super.start();
				this.firstFlag = false;
			}
			else this.notify();
			this.listenerStatus = ListenerStatus.ACTIVE;	
		}	
	}
	
	public void deactivate(){
		this.listenerStatus = ListenerStatus.INACTIVE;
	}
	
	public synchronized void terminate(){
		this.router.close();
		this.listenerStatus = ListenerStatus.DYING;
		this.notify();
	}
	
	@Override
	public void run(){
		try{
			String[] msg;
			while(this.listenerStatus != ListenerStatus.DYING){
				if(this.listenerStatus == ListenerStatus.ACTIVE){
					msg = router.receiveNonBlocking(); //router does not block
					if (msg != null){ //process incoming request
						try{
							if (msg[1].equals("XPROBE.METRIC")) //request is a metric from XProbe
								processXProbe(msg);
			     			
						} 
						catch(Exception e){
							this.agent.writeToLog(Level.SEVERE, e);
							Thread.sleep(5000);
							continue;
						}
			        }
					else
						Thread.sleep(3000);
				}
				else 
					synchronized(this){
						while(this.listenerStatus == ListenerStatus.INACTIVE)
							this.wait();
					}
			}
		}
		catch (InterruptedException e){
			this.agent.writeToLog(Level.SEVERE, e);
		} 
		finally{
			this.router.close();
		}
	}	
	
	private void processXProbe(String[] msg) throws InterruptedException{
		this.router.send(msg[0],msg[1],"{\"status\":\"OK\"}");
		this.metricQueue.offer(msg[2], 500, TimeUnit.MILLISECONDS); //offer the content
		if(this.agent.inDebugMode())
			System.out.println("Probe Controller>> Received metric from XProbe and enqueued it to metric queue...\n"+msg[2]);
	}
}
