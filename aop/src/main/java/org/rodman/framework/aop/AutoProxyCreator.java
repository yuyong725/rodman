package org.rodman.framework.aop;

import java.util.List;

import org.rodman.framework.ioc.BeanPostProcessor;
import org.rodman.framework.ioc.Component;

/**
 * @author 余勇
 * @date 2021年03月04日 23:04:00
 */
@Component
public class AutoProxyCreator implements BeanPostProcessor {

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		return wrapNecessary(bean);
	}

	private Object wrapNecessary(Object bean) {
		List<Advisor> advisors = getAdvicesAndAdvisorsForBean(bean.getClass());
		return createProxy(bean, advisors);
	}

	private Object createProxy(Object bean, List<Advisor> advisors) {
		return null;
	}

	private List<Advisor> getAdvicesAndAdvisorsForBean(Class<?> beanClass) {
		return null;
	}

}
