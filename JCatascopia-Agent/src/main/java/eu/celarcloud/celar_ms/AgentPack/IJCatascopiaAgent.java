package eu.celarcloud.celar_ms.AgentPack;

import java.util.HashMap;
import java.util.logging.Level;

import eu.celarcloud.celar_ms.ProbePack.IProbe;

public interface IJCatascopiaAgent {
	public String getAgentIP();
	public String getAgentID();
	public HashMap<String,IProbe> getProbeMap();
	public void writeToLog(Level level, Object msg);
	public boolean inDebugMode();
}
