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
package eu.celarcloud.jcatascopia.probepack.filters;

/**
 * The SimpleFilter is used to filter values below/over a given threshold
 * 
 * @author Demetris Trihinas
 *
 */
public class SimpleFilter extends Filter{

	public enum SimpleFilterType {OVER, BELOW};
	
	private double threshold;
	private SimpleFilterType type;
	
	public SimpleFilter(double threshold, SimpleFilterType type){
		this.threshold = threshold;
		this.type = type;
	}
	
	public SimpleFilter(double threshold){
		this(threshold,SimpleFilterType.BELOW);
	}
	
	@Override
	public void adjustFilter(double curValue) {
		//ignore output of check() since we do not have a window here but a threshold
		
		if (this.type == SimpleFilterType.BELOW){
			if (curValue < this.threshold) //filter values below threshold
				this.checkFlag = true;
			else
				this.checkFlag = false;
		}
		else {
			if (curValue > this.threshold) //filter values over threshold
				this.checkFlag = true;
			else
				this.checkFlag = false;
		}
	}
}
