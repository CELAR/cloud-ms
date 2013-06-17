package eu.celarcloud.celar_ms.ProbePack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import eu.celarcloud.celar_ms.ProbePack.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.utils.Timestamp;


public interface IProbe {
	
	public enum ProbeStatus{INACTIVE,ACTIVE,DYING};
	
	public String getProbeID();
	
	public String getProbeName();
	
	public void setProbeName(String name);
	
	public int getCollectFreq();
	
	public void setCollectFreq(int freq);
	
	public ProbeStatus getProbeStatus();
	
	public void writeToProbeLog(Level level, String msg);

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
	
	public Timestamp getLastUpdateTime();
	
	
	//user must override these
	public abstract String getDescription();
	
	public abstract ProbeMetric collect();
	//
	
	//user optionally can override 
	public void cleanUp();
	//
	
	public void attachQueue(LinkedBlockingQueue<ProbeMetric> queue);
	
	public void removeQueue();
}
