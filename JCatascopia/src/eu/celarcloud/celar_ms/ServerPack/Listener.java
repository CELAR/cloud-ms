package eu.celarcloud.celar_ms.ServerPack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.SocketPack.ISubscriber;
import eu.celarcloud.celar_ms.SocketPack.Subscriber;

public abstract class Listener extends Thread implements IListener{
	//path to config file
	private static final String CONFIG_PATH = "resources"+File.separator+"server.properties";
	/**
	 * properties read from config file
	 */
	private Properties config;
	
	//the objects that does the magic. It receives monitoring messages from the application VMs
	private Subscriber subscriber;
	private boolean firstFlag;
	private ListenerStatus listenerStatus;
	
	public Listener(String ipAddr, String port, String protocol, long hwm) throws CatascopiaException{
		super("Listener-Thread");
		
		this.subscriber = new Subscriber(ipAddr,port,protocol,hwm,ISubscriber.ConnectType.BIND);
		this.listenerStatus = ListenerStatus.INACTIVE; //start as INACTIVE and wait to be ACTIVATED
		this.firstFlag = true;	
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
		this.subscriber.close();
		this.listenerStatus = ListenerStatus.DYING;
		this.notify();
	}
	
	@Override
	public void run(){
		try{
			String msg;
			while(this.listenerStatus != ListenerStatus.DYING){
				if(this.listenerStatus == ListenerStatus.ACTIVE){
					//the subscriber blocks until it receives a new message
					msg = subscriber.receiveNonBlocking();
					//process the message depending on client implemented
					if (msg != null)
						this.processMessage(msg);
					else
						Thread.sleep(1000);
				}
				else 
					synchronized(this){
						while(this.listenerStatus == ListenerStatus.INACTIVE)
							this.wait();
					}
			}
		}
		catch (InterruptedException e){
			e.printStackTrace();
		}
		finally{
			this.subscriber.close();
		}
	}	
	
	public abstract void processMessage(String msg);	
}
