package eu.celarcloud.celar_ms.utils;

import java.util.Date;

/**
 * 
 * @author Demetris Trihinas
 * Utility class for expressing timestamps. 
 * Time is expressed as UNIX time. Seconds from UNIX epoch
 *
 */
public class CatascopiaTimestamp {
	
	private long timestamp;
	
	public CatascopiaTimestamp(){
		//this.timestamp = System.currentTimeMillis()/1000L;
		this.timestamp = System.currentTimeMillis();
	}
	
	public CatascopiaTimestamp(long timestamp){
		this.timestamp = timestamp;
	}
	
	public long getTimestamp(){
		return this.timestamp;
	}
	
	public void setTimestamp(long t){
		this.timestamp = t;
	}
	
	public String getReadableTimestamp(){
		//Date d = new Date(this.timestamp*1000L);
		Date d = new Date(this.timestamp);
		return d.toString();
	}
	
	//for testing
	public static void main(String[] args){
		CatascopiaTimestamp t = new CatascopiaTimestamp();
		System.out.println(t.getTimestamp());
		System.out.println(t.getReadableTimestamp());
	}
}
