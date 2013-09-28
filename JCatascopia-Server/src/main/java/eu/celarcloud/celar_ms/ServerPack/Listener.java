package eu.celarcloud.celar_ms.ServerPack;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.SocketPack.ISocket;
import eu.celarcloud.celar_ms.SocketPack.Router;
import eu.celarcloud.celar_ms.SocketPack.Subscriber;

public abstract class Listener extends Thread implements IListener{
	
	//the objects that does the magic. It receives monitoring messages from the application VMs
	private ISocket socket;
	private boolean firstFlag;
	private ListenerStatus listenerStatus;
	private long listen_period;
	
	public Listener(ListenerType type, String ipAddr, String port, String protocol, long hwm,long listen_period) throws CatascopiaException{
		super("Listener-Thread");
		
		if (type == ListenerType.ROUTER)
			this.socket = new Router(ipAddr,port,protocol,hwm);
		else 
			this.socket = new Subscriber(ipAddr,port,protocol,hwm,ISocket.ConnectType.BIND);
		
		this.listenerStatus = ListenerStatus.INACTIVE; //start as INACTIVE and wait to be ACTIVATED
		this.firstFlag = true;	
		this.listen_period = listen_period;
	}
	
	public Listener(String ipAddr, String port, String protocol, long hwm,long listen_period) throws CatascopiaException{
		this(ListenerType.SUBSCRIBER, ipAddr, port, protocol, hwm, listen_period);
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
		this.socket.close();
		this.listenerStatus = ListenerStatus.DYING;
		this.notify();
	}
	
	@Override
	public void run(){
		try{
			String[] msg;
			while(this.listenerStatus != ListenerStatus.DYING){
				if(this.listenerStatus == ListenerStatus.ACTIVE){
					//the subscriber blocks until it receives a new message
					msg = socket.receiveNonBlocking();
					//process the message depending on client implemented
					if (msg != null)
						this.listen(msg);
					else
						Thread.sleep(this.listen_period);
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
		catch (CatascopiaException e) {
			e.printStackTrace();
		}
		finally{
			this.socket.close();
		}
	}	
	
	public ISocket getListener(){
		return this.socket;
	}
	
	public abstract void listen(String[] msg);	
}
