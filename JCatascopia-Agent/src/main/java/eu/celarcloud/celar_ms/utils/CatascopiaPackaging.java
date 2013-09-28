package eu.celarcloud.celar_ms.utils;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import eu.celarcloud.celar_ms.Exceptions.CatascopiaException;

/**
 * CatascopiaPackaging is a library that contains function for listing resources 
 * such as available classes in packages
 * 
 * @author Demetris Trihinas
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CatascopiaPackaging{
	/**
	 * 
	 * @param packageName
	 * @return list of classes in specified package path
	 * @throws CatascopiaException
	 */
	public static ArrayList<String> listClassesInPackage(String packageName) throws CatascopiaException{
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources;
		
		ArrayList<String> classList = null;
		try {
			resources = classLoader.getResources(path);
		
			List<String> dirs = new ArrayList<String>();
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				dirs.add(URLDecoder.decode(resource.getFile(), "UTF-8"));
			}
			TreeSet<String> classes = new TreeSet<String>();
			for (String directory : dirs) {
				classes.addAll(findClasses(directory, packageName));
			}
			classList = new ArrayList<String>();
			for (Object c : classes) {
				String clazz = c.toString();
				//classList.add(Class.forName(clazz).toString());
				String[] temp = clazz.split("\\.");
				classList.add(temp[temp.length-1]);
			}
		} catch (IOException e) {
			throw new CatascopiaException("IOException", CatascopiaException.ExceptionType.PACKAGING);
		}
		return classList;
	}
	
	private static TreeSet findClasses(String path, String packageName) throws MalformedURLException, IOException {
		TreeSet classes = new TreeSet();
		if (path.startsWith("file:") && path.contains("!")) {
			String[] split = path.split("!");
			URL jar = new URL(split[0]);
			ZipInputStream zip = new ZipInputStream(jar.openStream());
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				if (entry.getName().endsWith(".class")) {
					String className = entry.getName().replaceAll("[$].*", "").replaceAll("[.]class", "").replace('/', '.');
					if (className.startsWith(packageName)) {
						classes.add(className);
					}
				}
			}
		}
		File dir = new File(path);
		if (!dir.exists()) {
			return classes;
		}
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file.getAbsolutePath(), packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
				classes.add(className);
			}
		}
		return classes;
	}
}