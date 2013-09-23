package eu.celarcloud.celar_ms.ServerPack;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;

public class ControlListener extends Listener{
	
	private MonitoringServer server;
	
	public ControlListener(String ip,String port,String protocol,long hwm,MonitoringServer server) throws CatascopiaException {
		super(ListenerType.ROUTER, ip, port, protocol, hwm, 3000);
		
		this.server = server;
	}

	@Override
	public void listen(String[] msg){
		if (msg[1].contains("SUBSCRIPTION"))
			this.server.controlExecutor.process(new SubProcessor(msg,this.getListener(),this.server));		
		else if (msg[1].contains("AGENT"))
			this.server.controlExecutor.process(new AgentRegister(msg,this.getListener(),this.server));		
	}

}
