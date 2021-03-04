package org.rodman.framework;

import javax.annotation.Resource;
import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ClassUtil;

/**
 * @author 余勇
 * @date 2021年03月04日 20:44:00
 */
public class BeanFactory {

	/**
	 * beanDefinition Map
	 */
	private final Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

	private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

	public BeanFactory(Class<?> mainClass) throws Exception {
		init(mainClass);
	}

	/**
	 * 扫描启动类所在目录的所有子目录下的所有 @Bean 注解
	 */
	private void init(Class<?> mainClass) throws ClassNotFoundException {
		Console.log("开始启动IOC容器");
		// 获取主类所在目录
		final String packagePath = ClassUtil.getPackagePath(mainClass);
		final String classPath = ClassUtil.getClassPath();
		// 加载目录下，包含指定注解的类
		List<File> files = FileUtil.loopFiles(classPath + packagePath, pathname -> pathname.getName().endsWith(".class"));
		for (File file : files) {
			String className = file.getAbsolutePath().substring(classPath.length())
				.replace("/", ".")
				.replace("\\", ".")
				.replace(".class", "");
			Class<?> beanClass = Class.forName(className);
			// fixme 不健壮，还可能是接口
			if (beanClass.isAnnotationPresent(Component.class)) {
				// 转成beanDefinition存起来
				String beanName = beanClass.getSimpleName();
				BeanDefinition beanDefinition = BeanDefinition.newInstance(beanName, beanClass);
				// 拿到类里面所有包含 @Resource 的字段
				Field[] declaredFields = beanClass.getDeclaredFields();
				for (Field declaredField : declaredFields) {
					if (declaredField.isAnnotationPresent(Resource.class)) {
						beanDefinition.getInjectFieldMap().put(declaredField.getType(), declaredField);
					}
				}
				beanDefinitionMap.put(beanName, beanDefinition);
				Console.log("初始化 beanDefinition =====> {} ", beanName);
			}
		}
		Console.log("IOC容器结束");
	}

	/**
	 * 根据名字获取实例
	 */
	@SuppressWarnings("unchecked")
	public <T> T getBean(Class<T> requiredType) throws IllegalAccessException, InstantiationException {
		String beanName = requiredType.getSimpleName();

		if (singletonObjects.containsKey(beanName)){
			return (T) singletonObjects.get(beanName);
		}

		BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
		Object instance = beanDefinition.getBeanClass().newInstance();
		singletonObjects.put(beanName, (T) instance);
		Console.log("实例化 bean =====> {} ", beanName);

		Map<Class<?>, Field> injectFieldMap = beanDefinition.getInjectFieldMap();
		for (Class<?> fieldClass : injectFieldMap.keySet()) {
			Object injectBean = getBean(fieldClass);
			Field field = injectFieldMap.get(fieldClass);
			field.setAccessible(true);
			field.set(instance, injectBean);
			Console.log("{} 注入到=====> {} ", fieldClass.getSimpleName(), beanName);
		}

		return (T) instance;
	}

}
