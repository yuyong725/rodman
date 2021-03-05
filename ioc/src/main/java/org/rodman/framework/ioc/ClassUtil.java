package org.rodman.framework.ioc;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author 余勇
 * @date 2021年03月05日 22:14:00
 */
public class ClassUtil {

	public static String getPackage(Class<?> clazz) {
		if (clazz == null) {
			return "";
		} else {
			String className = clazz.getName();
			int packageEndIndex = className.lastIndexOf(".");
			return packageEndIndex == -1 ? "" : className.substring(0, packageEndIndex);
		}
	}

	/**
	 * 从包package中获取所有的Class
	 *
	 * @param pack 包名
	 */
	public static Set<Class<?>> getClasses(String pack) {
		Set<String> clazzNameSet = getClassNames(pack);
		if (clazzNameSet.isEmpty()) {
			return Collections.emptySet();
		}

		CopyOnWriteArraySet<Class<?>> queue = new CopyOnWriteArraySet<Class<?>>();
		for (String clazzName : clazzNameSet) {
			Class<?> clazz = null;
			try {
				clazz = Thread.currentThread().getContextClassLoader().loadClass(clazzName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			queue.add(clazz);
		}
		return queue;
	}

	public static Set<String> getClassNames(String pack) {

		Set<String> classes = new LinkedHashSet<String>();
		boolean recursive = true;
		String packageName = pack;
		String packageDirName = packageName.replace('.', '/');
		Enumeration<URL> dirs;
		try {
			dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
			while (dirs.hasMoreElements()) {
				URL url = dirs.nextElement();
				String protocol = url.getProtocol();
				if ("file".equalsIgnoreCase(protocol)) {
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
					continue;
				}
				if (!"jar".equalsIgnoreCase(protocol)) {
					continue;
				}
				try {
					JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
					Enumeration<JarEntry> entries = jar.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						String name = entry.getName();
						if (name.charAt(0) == '/') {
							name = name.substring(1);
						}
						if (!name.startsWith(packageDirName)) {
							continue;
						}
						int idx = name.lastIndexOf('/');
						if (idx != -1) {
							packageName = name.substring(0, idx).replace('/', '.');
						}
						if (entry.isDirectory()) {
							continue;
						}
						if (name.endsWith(".class")) {
							String className = name.substring(packageName.length() + 1, name.length() - 6);
							classes.add(packageName + '.' + className);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return classes;
	}

	/**
	 * 以文件的形式来获取包下的所有Class
	 *
	 * @param packageName 包名
	 * @param packagePath 包路径
	 * @param recursive   是否递归
	 * @param classes     类
	 */
	public static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive,
		Set<String> classes) {
		File dir = new File(packagePath);
		if (!dir.exists() || !dir.isDirectory()) {
			return;
		}
		File[] dirfiles = dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
			}
		});
		for (File file : dirfiles) {
			if (file.isDirectory()) {
				findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive,
					classes);
				continue;
			}
			String className = file.getName().substring(0, file.getName().length() - 6);
			classes.add(packageName + '.' + className);
		}
	}

}
