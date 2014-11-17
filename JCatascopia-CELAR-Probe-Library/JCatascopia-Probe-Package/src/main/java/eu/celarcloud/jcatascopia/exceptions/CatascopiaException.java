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
package eu.celarcloud.jcatascopia.exceptions;

public class CatascopiaException extends Exception{
	private static final long serialVersionUID = 1L;
	
	public enum ExceptionType {ARGUMENT,ATTRIBUTE,KEY,TYPE,QUEUE,PROBE_EXISTANCE,NETWORKING,PACKAGING,FILE_ERROR,SUBCRIPTION,CONNECTION,DATABASE,AGGREGATOR}; 

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
