package eu.celarcloud.jcatascopia.web.queryMaster.beans;
/**
 * This represents a metric object. It includes the metricID,
 * name, units, type, group, value and timestamp.
 *
 */
public class MetricObj {
	private String metricID;
	private String name;
	private String units;
	private String type;
	private String group;
	private String value;
	private String timestamp;
	
	public MetricObj(String metricID,String name,String units,String type,String group,String value, String timestamp){
		this.metricID = metricID;	
		this.name = name;
		this.units = units;
		this.type = type;
		this.group = group;
		this.value = value;
		this.timestamp = timestamp;
	}
	
	public MetricObj(String metricID,String name,String units,String type, String group){
		this(metricID, name, units, type, group, null, null);
	}
	
	public String getMetricID(){
		return this.metricID;
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
	
	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String toString(){
		String str = "name: "+this.name+" type: "+this.type+" units: "+this.units+" group: "+this.group;
		return (this.value != null) ? str+" current_value: "+this.value : str;
	}
	
	public String toJSON() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean previous = false;
		if(this.metricID!=null) {
			sb.append("\"metricID\":\""+this.metricID+"\"");
			previous = true;
		}
		if(this.name!=null) {
			sb.append((previous?",":"") + "\"name\":\""+this.name+"\"");
			previous = true;
		}
		if(this.units!=null) {
			sb.append((previous?",":"") + "\"units\":\""+this.units+"\"");
			previous = true;
		}
		if(this.type!=null) {
			sb.append((previous?",":"") + "\"type\":\""+this.type+"\"");
			previous = true;
		}
		if(this.group!=null) {
			sb.append((previous?",":"") + "\"group\":\""+this.group+"\""); 
			previous = true;
		}
		if(this.value!=null) {
			sb.append((previous?",":"") + "\"value\":\""+this.value+"\"");	
			previous = true;
		}
		if(this.timestamp!=null) {
//			sb.append((previous?",":"") + "\"timestamp\":\""+this.timestamp.split(" ")[1].replace(".0", "")+"\"");
			sb.append((previous?",":"") + "\"timestamp\":\""+this.timestamp+"\"");	
			previous = true;
		}
		sb.append("}");
		
		return sb.toString();
	}
}