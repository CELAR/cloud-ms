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
 * The RangeFilter is used to simply filter values in the window [previousValue-Range,previousValue+Range]
 * The RangeFilteronly requires from users to define upon instantiation, the range of their interest.
 * 
 * @author Demetris Trihinas
 *
 */
public class RangeFilter extends Filter{
	
	private double range;
	
	public RangeFilter(double range){
		this.range = range;
	}
	
	public void setRange(double r){
		this.range = r;
	}
	
	@Override
	public void adjustFilter(double curValue) {
		if (!this.checkFlag){
			this.window_low = curValue - this.range;
			this.window_high = curValue + this.range;
		}
	}
}
