import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import cn.hutool.core.util.StrUtil;

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
		InputStream ins = ServerConfigBuilder.class.getClassLoader().getResourceAsStream("cat.properties");
		if (ins == null) {
			throw new CatException("rodman.properties不存在");
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
	public static <T> T flush(BaseConfig config, String prefix)
		throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = config.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (Modifier.isFinal(field.getModifiers())) {
				continue;
			}
			String configField = prefix + "." + field.getName();
			Object configValue = getProperty(configField);
			if (StrUtil.isBlank(configValue.toString())) {
				continue;
			}
			configValue = PropertyUtil.parseValue(configValue, field.getType());
			field.setAccessible(true);
			field.set(config, configValue);
		}
		return (T) config;
	}

}
