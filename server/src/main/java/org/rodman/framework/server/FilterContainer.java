package org.rodman.framework.server;

import javax.servlet.http.HttpFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 余勇
 * @date 2021年03月07日 13:13:00
 */
public class FilterContainer {

	public static final Map<String, List<HttpFilter>> FILTER_CONTAINER=new HashMap<String, List<HttpFilter>>();

	public static void putServlet(String mapping, HttpFilter filter) {
		if (!FILTER_CONTAINER.containsKey(mapping)) {
			FILTER_CONTAINER.put(mapping, new ArrayList<>());
		}
		FILTER_CONTAINER.get(mapping).add(filter);
	}
}
