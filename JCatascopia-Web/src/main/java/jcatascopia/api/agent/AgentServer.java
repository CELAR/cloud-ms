package jcatascopia.api.agent;

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

import dbPackage.DBHandler;
import dbPackage.beans.AgentObj;
import dbPackage.beans.MetricObj;
import dbPackage.dao.AgentDAO;
import dbPackage.dao.MetricDAO;

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
			              @QueryParam("applicationID") String appID){
		
		DBHandler dbHandler = (DBHandler) context.getAttribute("dbHandler");
		ArrayList<AgentObj> agentlist = AgentDAO.getAgents(dbHandler.getConnection(), status);	

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
		System.out.println("Listing all agents for appID = " + appID);
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
		
		DBHandler dbHandler = (DBHandler) context.getAttribute("dbHandler");
		ArrayList<MetricObj> metriclist = 
				               MetricDAO.getAvailableMetricsMetaData(dbHandler.getConnection(), new String[]{agentID});
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
		System.out.println("Listing available metrics for agent " + agentID);
		return Response.status(Response.Status.OK)
			           .entity(sb.toString())
			           .build();			
	}
}