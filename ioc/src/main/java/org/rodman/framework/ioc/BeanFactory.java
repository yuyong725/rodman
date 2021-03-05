package org.rodman.framework.ioc;

import javax.annotation.Resource;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Console;

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
	private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>(16);

	public BeanFactory(Class<?>... sourceClasses) throws Exception {
		init(sourceClasses);
	}

	/**
	 * 扫描启动类所在目录的所有子目录下的所有 @Bean 注解
	 */
	private void init(Class<?>... sourceClasses) throws ClassNotFoundException {
		Console.log("开始启动IOC容器");
		loadBeanDefinition(sourceClasses);

		resolveAware();

		loadBeanPostProcessor();

		Console.log("IOC容器结束");
	}

	/**
	 * 加载指定根目录下所有的bean
	 */
	private void loadBeanDefinition(Class<?>... sourceClasses) throws ClassNotFoundException{
		Console.log("开始加载指定根目录下所有的beanDefinition");
		// 加载目录下，包含指定注解的类
		Set<Class<?>> classes = new HashSet<>();
		for (Class<?> sourceClass : sourceClasses) {
			String aPackage = ClassUtil.getPackage(sourceClass);
			Set<Class<?>> subClasses = ClassUtil.getClasses(aPackage);
			classes.addAll(subClasses);
		}
		for (Class<?> beanClass : classes) {
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
		Console.log("加载指定根目录下所有的beanDefinition完毕");
	}

	/**
	 * 初始化所有 beanPostProcessor
	 */
	public void loadBeanPostProcessor() {
		Console.log("初始化所有 beanPostProcessor");
		for (BeanDefinition beanDefinition : beanDefinitionMap.values()) {
			Class<?> beanClass = beanDefinition.getBeanClass();
			if (BeanPostProcessor.class.isAssignableFrom(beanClass)) {
				Object bean = getBean(beanClass);
				beanPostProcessors.add((BeanPostProcessor) bean);
				Console.log("初始化 beanPostProcessor:[{}] 完成", beanClass.getName());
			}
		}
		Console.log("初始化所有 beanPostProcessor 完成");
	}

	/**
	 * 注入aware
	 */
	private void resolveAware() {
		Console.log("开始注入aware");
		for (BeanDefinition beanDefinition : beanDefinitionMap.values()) {
			Class<?> beanClass = beanDefinition.getBeanClass();
			if (BeanFactoryAware.class.isAssignableFrom(beanClass)) {
				BeanFactoryAware bean = (BeanFactoryAware) getBean(beanClass);
				bean.setBeanFactory(this);
				Console.log("初始化 beanPostProcessor:[{}] 完成", beanClass.getName());
			}
		}

		Console.log("注入aware结束");
	}

	/**
	 * 根据名字获取实例
	 */
	@SuppressWarnings("unchecked")
	public <T> T getBean(Class<T> requiredType) {
		String beanName = requiredType.getSimpleName();

		if (singletonObjects.containsKey(beanName)){
			return (T) singletonObjects.get(beanName);
		}

		BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
		Object instance = null;
		try {
			Class<?> beanClass = beanDefinition.getBeanClass();
			instance = beanClass.newInstance();
			if (!BeanPostProcessor.class.isAssignableFrom(beanClass)) {
				for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
					instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
				}
			}
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		singletonObjects.put(beanName, (T) instance);
		Console.log("实例化 bean =====> {} ", beanName);

		Map<Class<?>, Field> injectFieldMap = beanDefinition.getInjectFieldMap();
		for (Class<?> fieldClass : injectFieldMap.keySet()) {
			Object injectBean = getBean(fieldClass);
			Field field = injectFieldMap.get(fieldClass);
			field.setAccessible(true);
			try {
				field.set(instance, injectBean);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			Console.log("{} 注入到=====> {} ", fieldClass.getSimpleName(), beanName);
		}

		return (T) instance;
	}

	public List<Object> getBeansByAnnotation(Class<? extends Annotation> type) {
		List<Object> beans = new ArrayList<>();
		for (BeanDefinition beanDefinition : beanDefinitionMap.values()) {
			Class<?> beanClass = beanDefinition.getBeanClass();
			if (beanClass.isAnnotationPresent(type)) {
				beans.add(getBean(beanClass));
			}
		}
		return beans;
	}
}
