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
package eu.celarcloud.jcatascopia.web.queryMaster.beans;
/**
 * This represents a subscription. It includes the subID, name,
 * groupping function, period, origin metric, metricID, metric type,
 * metric units and group.
 *
 */
public class SubscriptionObj {
	private String subID;
	private String subName;
	private String func;
	private String period;
	private String originMetric;
	private String metricID;
	private String metricType;
	private String metricUnits;
	private String group;
	
	public SubscriptionObj(String subID, String subName, String func, String period, String originMetric,
								String metricID, String metricType, String metricUnits, String group) {
		this.subID = subID;
		this.subName = subName;
		this.func = func;
		this.period = period;
		this.originMetric = originMetric;
		this.metricID = metricID;
		this.metricType = metricType;
		this.metricUnits = metricUnits;
		this.group = group;
	}
	
	public SubscriptionObj(String subID, String subName, String func, String period) {
		this(subID, subName, func, period, null, null, null, null, null);
	}
	
	public String getSubID() {
		return subID;
	}
	
	public void setSubID(String subID) {
		this.subID = subID;
	}
	
	public String getSubName() {
		return subName;
	}

	public void setSubName(String subName) {
		this.subName = subName;
	}

	public String getFunc() {
		return func;
	}
	
	public void setFunc(String func) {
		this.func = func;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}
	
	public String getOriginMetric() {
		return originMetric;
	}

	public void setOriginMetric(String originMetric) {
		this.originMetric = originMetric;
	}

	public String getMetricID() {
		return metricID;
	}

	public void setMetricID(String metricID) {
		this.metricID = metricID;
	}

	public String getMetricType() {
		return metricType;
	}

	public void setMetricType(String metricType) {
		this.metricType = metricType;
	}

	public String getMetricUnits() {
		return metricUnits;
	}

	public void setMetricUnits(String metricUnits) {
		this.metricUnits = metricUnits;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	@Override
	public String toString() {
		return "subID: "+this.subID;
	}
	
	public String toJSON() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"subID\":\""+this.subID+"\"");
		if(this.subName != null && this.subName.length() > 0)
			sb.append(",\"subName\":\""+this.subName+"\"");
		if(this.func != null && this.func.length() > 0)
			sb.append(",\"func\":\""+this.func+"\"");
		if(this.period != null && this.period.length() > 0)
			sb.append(",\"period\":\""+this.period+"\"");
		if(this.originMetric != null && this.originMetric.length() > 0)
			sb.append(",\"originMetric\":\""+this.originMetric+"\"");
		if(this.metricID != null && this.metricID.length() > 0)
			sb.append(",\"metricID\":\""+this.metricID+"\"");
		if(this.metricType != null && this.metricType.length() > 0)
			sb.append(",\"metricType\":\""+this.metricType+"\"");
		if(this.metricUnits != null && this.metricUnits.length() > 0)
			sb.append(",\"metricUnits\":\""+this.metricUnits+"\"");
		if(this.group != null && this.group.length() > 0)
			sb.append(",\"group\":\""+this.group+"\"");
		sb.append("}");
		return sb.toString();
	}
}
