package eu.celarcloud.celar_ms.ServerPack.Beans;

public class MetricObj {
	private String metricID;
	private String agentID;
	private String subID;
	private String name;
	private String units;
	private String type;
	private String group;
	private String value;
	
	public MetricObj(String metricID,String agentID,String subID,String name,String units,
			         String type,String group,String value){
		this.metricID = metricID;
		this.agentID = agentID;
		this.subID = subID;
		
		this.name = name;
		this.units = units;
		this.type = type;
		this.group = group;
		this.value = value;
	}
	
	public MetricObj(String metricID,String agentID,String subID,String name,String units,
			         String type, String group){
		this(metricID, agentID, subID, name, units, type, group, null);
	}
	
	public String getMetricID(){
		return this.metricID;
	}
	
	public String getAgentID(){
		return this.agentID;
	}
	
	public String getSubID(){
		return this.subID;
	}

	public String getName(){
		return this.name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getGroup(){
		return this.group;
	}
	
	public void setGroup(String group){
		this.group = group;
	}
	
	public String getUnits(){
		return this.units;
	}
	
	public void setUnits(String units){
		this.units = units;
	}
	
	public String getType(){
		return this.type;
	}
	
	public void setType(String type){
		this.type = type;
	}
		
	public String getValue(){
		return this.value;
	}
	
	public void setValue(String value){
		this.value = value;
	}
	
	public void setAll(String metricID,String agentID,String name,String units,String type,
	         String group,String value){
		this.metricID = metricID;
		this.agentID = agentID;
		
		this.name = name;
		this.units = units;
		this.type = type;
		this.group = group;
		this.value = value;	
	}
	
	public String toString(){
		String str = "name: "+this.name+" type: "+this.type+" units: "+this.units+" group: "+this.group;
		return (this.value != null) ? str+" current_value: "+this.value : str;
	}
}