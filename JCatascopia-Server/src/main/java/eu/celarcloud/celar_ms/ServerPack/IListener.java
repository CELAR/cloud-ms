package eu.celarcloud.celar_ms.ServerPack;

public interface IListener {
	/*
	 * INACTIVE - running but receiving messages is paused
	 * ACTIVE   - running and receiving messages
	 * DYING    - in the process of terminating
	 */
	public enum ListenerStatus{INACTIVE,ACTIVE,DYING};
	
	public enum ListenerType{SUBSCRIBER,ROUTER};
}
