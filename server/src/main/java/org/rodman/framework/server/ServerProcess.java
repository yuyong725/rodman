package org.rodman.framework.server;

import javax.servlet.http.HttpServlet;

/**
 * @author 余勇
 * @date 2021年03月07日 16:06:00
 */
public class ServerProcess {

	public static void doService(HttpBuilder build) throws Exception {
		HttpServlet servlet = ServletContainer.getServlet(build.getRequest().getRequestURI());
		ApplicationFilterChain chain = new ApplicationFilterChain(servlet);
		chain.doFilter(build.getRequest(), build.getResponse());
	}

}
