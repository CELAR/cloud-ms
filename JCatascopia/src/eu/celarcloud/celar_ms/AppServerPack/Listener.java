package eu.celarcloud.celar_ms.AppServerPack;

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
	
	public Listener() throws CatascopiaException{
		super("Listener-Thread");
		//parse config file
		this.parseConfig();
		this.subscriber = this.initSubscriber();
		this.listenerStatus = ListenerStatus.INACTIVE; //start as INACTIVE and wait to be ACTIVATED
		this.firstFlag = true;	
	}
	
	//parse the configuration file
	private void parseConfig() throws CatascopiaException{
		this.config = new Properties();
		//load config properties file
		try {
			FileInputStream fis = new FileInputStream(CONFIG_PATH);
			config.load(fis);
			if (fis != null)
	    		fis.close();
		} 
		catch (FileNotFoundException e){
			throw new CatascopiaException("config file not found", CatascopiaException.ExceptionType.FILE_ERROR);
		} 
		catch (IOException e){
			throw new CatascopiaException("config file parsing error", CatascopiaException.ExceptionType.FILE_ERROR);
		}
	}
	
	/**
	 * Initialize the Subscriber.
	 * Bind Subscriber to Application Server IP in order to receive incoming messages from
	 * various Monitoring Agents
	 */
	private Subscriber initSubscriber(){
		String port = this.config.getProperty("sub_port", "4242");
		String protocol = this.config.getProperty("sub_protocol","tcp");
    	String ip = this.config.getProperty("sub_ip","*");
    	String hwm = this.config.getProperty("sub_hwm","32");
		return new Subscriber(ip,port,protocol,Long.parseLong(hwm),ISubscriber.ConnectType.BIND);
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
