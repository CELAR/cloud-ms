package eu.celarcloud.celar_ms.ServerPack;

import java.util.Map.Entry;

import eu.celarcloud.celar_ms.ServerPack.Beans.AgentObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.MetricObj;

public class Aggregator{

	private StringBuffer aggregator;
	private MonitoringServer server;
	
	public Aggregator(MonitoringServer server){
		this.aggregator = new StringBuffer();
		this.server = server;
	}

	public String createMessage(){
		this.aggregator.append("{\"serverID\":\""+server.getServerID()+"\",\"serverIP\":\""+server.getServerIP()+"\",\"agents\":[");
		AgentObj a;
		for (Entry<String,AgentObj> entry : server.agentMap.entrySet()){
			a = entry.getValue();
			this.aggregator.append("{");
			this.aggregator.append("\"agentID\":\""+a.getAgentID()+"\",");
			this.aggregator.append("\"agentIP\":\""+a.getAgentIP()+"\",");
			
			//add metrics
			this.aggregator.append("\"metrics\":[");
			MetricObj m = null;
			for (Entry<String,MetricObj> entry2 : server.metricMap.entrySet()){
				m = entry2.getValue();
				this.aggregator.append("{");
				this.aggregator.append("\"timestamp\":\""+m.getTimestamp()+"\",");
				this.aggregator.append("\"group\":\""+m.getGroup()+"\",");
				this.aggregator.append("\"name\":\""+m.getName()+"\",");
				this.aggregator.append("\"units\":\""+m.getUnits()+"\",");
				String t = m.getType();
				this.aggregator.append("\"type\":\""+t+"\",");
				if (t.equals("DOUBLE") || t.equals("INTEGER") || t.equals("LONG") || t.equals("FLOAT"))
					this.aggregator.append("\"val\":\""+m.getAvg()+"\"},");
				else
					this.aggregator.append("\"val\":\""+m.getValue()+"\"},");
			}
			if (m != null)
				this.aggregator.replace(this.aggregator.length()-1, this.aggregator.length(), "");
			this.aggregator.append("]},");
		}
		if (!server.agentMap.isEmpty())
			this.aggregator.replace(this.aggregator.length()-1, this.aggregator.length(), "");
		this.aggregator.append("]}");
		
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
