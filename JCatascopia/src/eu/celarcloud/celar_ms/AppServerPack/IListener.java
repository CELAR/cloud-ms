package eu.celarcloud.celar_ms.AppServerPack;

public interface IListener {
	/*
	 * INACTIVE - running but receiving messages is paused
	 * ACTIVE   - running and receiving messages
	 * DYING    - in the process of terminating
	 */
	public enum ListenerStatus{INACTIVE,ACTIVE,DYING};
}
