package org.rodman.framework.aop.test;

import org.rodman.framework.aop.AutoProxyCreator;
import org.rodman.framework.aop.test.components.HelloController;
import org.rodman.framework.ioc.BeanFactory;

/**
 * @author 余勇
 * @date 2021年03月04日 20:46:00
 */
public class AopApp {

	public static void main(String[] args) throws Exception {
		BeanFactory factory = new BeanFactory(AopApp.class, AutoProxyCreator.class);
		HelloController helloController = factory.getBean(HelloController.class);
		helloController.say("IocApp Test");
	}

}
