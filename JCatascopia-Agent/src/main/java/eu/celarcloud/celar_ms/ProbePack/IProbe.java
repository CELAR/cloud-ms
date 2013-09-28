package eu.celarcloud.celar_ms.ProbePack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;


public interface IProbe {
	
	public enum ProbeStatus{INACTIVE,ACTIVE,DYING};
	
	public String getProbeID();
	
	public String getProbeName();
	
	public void setProbeName(String name);
	
	public int getCollectPeriod();
	
	public void setCollectPeriod(int freq);
	
	public ProbeStatus getProbeStatus();
	
	public void writeToProbeLog(Level level, Object msg);

	public void addProbeProperty(int propID,String propName,ProbePropertyType propType, String propUnits, String desc);
	
	public HashMap<Integer,ProbeProperty> getProbeProperties();
	
	public ArrayList<ProbeProperty> getProbePropertiesAsList();
	
	public ProbeProperty getProbePropertyByID(int propID)  throws CatascopiaException;
	
	public void activate();
	
	public void deactivate();
	
	public void terminate();
	
	public HashMap<String, String> getProbeMetadata();
	
	public ProbeMetric getLastMetric();
	
	public void setLastMetric(ProbeMetric metric);
	
	public HashMap<Integer,Object> getLastMetricValues();
	
	public long getLastUpdateTime();
	
	
	//user must override these
	public abstract String getDescription();
	
	public abstract ProbeMetric collect();
	//
	
	//user optionally can override 
	public void cleanUp();
	public void checkReceivedMetric(ProbeMetric metric) throws CatascopiaException;
	//
	
	public void attachLogger(Logger logger);

	public void attachQueue(LinkedBlockingQueue<String> queue);
	
	public void removeQueue();
	
	public boolean metricsPullable();
	
	public void setPullableFlag(boolean flag);
	
	public void pull();
}
