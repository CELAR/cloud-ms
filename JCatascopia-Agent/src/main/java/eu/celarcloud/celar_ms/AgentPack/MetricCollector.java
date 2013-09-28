package eu.celarcloud.celar_ms.AgentPack;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class MetricCollector extends Thread{
	
	public enum CollectorStatus{INACTIVE,ACTIVE,DYING};
	private CollectorStatus collectorStatus;
	private boolean firstFlag;
	private LinkedBlockingQueue<String> metricQueue;
	private Aggregator aggregator;
	private IJCatascopiaAgent agent;
	
	public MetricCollector(LinkedBlockingQueue<String> metricQueue,Aggregator aggregator,IJCatascopiaAgent agent){	
		super("MetricCollector-Thread");
		this.metricQueue = metricQueue;
		this.collectorStatus = CollectorStatus.INACTIVE;
		this.firstFlag = true;
		
		this.aggregator = aggregator;
		this.agent = agent;
	}
	
	@Override
	public void start(){
		this.activate();
	}
	
	public synchronized void activate(){
		if (this.collectorStatus == CollectorStatus.INACTIVE){
			if (this.firstFlag){
				super.start();
				this.firstFlag = false;
			}
			else this.notify();
			this.collectorStatus = CollectorStatus.ACTIVE;	
		}	
	}
	
	public synchronized void terminate(){
		this.collectorStatus = CollectorStatus.DYING;
		this.notify();
	}
	
	@Override
	public void run(){
		String metric;
		while(this.collectorStatus != CollectorStatus.DYING){//TODO -logging - if reading from queue fails we log error and continue;
			if(this.collectorStatus == CollectorStatus.ACTIVE){
				try {
					metric = this.metricQueue.poll(500, TimeUnit.MILLISECONDS);
					if (metric != null){
						if(this.agent.inDebugMode())
							System.out.println("Metric Collector>> DeQueued Metric...\n"+metric);
						this.aggregator.add(metric);
					}
					else
						Thread.sleep(2000);
				}
				catch(InterruptedException e){
					this.agent.writeToLog(Level.SEVERE, e);
					continue;
				}
				catch(Exception e){
					this.agent.writeToLog(Level.SEVERE, e);
				}
			}
			else 
				synchronized(this){
					while(this.collectorStatus == CollectorStatus.INACTIVE)
						try {
							this.wait();
						} catch (InterruptedException e) {
							this.agent.writeToLog(Level.SEVERE, e);
						}
				}
		}
	}
}