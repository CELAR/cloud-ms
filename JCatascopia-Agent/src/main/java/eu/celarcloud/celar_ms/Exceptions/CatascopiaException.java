package eu.celarcloud.celar_ms.Exceptions;

public class CatascopiaException extends Exception{
	private static final long serialVersionUID = 1L;
	
	public enum ExceptionType {ARGUMENT,ATTRIBUTE,KEY,TYPE,QUEUE,PROBE_EXISTANCE,NETWORKING,PACKAGING,FILE_ERROR,SUBCRIPTION,CONNECTION,DATABASE}; 

	private String message = null;
	private ExceptionType extype;
	 
	public CatascopiaException() {
		super();
	}
	 
	public CatascopiaException(String message, ExceptionType type) {
		super(message);
	    this.message = type+" Exception: " + message;
	    this.extype = type;
	}
	 
	public CatascopiaException(Throwable cause) {
		super(cause);
	}
	
	public ExceptionType getExceptionType(){
		return this.extype;
	}
	 
	@Override
	public String toString() {
		return message;
	}
	 
	@Override
	public String getMessage() {
		return message;
	}
}