package eu.celarcloud.celar_ms.ServerPack.Beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AgentObj{
	
	public enum AgentStatus{UP,DOWN,DEAD};
	
	private String agentID;
	private String agentIP;
	private AgentStatus agentStatus;
	private byte attempts;
	private List<String> metricList;
	
	public AgentObj(String agentID,String agentIP){
		this.agentID = agentID;
		this.agentIP = agentIP;
		this.agentStatus = AgentStatus.UP;
		this.attempts = 0;
		this.metricList = Collections.synchronizedList(new ArrayList<String>());
	}
	
	public String getAgentID(){
		return this.agentID;
	}
	
	public String getAgentIP(){
		return this.agentIP;
	}
	
	public void setAgentIP(String ip){
		this.agentIP = ip;
	}
		
	public boolean isRunning(){
		return (this.agentStatus == AgentStatus.UP) ? true : false;
	}
	
	public AgentStatus getStatus(){
		return this.agentStatus;
	}
	
	public void setStatus(AgentStatus status){
		this.agentStatus = status;
	}
	
	public void incrementAttempts(){
		this.attempts++;
	}
	
	public void clearAttempts(){
		this.attempts = 0;
	}
	
	public byte getAttempts(){
		return this.attempts;
	}
	
	public List<String> getMetricList(){
		return this.metricList;
	}
	
	public void addMetricToList(String metric){
		synchronized(metricList){
			this.metricList.add(metric);
		}
	}
	
	public void removeMetricFromList(String metric){
		synchronized(metricList){
			for(int i=0;i<metricList.size();i++)
				if (metricList.get(i).equals(metric))
					metricList.remove(i);
		}
	}
	
	public void clearMetricList(){
		synchronized(metricList){
			metricList.clear();
		}
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		try{
			synchronized(metricList){
				for(String m : metricList)
					sb.append(m.split(":")[1]+", ");
			}
		}
		catch(Exception e){}
		return "Agent>> AgentID: "+this.agentID+" AgentIP: "+this.agentIP+" status: "+this.agentStatus+
			   " available_metrics: ["+sb.toString()+"]";
	}
}
