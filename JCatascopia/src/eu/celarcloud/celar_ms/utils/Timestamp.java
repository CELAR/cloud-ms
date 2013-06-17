package eu.celarcloud.celar_ms.utils;

import java.util.Date;

/**
 * 
 * @author Demetris Trihinas
 * Utility class for expressing timestamps. 
 * Time is expressed as UNIX time. Seconds from UNIX epoch
 *
 */
public class Timestamp {
	
	private long timestamp;
	
	public Timestamp(){
		this.timestamp = System.currentTimeMillis()/1000L;
	}
	
	public Timestamp(long timestamp){
		this.timestamp = timestamp;
	}
	
	public long getTimestamp(){
		return this.timestamp;
	}
	
	public void setTimestamp(long t){
		this.timestamp = t;
	}
	
	public String getReadableTimestamp(){
		Date d = new Date(this.timestamp*1000);
		return d.toString();
	}
	
	//for testing
	public static void main(String[] args){
		Timestamp t = new Timestamp();
		System.out.println(t.getTimestamp());
		System.out.println(t.getReadableTimestamp());
	}
}
