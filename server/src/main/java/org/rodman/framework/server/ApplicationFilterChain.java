package org.rodman.framework.server;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author 余勇
 * @date 2021年03月07日 16:07:00
 */
public class ApplicationFilterChain implements FilterChain {

	public ApplicationFilterChain(HttpServlet servlet) {
		this.servlet = servlet;
	}

	private final HttpServlet servlet;

	public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
		for (String mapping : FilterContainer.FILTER_CONTAINER.keySet()) {
			if (((HttpServletRequest)request).getRequestURI().startsWith(mapping)) {
				for (HttpFilter httpFilter : FilterContainer.FILTER_CONTAINER.get(mapping)) {
					httpFilter.doFilter(request, response, this);
				}
			}
		}

		if (servlet == null) {
			throw new ServerException("该页面未找到>>" + ((HttpServletRequest)request).getRequestURI());
		}
		servlet.service(request, response);
	}

}
