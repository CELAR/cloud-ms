package eu.celarcloud.celar_ms.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;
import eu.celarcloud.celar_ms.ProbePack.IProbe;

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
