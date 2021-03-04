package org.rodman.framework.ioc.test.components;

import javax.annotation.Resource;

import org.rodman.framework.ioc.Component;

/**
 * @author 余勇
 * @date 2021年03月04日 20:47:00
 */
@Component
public class HelloController {

	@Resource
	private HelloService helloService;

	public void say(String phrase){
		helloService.say(phrase);
	}


}
