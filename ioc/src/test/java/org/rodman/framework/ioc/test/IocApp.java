package org.rodman.framework.ioc.test;

import org.rodman.framework.ioc.BeanFactory;
import org.rodman.framework.ioc.test.components.HelloController;

/**
 * @author 余勇
 * @date 2021年03月04日 20:46:00
 */
public class IocApp {

	public static void main(String[] args) throws Exception {
		BeanFactory factory = new BeanFactory(IocApp.class);
		HelloController helloController = factory.getBean(HelloController.class);
		helloController.say("IocApp Test");
	}

}
