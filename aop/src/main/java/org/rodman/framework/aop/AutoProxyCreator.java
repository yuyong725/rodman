package org.rodman.framework.aop;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.hutool.core.collection.CollUtil;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.rodman.framework.ioc.BeanFactory;
import org.rodman.framework.ioc.BeanFactoryAware;
import org.rodman.framework.ioc.BeanPostProcessor;
import org.rodman.framework.ioc.Component;

/**
 * @author 余勇
 * @date 2021年03月04日 23:04:00
 */
@Component
public class AutoProxyCreator implements BeanPostProcessor, BeanFactoryAware {

	private BeanFactory beanFactory;
	private List<Advisor> advisorsCache;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		if (bean.getClass().isAnnotationPresent(Aspect.class)) {
			return bean;
		}

		List<Advisor> candidateAdvisors = findCandidateAdvisors();
		// 找合适的通知器
		List<Advisor> advisorsThatCanApply = findAdvisorsThatCanApply(candidateAdvisors, bean.getClass());
		// 设置代理
		if (CollUtil.isEmpty(advisorsThatCanApply)) {
			return bean;
		}else {
			CglibDynamicAopProxy aopProxy = CglibDynamicAopProxy.newInstance(advisorsThatCanApply);
			return getProxy(bean.getClass(), aopProxy);
		}
	}

	/**
	 * 找所有的通知器
	 * 基于注解
	 */
	private List<Advisor> findCandidateAdvisors() {
		if (Objects.isNull(advisorsCache)) {
			synchronized (this) {
				List<Object> aspects = beanFactory.getBeansByAnnotation(Aspect.class);
				advisorsCache = new ArrayList<>();
				for (Object aspect : aspects) {
					Method[] methods = aspect.getClass().getDeclaredMethods();
					for (Method method : methods) {
						if (method.isAnnotationPresent(Around.class)) {
							Around around = method.getAnnotation(Around.class);
							Class<? extends Annotation>[] annotationClasses = around.annotationClass();
							for (Class<? extends Annotation> annotationClass : annotationClasses) {
								Advisor advisor = Advisor.newInstance(aspect, method, annotationClass);
								advisorsCache.add(advisor);
							}
						}
					}
				}
			}
		}
		return advisorsCache;
	}

	/**
	 * 寻找符合条件的
	 */
	private List<Advisor> findAdvisorsThatCanApply(List<Advisor> candidateAdvisors, Class<?> beanClass) {
		List<Advisor> advisorsThatCanApply = new ArrayList<>();
		for (Advisor advisor : candidateAdvisors) {
			if (advisor.getPointCut().match(beanClass)) {
				advisorsThatCanApply.add(advisor);
			}
		}
		return advisorsThatCanApply;
	}

	/**
	 * 创建代理
	 */
	public Object getProxy(Class<?> targetClass, MethodInterceptor methodInterceptor) {
		Enhancer enhancer = new Enhancer();
		// 设置代理类的父类
		enhancer.setSuperclass(targetClass);
		// 设置代理逻辑
		enhancer.setCallback(methodInterceptor);
		// 创建代理对象
		return enhancer.create();
	}
}
