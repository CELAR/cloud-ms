package eu.celarcloud.jcatascopia.agentpack.client;

import java.util.UUID;

import eu.celarcloud.jcatascopia.agentpack.sockets.Dealer;

/**
 * Client interface to communicate with JCatascopia Monitoring Agents
 * 
 * @author Demetris Trihinas
 *
 */
public class JCatascopiaAgentClient {
	
	public JCatascopiaAgentClient(){
		
	}
	
	public boolean deployProbe(String agentIP, String port, String probeClassContainer, String probeClass){
		Dealer dealer = new Dealer(agentIP, port, UUID.randomUUID().toString().replace("-", ""));
		String msg = "{\"container\":\""+probeClassContainer+"\",\"class\":\""+probeClass+"\"}";
		dealer.send("","AGENT.ADD_PROBE",msg);
		String[] response = dealer.receive(10000L);
		dealer.close();
		if (response != null)
			if (response[1].contains("OK"))
				return true;
		return false;
	}
}
