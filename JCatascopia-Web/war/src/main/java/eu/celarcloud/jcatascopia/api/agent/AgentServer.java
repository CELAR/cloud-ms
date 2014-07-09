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
package eu.celarcloud.jcatascopia.api.agent;

import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.Produces;

import eu.celarcloud.jcatascopia.web.queryMaster.beans.AgentObj;
import eu.celarcloud.jcatascopia.web.queryMaster.beans.MetricObj;
import eu.celarcloud.jcatascopia.web.queryMaster.database.IDBInterface;

@Path("/")
//from web.xml path until here is: /restAPI/agents/
public class AgentServer {

	/**
	 * Returns all the agents of an application. Optionally, if status is given, it returns all the
	 * agents of an application with the given status.
	 * 
	 * @param req
	 * @param response
	 * @param context
	 * @param status The desired status (Optional)
	 * @param appID The application's id
	 * @return a list of all the agents invoked in the given application
	 */
	@GET
	@Path("/")
	@Produces({MediaType.APPLICATION_JSON})
	public Response getAgents(@Context HttpServletRequest req, @Context HttpServletResponse response,
			              	   @Context ServletContext context, @QueryParam("status") String status, 
			              	   @QueryParam("time") String time, @QueryParam("applicationID") String appID){
		
		IDBInterface dbInterface = (IDBInterface) context.getAttribute("dbInterface");
		
		ArrayList<AgentObj> agentlist;
		if (time == null)
			agentlist = dbInterface.getAgents(status);
		else
			agentlist = dbInterface.getAgentsWithTimestamps(status);
		
		StringBuilder sb = new StringBuilder();
		sb.append(("{\"agents\":["));
		if(agentlist != null) {
			boolean first = true;
			for(AgentObj agent: agentlist) {
				if(!first) sb.append(",");
				sb.append(agent.toJSON());
				first = false;
			}	
		}
		sb.append("]}");
		
		if(context.getAttribute("debug_mode") != null && context.getAttribute("debug_mode").toString().equals("true")){
			System.out.println("Listing all agents for appID = " + appID);
			System.out.println(sb.toString());
		}
	
		return Response.status(Response.Status.OK)
			           .entity(sb.toString())
			           .build();	
	}
	
	/**
	 * Returns the available metrics of an agent.
	 * 
	 * @param req
	 * @param response
	 * @param context
	 * @param agentID The agent's id
	 * @return a list of all the available metrics for the given agent
	 */
	@GET
	@Path("/{agentID}/availableMetrics")
	@Produces({MediaType.APPLICATION_JSON})
	public Response getMetrics(@Context HttpServletRequest req, @Context HttpServletResponse response,
			                    @Context ServletContext context, @PathParam("agentID") String agentID){
		
		IDBInterface dbInterface = (IDBInterface) context.getAttribute("dbInterface");
		
		ArrayList<MetricObj> metriclist = dbInterface.getAgentAvailableMetrics(agentID);
		StringBuilder sb = new StringBuilder();
		sb.append("{\"metrics\":[");
		boolean first = true;
		if(metriclist != null)
			for(MetricObj m: metriclist) {
				if(!first) sb.append(",");
				sb.append(m.toJSON());
				first = false;
			}
		sb.append("]}");
		
		if(context.getAttribute("debug_mode") != null && context.getAttribute("debug_mode").toString().equals("true")){
			System.out.println("Listing available metrics for agent " + agentID);
			System.out.println(sb.toString());
		}
		
		return Response.status(Response.Status.OK)
			           .entity(sb.toString())
			           .build();			
	}
	
	@GET
	@Path("/availableMetrics")
	@Produces({MediaType.APPLICATION_JSON})
	public Response getMetricsForAllAgents(@Context HttpServletRequest req, @Context HttpServletResponse response,
			              	                @Context ServletContext context){
		IDBInterface dbInterface = (IDBInterface) context.getAttribute("dbInterface");
    	    	
		ArrayList<MetricObj> metriclist = dbInterface.getAvailableMetricsForAllAgents();
		StringBuilder sb = new StringBuilder();
		sb.append("{\"metrics\":[");
		boolean first = true;
		if(metriclist != null)
			for(MetricObj m: metriclist) {
				if(!first) sb.append(",");
				sb.append(m.toJSON());
				first = false;
			}
		sb.append("]}");
		
	
		return Response.status(Response.Status.OK)
			           .entity(sb.toString())
			           .build();	
	}
}
