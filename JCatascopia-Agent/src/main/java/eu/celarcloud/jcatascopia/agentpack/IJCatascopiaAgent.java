package eu.celarcloud.jcatascopia.agentpack;

import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

import eu.celarcloud.jcatascopia.exceptions.CatascopiaException;
import eu.celarcloud.jcatascopia.probepack.IProbe;

public interface IJCatascopiaAgent {
	public String getAgentIP();
	public String getAgentID();
	public HashMap<String,IProbe> getProbeMap();
	public void writeToLog(Level level, Object msg);
	public boolean inDebugMode();
	public Properties getConfig();
	public void deployProbeAtRuntime(String probeClassContainer, String probeClass) throws CatascopiaException;
}
