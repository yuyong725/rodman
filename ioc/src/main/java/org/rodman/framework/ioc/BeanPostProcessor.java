package org.rodman.framework.ioc;

/**
 * @author 余勇
 * @date 2021年03月04日 22:13:00
 */
public interface BeanPostProcessor {

	default Object postProcessAfterInitialization(Object bean, String beanName) {
		return bean;
	}

}
