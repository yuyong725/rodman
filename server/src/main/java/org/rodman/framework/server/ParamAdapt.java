package org.rodman.framework.server;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.core.util.StrUtil;

/**
 * @author 余勇
 * @date 2021年03月06日 21:28:00
 */
public class ParamAdapt {

	/**
	 * 简单装载参数
	 *
	 * @param queryString
	 * @return
	 */
	public static Map<String, List<Object>> buildGeneralParams(String queryString) {
		if (StrUtil.isBlank(queryString)) {
			return new HashMap<String, List<Object>>();
		}
		String[] lines = queryString.split("&");
		Map<String, List<Object>> params = new HashMap<String, List<Object>>();
		for (String line : lines) {
			try {
				int index = line.indexOf("=");
				if (index < 1 || index == line.length() - 1) {
					continue;
				}
				String paramName = line.substring(0, index);
				String paramValue = URLDecoder.decode(line.substring(index + 1));
				if (!params.containsKey(paramName)) {
					List<Object> paramValues = new ArrayList<Object>();
					params.put(paramName, paramValues);
				}
				params.get(paramName).add(paramValue);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return params;
	}

}
