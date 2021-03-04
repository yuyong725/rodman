package org.rodman.framework.ioc.test.components;

import cn.hutool.core.lang.Console;
import org.rodman.framework.ioc.Component;

/**
 * @author 余勇
 * @date 2021年03月04日 20:47:00
 */
@Component
public class HelloService {

	public void say(String phrase) {
		Console.log(phrase);
	}
}