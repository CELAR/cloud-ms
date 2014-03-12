package eu.celarcloud.celar_ms.AgentPack.aggregators;

public interface IAggregator {
	
	public void add(String metric);
	public String toMessage();
	public void clear();
	public int length();
}
