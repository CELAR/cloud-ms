package eu.celarcloud.jcatascopia.web.queryMaster.beans;
/**
 * Represents an agent. Stores the ID, IP and status of the agent
 */
public class AgentObj{
		
	private String agentID;
	private String agentIP;
	private String status;
	
	public AgentObj(String agentID,String agentIP, String status){
		this.agentID = agentID;
		this.agentIP = agentIP;
		this.status = status;
	}
	
	public AgentObj(){}
	
	public String getAgentID(){
		return this.agentID;
	}
	
	public String getAgentIP(){
		return this.agentIP;
	}
	
	public void setAgentIP(String ip){
		this.agentIP = ip;
	}
		
	public void setStatus(String status){
		this.status = status;
	}	
	
	public String getStatus(){
		return this.status;
	}
	
	public String toString(){
		return "Agent>> AgentID: "+this.agentID+" AgentIP: "+this.agentIP+" status: "+this.status;
	}
	
	public String toJSON() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		
		sb.append("\"agentID\":\""+this.agentID+"\"");
		if(this.agentIP != null && this.agentIP.length() > 0) 
			sb.append(",\"agentIP\":\""+this.agentIP+"\"");
		if(this.status != null && this.status.length() > 0)
			sb.append(",\"status\":\""+this.status+"\"");
		
		sb.append("}");
		return sb.toString();
	}
}
