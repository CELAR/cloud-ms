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
package eu.celarcloud.celar_ms.ServerPack.subsciptionPack;

import java.util.ArrayList;
import java.util.TimerTask;

import eu.celarcloud.celar_ms.ServerPack.MonitoringServer;
import eu.celarcloud.celar_ms.ServerPack.Beans.MetricObj;
import eu.celarcloud.celar_ms.ServerPack.Beans.SubObj;

public class SubTask extends TimerTask {
	
	private String subID;
	private MonitoringServer server;
	private String type;
		
	public SubTask(MonitoringServer server, String subID){
		this.subID = subID;
		this.server = server;
	}
	    
	@Override
	public void run() {
		try{
		SubObj sub = server.getSubMap().get(subID);
		//check if subscription removed to wrap it up and stop updating
		if(sub != null){
			MetricObj mobj = this.server.getMetricMap().get(sub.getMetricID());
			this.type = mobj.getType(); //INTEGER, DOUBLE, etc.
			
			//grab the latest values reported by interested agents for originMetric
			ArrayList<String> values = new ArrayList<String>();
			MetricObj m = null;
			for(String a : sub.getAgentList()){
				m = server.getMetricMap().get(a+":"+sub.getOriginMetric());
				if (m != null)
					values.add(m.getValue());
			}
			
			String result = "";
			switch(sub.getGroupingFunc()){
				case SUM: if (type.equals("INTEGER")) result = String.valueOf(Functions.sumInt(values));
						  else if (type.equals("LONG")) result = String.valueOf(Functions.sumLong(values));
						  else if (type.equals("DOUBLE")) result = String.valueOf(Functions.sumDouble(values));
						  break;
				case AVG: result = String.valueOf(Functions.avgDouble(values));
				          break;
				case MAX: if (type.equals("INTEGER")) result = String.valueOf(Functions.maxInt(values));
				  		  else if (type.equals("LONG")) result = String.valueOf(Functions.maxLong(values));
				          else if (type.equals("DOUBLE")) result = String.valueOf(Functions.maxDouble(values));
				          break;
				case MIN: if (type.equals("INTEGER")) result = String.valueOf(Functions.minInt(values));
					      else if (type.equals("LONG")) result = String.valueOf(Functions.minLong(values));
		                  else if (type.equals("DOUBLE")) result = String.valueOf(Functions.minDouble(values));
		                  break;
				default: 
					; // should never get here since we use enum
			}
			mobj.setValue(result);
			mobj.setTimestamp(System.currentTimeMillis());
			if (server.getDatabaseFlag())
				//MetricDAO.insertValue(server.dbHandler.getConnection(), sub.getMetricID(), System.currentTimeMillis(), result);
				this.server.dbHandler.insertMetricValue(mobj);
		}
		else
			this.cancel(); //subscription was deleted. Remove this TimerTask
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
