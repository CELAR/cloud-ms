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