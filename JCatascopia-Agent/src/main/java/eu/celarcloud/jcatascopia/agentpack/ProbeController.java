package eu.celarcloud.jcatascopia.agentpack;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.celarcloud.jcatascopia.agentpack.sockets.Router;
import eu.celarcloud.jcatascopia.exceptions.CatascopiaException;

public class ProbeController extends Thread{

	public enum ListenerStatus{INACTIVE,ACTIVE,DYING};
	private Router router;
	private boolean firstFlag;
	private ListenerStatus listenerStatus;
	private LinkedBlockingQueue<String> metricQueue;
	private IJCatascopiaAgent agent;
	
	private static final Pattern containerPattern = Pattern.compile("container\":\"[^\\\"]+");
	private static final Pattern classPattern = Pattern.compile("class\":\"[^\\\"]+");

	
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
							else if (msg[1].equals("AGENT.RECONNECT"))
								processReconnect(msg);
							else if (msg[1].equals("AGENT.ADD_PROBE"))
								deployProbe(msg);
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
		
		//offer metric to queue
		this.metricQueue.offer(msg[2], 500, TimeUnit.MILLISECONDS); 
		if(this.agent.inDebugMode())
			System.out.println("Probe Controller>> Received metric from XProbe and enqueued it to metric queue...\n"+msg[2]);
	}
	
	private void processReconnect(String[] msg){
		this.router.send(msg[0],msg[1],"{\"status\":\"OK\"}");
		agent.writeToLog(Level.INFO,"Probe Controller>> Received request from Monitoring Server to RECONNECT");
		if(this.agent.inDebugMode())
			System.out.println("Probe Controller>> Received request from Monitoring Server to RECONNECT");
		
		//reconnect
		String serverIP = agent.getConfig().getProperty("server_ip", "127.0.0.1");
		String port = agent.getConfig().getProperty("control_port", "4245");
		if (!ServerConnector.connect(serverIP, port, agent.getAgentID(), agent.getAgentIP(), agent.getProbeMap()))
			agent.writeToLog(Level.SEVERE, "FAILED to RECONNECT to Monitoring Server at: "+serverIP);
		else agent.writeToLog(Level.INFO, "Successfuly RECONNECTED to Server at: "+serverIP);
	}
	
	private void deployProbe(String[] msg){
		agent.writeToLog(Level.INFO, "Probe Controller>> Received request to deploy new Probe");
		if(this.agent.inDebugMode())
			System.out.println("Probe Controller>> Received request to deploy new Probe");
		
		//deploy new Probe
		String probeClassContainer = "";
		String probeClass = "";
		Matcher m = containerPattern.matcher(msg[2]);
		if (m.find())
			probeClassContainer = m.group().split("\":\"")[1];
		
		m = classPattern.matcher(msg[2]);
		if (m.find())
			probeClass = m.group().split("\":\"")[1];
	
		try {
			agent.deployProbeAtRuntime(probeClassContainer, probeClass);
			this.router.send(msg[0],msg[1],"{\"status\":\"OK\"}");
		} 
		catch (CatascopiaException e){
			this.router.send(msg[0],msg[1],"{\"status\":\"FAILED\"}");
			this.agent.writeToLog(Level.SEVERE, "ProbeController>> Failed to deploy new Monitoring Probe, " + e);
		}
		catch (Exception e){
			this.router.send(msg[0],msg[1],"{\"status\":\"FAILED\"}");
			this.agent.writeToLog(Level.SEVERE, "ProbeController>> Failed to deploy new Monitoring Probe, " + e);
		}
	}
}
