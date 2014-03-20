package eu.celarcloud.celar_ms.ServerPack;

import java.util.logging.Level;

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
	private IJCatascopiaServer server;
	
	public Listener(ListenerType type, String ip, String port, long listen_period, IJCatascopiaServer server) throws CatascopiaException{
		super("Listener-Thread");
		
		if (type == ListenerType.ROUTER)
			this.socket = new Router(ip,port);
		else 
			this.socket = new Subscriber(ip, port, ISocket.ConnectType.BIND);
		
		this.listenerStatus = ListenerStatus.INACTIVE; //start as INACTIVE and wait to be ACTIVATED
		this.firstFlag = true;	
		this.listen_period = listen_period;
		
		this.server = server;
	}
	
	public Listener(String ip, String port, long listen_period, IJCatascopiaServer server) throws CatascopiaException{
		this(ListenerType.SUBSCRIBER, ip, port, listen_period, server);
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
			String[] msg;
			while(this.listenerStatus != ListenerStatus.DYING){
				try{
					if(this.listenerStatus == ListenerStatus.ACTIVE){
						msg = socket.receiveNonBlocking();
						//process the message depending on Listener implementation
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
				catch (CatascopiaException e) {
					e.printStackTrace();
					server.writeToLog(Level.SEVERE, e);
					continue;
				}
				catch (Exception e){
					e.printStackTrace();
					server.writeToLog(Level.SEVERE, e);
					continue;
				} 
//				finally{
//					this.socket.close();
//				}
			}
	}	
	
	public ISocket getListener(){
		return this.socket;
	}
	
	public abstract void listen(String[] msg);	
}
