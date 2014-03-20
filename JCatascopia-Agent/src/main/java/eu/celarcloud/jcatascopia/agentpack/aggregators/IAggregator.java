package eu.celarcloud.jcatascopia.agentpack.aggregators;

public interface IAggregator {
	
	public void add(String metric);
	public String toMessage();
	public void clear();
	public int length();
}
