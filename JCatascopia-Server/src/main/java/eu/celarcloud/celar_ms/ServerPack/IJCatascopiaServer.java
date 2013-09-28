package eu.celarcloud.celar_ms.ServerPack;

import java.util.logging.Level;

public interface IJCatascopiaServer {
	public String getServerID();
	public String getServerIP();
	public void writeToLog(Level level, Object msg);
	public boolean inDebugMode();
	public boolean inRedistributeMode();
}
