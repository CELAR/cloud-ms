/*******************************************************************************
 * Copyright 2014, Laboratory of Internet Computing (LInC), Department of Computer Science, University of Cyprus
 * 
 * For any information relevant to JCatascopia Monitoring System,
 * please contact Demetris Trihinas, trihinas{at}cs.ucy.ac.cy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.celarcloud.jcatascopia.probepack;

import java.util.HashMap;

public class ProbeProperty {
	
	private int propertyID;
	private String propertyName;
	private String propertyUnits;
	private ProbePropertyType propertyType;
	private String propertyDesc;
	
	public ProbeProperty(int propID, String propName, ProbePropertyType propType, 
			             String propUnits, String propDesc){
		this.propertyID = propID;
		this.propertyName = propName;
		this.propertyType = propType;
		this.propertyUnits = propUnits; 
		this.propertyDesc = propDesc;
	}
	
	public int getPropertyID(){
		return this.propertyID;
	}
	
	public void setPropertyID(int propID){
		this.propertyID = propID;
	}

	public String getPropertyName(){
		return this.propertyName;
	}
	
	public void setPropertyName(String propName){
		this.propertyName = propName;
	}
	
	public String getPropertyUnits(){
		return this.propertyUnits;
	}
	
	public void setPropertyUnits(String propUnits){
		this.propertyUnits = propUnits;
	}
	
	public String getPropertyDescription(){
		return this.propertyDesc;
	}
	
	public void setPropertyDescription(String propDesc){
		this.propertyUnits = propDesc;
	}
	
	//it returns INTEGER, BOOLEAN, not 1 or 0
	public ProbePropertyType getPropertyType(){
		return this.propertyType;		
	}
	
	public void setPropertyType(ProbePropertyType p){
		this.propertyType = p;
	}
	
	public HashMap<String,String> getProbePropertyMetadata(){
		HashMap<String,String> meta = new HashMap<String,String>();
		meta.put("propertyID", Integer.toString(this.propertyID));
		meta.put("propertyName", this.propertyName);
		meta.put("propertyType", this.propertyType.toString());
		meta.put("propertyUnits", this.propertyUnits);
		meta.put("propertyDesc", this.propertyDesc);
		return meta;
	}
}
