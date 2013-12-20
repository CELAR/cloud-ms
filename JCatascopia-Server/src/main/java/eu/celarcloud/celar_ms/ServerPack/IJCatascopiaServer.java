package eu.celarcloud.celar_ms.ServerPack;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import eu.celarcloud.celar_ms.ServerPack.Beans.AgentObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.MetricObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.SubObj;

public interface IJCatascopiaServer {
	public String getServerID();
	public String getServerIP();
	public void writeToLog(Level level, Object msg);
	public boolean inDebugMode();
	public boolean inRedistributeMode();
	
	public ConcurrentHashMap<String,AgentObj> getAgentMap();
	public ConcurrentHashMap<String,MetricObj> getMetricMap();
	public ConcurrentHashMap<String,SubObj> getSubMap();
}
