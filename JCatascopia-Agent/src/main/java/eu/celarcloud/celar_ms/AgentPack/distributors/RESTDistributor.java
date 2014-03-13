package eu.celarcloud.celar_ms.AgentPack.distributors;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;

public class RESTDistributor implements IDistributor{
	
	private String url;
	private Client client; 
	
	public RESTDistributor(String ip, String port, String url){
		this.url = url;
		this.client = Client.create();
	}

	public void send(String msg) throws CatascopiaException {
		WebResource webResource = client.resource(this.url);
		ClientResponse response = webResource.type("application/json").post(ClientResponse.class, msg);

//		System.out.println(response.getEntity(String.class));
	}

	public void terminate() {
		client.destroy();		
	}
}
