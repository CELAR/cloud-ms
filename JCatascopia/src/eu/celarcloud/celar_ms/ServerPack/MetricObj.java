package eu.celarcloud.celar_ms.ServerPack;

public class MetricObj {

	private String name;
	private String units;
	private String type;
	private String value;
	
	public MetricObj(String name,String units,String type,String value){
		this.name = name;
		this.units = units;
		this.type = type;
		this.value = value;
	}

	public String getName(){
		return this.name;
	}
	
	public void setName(String name){
		this.name = name;
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
}
