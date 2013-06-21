package eu.celarcloud.celar_ms.AgentPack;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AgentAggregator {

	private LinkedBlockingQueue<String> msgQueue;
	
	public AgentAggregator(LinkedBlockingQueue<String> msgQueue){
		this.msgQueue = msgQueue;
	}
	
	public void process(String msg){
		try {
			this.msgQueue.offer(msg,1,TimeUnit.SECONDS);
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
