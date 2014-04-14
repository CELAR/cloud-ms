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
package eu.celarcloud.jcatascopia.agentpack.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import eu.celarcloud.jcatascopia.exceptions.CatascopiaException;
import eu.celarcloud.jcatascopia.probepack.IProbe;

/**
 * CatascopiaProbeFactory is a library that contains functions for creating dynamically
 * new instances of classes on runtime
 * @author Demetris Trihinas
 *
 */
public class CatascopiaProbeFactory {
	
	public static IProbe newInstance(String PROBE_LIB,String myProbeClass) throws CatascopiaException {
		URLClassLoader tmp = new URLClassLoader( new URL[] { getClassPath() }){ 
			@Override
			public synchronized Class<?> loadClass(String name) throws ClassNotFoundException {
				return super.loadClass(name);
			}
		};

		try{
			return (IProbe) tmp.loadClass(PROBE_LIB+myProbeClass).newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e.getCause());
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new CatascopiaException(myProbeClass+": Requested probe does not exist", 
					                      CatascopiaException.ExceptionType.PROBE_EXISTANCE);
		}
	}

	private static URL getClassPath() {
		String resName = CatascopiaProbeFactory.class.getName().replace('.', '/') + ".class";
		String loc = CatascopiaProbeFactory.class.getClassLoader().getResource(resName).toExternalForm();    
		URL cp;
		try {
			cp = new URL(loc.substring(0, loc.length() - resName.length()));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		return cp;
	}
}
