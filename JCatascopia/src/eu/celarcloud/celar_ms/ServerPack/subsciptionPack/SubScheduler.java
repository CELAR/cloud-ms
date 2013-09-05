package eu.celarcloud.celar_ms.ServerPack.subsciptionPack;

import java.util.Timer;
import java.util.TimerTask;

public class SubScheduler {
	
	private Timer scheduler;
	
	public SubScheduler(){
		this.scheduler = new Timer();
	}
	
	public void scheduleTask(TimerTask task, long period){
		scheduler.schedule(task, period, period);
	}
}
