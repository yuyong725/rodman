package org.rodman.framework.server;

import javax.servlet.http.HttpServlet;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 余勇
 * @date 2021年03月06日 23:40:00
 * 不支持模糊匹配
 */
public class ServletContainer {

	private static final Map<String, HttpServlet> SERVLET_CONTAINER = new HashMap<String, HttpServlet>();

	public static HttpServlet getServlet(String path) {
		return SERVLET_CONTAINER.get(path);
	}

	public static void putServlet(String path, HttpServlet servlet) {
		SERVLET_CONTAINER.put(path, servlet);
	}

	public static boolean match(String path){
		return SERVLET_CONTAINER.containsKey(path);
	}
}
