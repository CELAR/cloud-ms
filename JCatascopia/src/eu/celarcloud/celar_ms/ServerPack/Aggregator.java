package eu.celarcloud.celar_ms.ServerPack;

public class Aggregator{

	private StringBuffer aggregator;
	private IJCatascopiaServer server;
	
	public Aggregator(IJCatascopiaServer server){
		this.aggregator = new StringBuffer();
		this.server = server;
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
		this.aggregator.append("],\"agentID\":\""+this.server.getServerID()+"\"");
		this.aggregator.append(",\"agentIP\":\""+this.server.getServerIP()+"\"}");
		
		if (this.server.inDebugMode())
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
