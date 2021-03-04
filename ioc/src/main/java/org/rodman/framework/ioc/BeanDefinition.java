package org.rodman.framework.ioc;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/**
 * @author 余勇
 * @date 2021年03月04日 20:43:00
 */
@Data
public class BeanDefinition {

	/**
	 * 类
	 */
	private Class<?> beanClass;

	/**
	 * 名字
	 */
	private String beanName;

	/**
	 * 要注入的字段
	 */
	private Map<Class<?>, Field> injectFieldMap = new HashMap<>();


	public static BeanDefinition newInstance(String beanName, Class<?> beanClass) {
		BeanDefinition beanDefinition = new BeanDefinition();
		beanDefinition.setBeanClass(beanClass);
		beanDefinition.setBeanName(beanName);
		return beanDefinition;
	}
}

