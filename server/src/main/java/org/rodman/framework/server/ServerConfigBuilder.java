package org.rodman.framework.server;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;

/**
 * @author 余勇
 * @date 2021年03月06日 19:01:00
 */
public class ServerConfigBuilder {

	private static final Map<String, String> config = new HashMap<String, String>();

	public static String getProperty(String key) {
		return config.get(key);
	}

	public static void builder() throws IOException {
		if (!config.isEmpty()) {
			return;
		}
		InputStream ins = ServerConfigBuilder.class.getClassLoader().getResourceAsStream("server.properties");
		if (ins == null) {
			throw new ServerException("server.properties不存在");
		}
		loadPropertyByDir(ins);
	}

	private static void loadPropertyByDir(InputStream inStream) throws IOException {
		Properties prop = new Properties();
		prop.load(inStream);
		Enumeration<Object> keys = prop.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String value = prop.getProperty(key);
			if (StrUtil.isBlank(value)) {
				value = "";
			}
			config.put(key, value.trim());
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T flush(ServerConfig config, String prefix)
		throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = config.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (Modifier.isFinal(field.getModifiers())) {
				continue;
			}
			String configField = prefix + "." + field.getName();
			String configValue = getProperty(configField);
			if (StrUtil.isBlank(configValue)) {
				continue;
			}

			Object parseValue = Convert.convert(field.getType(), configValue);

			field.setAccessible(true);
			field.set(config, parseValue);
		}
		return (T) config;
	}

}
