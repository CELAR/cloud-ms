package eu.celarcloud.celar_ms.AgentPack;

public class Aggregator{

	private StringBuffer aggregator;
	private String agentID;
	private String agentIP;
	private IJCatascopiaAgent agent;
	
	public Aggregator(String agentID, String agentIP, IJCatascopiaAgent agent){
		this.aggregator = new StringBuffer();
		this.agentID = agentID;
		this.agentIP = agentIP;
		this.agent = agent;
	}

	public void add(String metric){
		if(this.aggregator.length()>0)
			this.aggregator.append(","+metric);
		else
			this.aggregator.append("{\"events\":["+metric);
	}
	
	public String toMessage(){
		if(this.aggregator.length()==0)
			this.add("");
		this.aggregator.append("],\"agentID\":\""+this.agentID+"\"");
		this.aggregator.append(",\"agentIP\":\""+this.agentIP+"\"}");
		
		if (this.agent.inDebugMode())
			System.out.println("Aggregator>> Message Ready for Distribution...\n"+this.aggregator.toString());
		return this.aggregator.toString();
	}
	
	public void clear(){
		this.aggregator.setLength(0);
	}
	
	public int length(){
		return this.aggregator.length();
	}
}
