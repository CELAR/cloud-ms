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
package eu.celarcloud.jcatascopia.api.subscriptions;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import eu.celarcloud.jcatascopia.web.queryMaster.beans.AgentObj;
import eu.celarcloud.jcatascopia.web.queryMaster.beans.SubscriptionObj;
import eu.celarcloud.jcatascopia.web.queryMaster.database.IDBInterface;


@Path("/")
//from web.xml path until here is: /restAPI/subscriptions/
public class SubscriptionsServer {
	
	/**
	 * Returns a list of all the subscriptions
	 * 
	 * @param req
	 * @param response
	 * @param context
	 * @return a list of all the subscriptions
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAvailableSubscriptions(@Context HttpServletRequest req, @Context HttpServletResponse response,
							                   @Context ServletContext context){
		IDBInterface dbInterface = (IDBInterface) context.getAttribute("dbInterface");
		ArrayList<SubscriptionObj> sublist = dbInterface.getSubscriptions();
		
		StringBuilder sb = new StringBuilder();
		sb.append("{\"subs\":[");
		if(sublist != null) {
			boolean first = true;
			for(SubscriptionObj s: sublist) {
				if(!first) sb.append(",");
				sb.append(s.toJSON());
				first = false;
			}
		}
		sb.append("]}");
		
		if(context.getAttribute("debug_mode") != null && context.getAttribute("debug_mode").toString().equals("true")){
			System.out.println("Listing available subscriptions");
			System.out.println(sb.toString());
		}	
		
		return Response.status(Response.Status.OK)
		           .entity(sb.toString())
		           .build();
	}
	
	/**
	 * Returns the given subscription's metadata
	 * 
	 * @param req
	 * @param response
	 * @param context
	 * @param subID The subscription's id
	 * @return a json containing all the subscription's metadata
	 */
	@GET
	@Path("/{subid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSubscriptionMetadata(@Context HttpServletRequest req, @Context HttpServletResponse response,
							              @Context ServletContext context, @PathParam("subid") String subID){
		IDBInterface dbInterface = (IDBInterface) context.getAttribute("dbInterface");
		SubscriptionObj sub = dbInterface.getSubMeta(subID);
		
		if(context.getAttribute("debug_mode") != null && context.getAttribute("debug_mode").toString().equals("true")){
			System.out.println("Request to retieve sub metadata: " + sub.getSubID());
			System.out.println(sub.toJSON());
		}
		
		return Response.status(sub != null ? Response.Status.OK : Response.Status.NOT_FOUND)
		           .entity(sub!=null ? sub.toJSON() : "Subscripiton with id "+subID+" not found!")
		           .build();
	}
	
	/**
	 * Returns a list of all the agents invoked in this subscription.
	 * 
	 * @param req
	 * @param response
	 * @param context
	 * @param subID The subscription's id
	 * @return a list of all the agents in this subscription
	 */
	@GET
	@Path("/{subid}/agents")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSubscriptionAgents(@Context HttpServletRequest req, @Context HttpServletResponse response,
							              @Context ServletContext context, @PathParam("subid") String subID){
		IDBInterface dbInterface = (IDBInterface) context.getAttribute("dbInterface");
		ArrayList<AgentObj> subAgents = dbInterface.getAgentsForSub(subID);
		
		StringBuilder sb = new StringBuilder();
		sb.append("{\"agents\":[");
		if(subAgents != null) {
			boolean first = true;
			for(AgentObj a: subAgents) {
				if(!first) sb.append(",");
				sb.append(a.toJSON());
				first = false;
			}
		}
		sb.append("]}");
		
		if(context.getAttribute("debug_mode") != null && context.getAttribute("debug_mode").toString().equals("true")){
			System.out.println("Request to retrieve sub: "+subID+" agents");
			System.out.println(sb.toString());
		}
		
		return Response.status(Response.Status.OK)
		           .entity(sb.toString())
		           .build();
	}

	/**
	 * Creates a new subscription.
	 * 
	 * @param req
	 * @param response
	 * @param context
	 * @param body A body containing all the required data for creating a new subscription
	 * @return a message with the status of creating the new subscription
	 */
	@PUT
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addSubscription(@Context HttpServletRequest req, @Context HttpServletResponse response,
			              			 @Context ServletContext context, String body){
		String serverIP = (String) context.getAttribute("serverIP");
		String serverPort = (String) context.getAttribute("serverPort");
		IDBInterface dbInterface = (IDBInterface) context.getAttribute("dbInterface");

		try {
			if(body.startsWith("subscription="))
				body = URLDecoder.decode(body.split("=")[1], "UTF-8");	
			
			JSONObject json = new JSONObject(body);
			json.put("subID", UUID.randomUUID().toString().replace("-", ""));
			
			boolean resp = dbInterface.createSubscription(serverIP, serverPort, json.toString());
			if (resp){
				if(context.getAttribute("debug_mode") != null && context.getAttribute("debug_mode").toString().equals("true"))
					System.out.println("Added a new subscription with id " + json.getString("subID"));
				return Response.status(Response.Status.CREATED)
					           .entity("{\"status\":\"added\",\"subID\":\""+json.getString("subID")+"\"}")
					           .build();
			}
			else{
				System.out.println("Failed to add new subscription");
				//TODO fix this status code which gives the wrong impression
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					           .entity("{\"status\":\"failed\"}")
					           .build();
			}
		} 
		catch (JSONException e){
			e.printStackTrace();	
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Deletes the given subscription.
	 * 
	 * @param req
	 * @param response
	 * @param context
	 * @param subID The subscription's id to delete
	 * @return a status message
	 */
	@DELETE
	@Path("/{subid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteSubscription(@Context HttpServletRequest req, @Context HttpServletResponse response,
			              				@Context ServletContext context, @PathParam("subid") String subID){
		String serverIP = (String) context.getAttribute("serverIP");
		String serverPort = (String) context.getAttribute("serverPort");
		IDBInterface dbInterface = (IDBInterface) context.getAttribute("dbInterface");
		
		boolean resp = dbInterface.removeSubscription(serverIP, serverPort, "{\"subID\" : \""+subID+"\"}");
		if (resp){
			if(context.getAttribute("debug_mode") != null && context.getAttribute("debug_mode").toString().equals("true"))
				System.out.println("Removed subscription with id " + subID);
			
			return Response.status(Response.Status.OK)
			           .entity("{\"status\":\"success\"}")
			           .build();
		}
		else{
			System.out.println("Failed to delete subscription with id " + subID);
			return Response.status(Response.Status.NOT_FOUND)
				           .entity("{\"status\":\"failed\"}")
				           .build();
		}
	}
	
	/**
	 * Adds/removes an agent to/from a subscription.
	 * 
	 * @param req
	 * @param response
	 * @param context
	 * @param subID The subscription's id
	 * @param agentID The agent's id
	 * @param action The action to perform (addAgent/removeAgent)
	 * @return
	 */
	@GET
	@Path("{subid}/agent/{agentid}")
	@QueryParam("action")
	@Produces(MediaType.APPLICATION_JSON)
	public Response actionForAgent(@Context HttpServletRequest req, @Context HttpServletResponse response,
			              			@Context ServletContext context, @PathParam("subid") String subID, 
			              			@PathParam("agentid") String agentID, @QueryParam("action") String action){
		String serverIP = (String) context.getAttribute("serverIP");
		String serverPort = (String) context.getAttribute("serverPort");
		IDBInterface dbInterface = (IDBInterface) context.getAttribute("dbInterface");
		
		boolean success = false;
		StringBuilder sb = new StringBuilder();
		sb.append("{\"subID\":\""+subID+"\",");
		sb.append("\"agentID\":\""+agentID+"\"}");
		
		if(action.equals("addAgent")) {
			success = dbInterface.addAgent(serverIP, serverPort, sb.toString());
		}
		else if(action.equals("removeAgent")) {
			success = dbInterface.removeAgent(serverIP, serverPort, sb.toString());
		}
		else {
			System.out.println("Action "+action+" is not valid");
			return Response.status(Response.Status.NOT_FOUND)
				           .entity("{\"status\":\"NOT_FOUND\"}")
				           .build();
		}
		System.out.println(action + " with id "+agentID+" to subscription " + subID);
		return Response.status(success ? Response.Status.OK : Response.Status.INTERNAL_SERVER_ERROR)
			           .entity("{\"status\":\"DONE\"}")
			           .build();
	}
}
