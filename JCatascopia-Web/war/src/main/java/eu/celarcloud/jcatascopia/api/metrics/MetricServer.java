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
package eu.celarcloud.jcatascopia.api.metrics;

import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.celarcloud.jcatascopia.web.queryMaster.beans.MetricObj;
import eu.celarcloud.jcatascopia.web.queryMaster.database.IDBInterface;

@Path("/")
//from web.xml path until here is: /restAPI/metrics/
public class MetricServer {

	/**
	 * Returns a list of the latest values for the given metric ids.
	 * 
	 * @param req
	 * @param response
	 * @param context
	 * @param body A string containing the metric ids separated by comma
	 * @return
	 */
	@POST
	@Path("/")
//	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRegisteredMetrics(@Context HttpServletRequest req, @Context HttpServletResponse response,
			                              @Context ServletContext context, String body){
		long t1 = System.currentTimeMillis();
		IDBInterface dbInterface = (IDBInterface) context.getAttribute("dbInterface");

		if (body.startsWith("metrics=")) 
			body = body.split("=")[1].replaceAll("%3A", ":");
		
		ArrayList<MetricObj> metriclist = dbInterface.getMetricValues(body.split(","));
		StringBuilder sb = new StringBuilder();
		sb.append("{\"metrics\":[");
		if(metriclist!=null) {
			boolean first = true;
			for(MetricObj mo: metriclist) {
				if(!first) sb.append(",");
				sb.append(mo.toJSON());
				first = false;
			}
		}
		sb.append("]}");
		
		if(context.getAttribute("debug_mode") != null && context.getAttribute("debug_mode").toString().equals("true")){
			System.out.println("Listing registered metrics");
			System.out.println(sb.toString());
			System.out.println("query time (ms): " + (System.currentTimeMillis()-t1));
		}

		return Response.status(Response.Status.OK)
				.entity(sb.toString())
				.build();	
	}

	/**
	 * Returns the available metrics for a new subscription.
	 * 
	 * @param req
	 * @param response
	 * @param context
	 * @return a list of all the available metrics for a new subscription
	 */
	@GET
	@Path("/subscriptions")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSubscriptionsMetrics(@Context HttpServletRequest req, @Context HttpServletResponse response,
			                                 @Context ServletContext context){
		IDBInterface dbInterface = (IDBInterface) context.getAttribute("dbInterface");
		
		ArrayList<MetricObj> metriclist = dbInterface.getAvailableMetricsForSubs();
		StringBuilder sb = new StringBuilder();
		sb.append("{\"metrics\":[");
		if(metriclist!=null) {
			boolean first = true;
			for(MetricObj mo: metriclist) {
				if(!first) sb.append(",");
				sb.append(mo.toJSON());
				first = false;
			}
		}
		sb.append("]}");
		
		if(context.getAttribute("debug_mode") != null && context.getAttribute("debug_mode").toString().equals("true")){
			System.out.println("Listing available metrics to subscribe to");
			System.out.println(sb.toString());
		}
		
		return Response.status(Response.Status.OK)
				.entity(sb.toString())
				.build();
	}

	/**
	 * Returns the values of a given metric for the specified timerange or interval.
	 * 
	 * @param req
	 * @param response
	 * @param context
	 * @param metricID The metric id
	 * @param interval The interval
	 * @param start The time to start from
	 * @param end The time to end to
	 * @return a list of all the values between this timerange
	 * 
	 * timestamps should be given in UNIX TIME
	 */
	@GET
	@Path("/{metricid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getValuesForTimeRange(@Context HttpServletRequest req, @Context HttpServletResponse response,
										   @Context ServletContext context, @PathParam("metricid") String metricID,
										   @QueryParam("interval") String interval, @QueryParam("tstart") String tstart, 
										   @QueryParam("tend") String tend){
		long t1 = System.currentTimeMillis();
		IDBInterface dbInterface = (IDBInterface) context.getAttribute("dbInterface");

		ArrayList<MetricObj> metriclist = null;
		if(tstart == null && tend == null && interval == null){
			String[] regMetrics = new String[1];
			regMetrics[0] = metricID;
			metriclist = dbInterface.getMetricValues(regMetrics);
		}
		else if(tstart == null && tend == null){
			long interv = Long.parseLong(interval);
			metriclist = dbInterface.getMetricValuesByTime(metricID, interv);
		}
		else{
			long start = Long.parseLong(tstart);
			long end = Long.parseLong(tend);
			metriclist = dbInterface.getMetricValuesByTime(metricID, start, end);
		}

		StringBuilder sb = new StringBuilder();
		sb.append("{\"metricID\":\""+metricID+"\", \"values\":[");
		if(metriclist!=null) {
			boolean first = true;
			for(MetricObj mo: metriclist) {
				if(!first) sb.append(",");
				sb.append(mo.toJSON());
				first = false;
			}
		}
		sb.append("]}");
		
		if(context.getAttribute("debug_mode") != null && context.getAttribute("debug_mode").toString().equals("true")){
			System.out.println("Listing timerange values for metric " + metricID);
			System.out.println(sb.toString());
			System.out.println("query time (ms): " + (System.currentTimeMillis()-t1));
		}	
	

		return Response.status(Response.Status.OK)
					    .entity(sb.toString())
				        .build();
	}

	/**
	 * Returns the values of all the agent metrics for the specified timerange.
	 * 
	 * @param req
	 * @param response
	 * @param context
	 * @param agentID The agent's id
	 * @param start The time to start from
	 * @param end The time to end to
	 * @return an array of the lists of each metric's values
	 */
	@GET
	@Path("/agent/{agentid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAgentValuesForTimeRange(@Context HttpServletRequest req, 
			@Context HttpServletResponse response,
			@Context ServletContext context,
			@PathParam("agentid") String agentID,
			@QueryParam("tstart") String start, @QueryParam("tend") String end) 
	{
//		DBHandlerWithConnPool dbHandler = (DBHandlerWithConnPool) context.getAttribute("dbHandler");
//		
//		JSONArray jresponse = MetricDAO.getAgentsLatestMetrics(dbHandler.getConnection(), new String[]{agentID});
//
//		try {
//			return Response.status(Response.Status.OK)
//							.entity(jresponse.get(0).toString())
//							.build();
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return Response.status(Response.Status.NOT_FOUND)
//				.entity("")
//				.build();
		return Response.status(Response.Status.OK).build();
	}
	
	/**
	 * Returns the values of all the agent metrics in this deployment for the specified timerange.
	 * 
	 * @param req
	 * @param response
	 * @param context
	 * @param deploymentID The deployment's id
	 * @param start The time to start from
	 * @param end The time to end to
	 * @return an array of arrays of the lists of each metric's values for each agent
	 */
	@GET
	@Path("/deployment/{deploymentid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDeploymentAgentsValuesForTimeRange(	@Context HttpServletRequest req, 
															@Context HttpServletResponse response,
															@Context ServletContext context,
															@PathParam("deploymentid") String deploymentID,
															@QueryParam("tstart") String start, @QueryParam("tend") String end) 
	{
//		DBHandlerWithConnPool dbHandler = (DBHandlerWithConnPool) context.getAttribute("dbHandler");
//		
//		ArrayList<AgentObj> agents = AgentDAO.getAgents(dbHandler.getConnection(), null);
//		if(agents != null) {
//			StringBuilder sb = new StringBuilder();
//			boolean first = true;
//			for(AgentObj ao: agents) {
//				if(!first) sb.append(",");
//				sb.append(ao.getAgentID());
//				first = false;
//			}
//			JSONArray jresponse = MetricDAO.getAgentsLatestMetrics(dbHandler.getConnection(),sb.toString().split(","));
//	
//			return Response.status(Response.Status.OK)
//							.entity(jresponse.toString())
//							.build();
//		}
//		return Response.status(Response.Status.NOT_FOUND)
//				.entity("")
//				.build();
		return Response.status(Response.Status.OK).build();
	}
}
