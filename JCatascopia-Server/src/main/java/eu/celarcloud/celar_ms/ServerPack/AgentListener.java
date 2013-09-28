package eu.celarcloud.celar_ms.ServerPack;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;

public class AgentListener extends Listener{

	private MonitoringServer server;
	
	public AgentListener(String ip,String port,String protocol,long hwm,MonitoringServer server) throws CatascopiaException {
		super(ip,port,protocol,hwm,1000L);
		this.server = server;
	}

	@Override
	public void listen(String[] msg){
		this.server.processExecutor.process(new MetricProcessor(server,msg[0]));
	}
}