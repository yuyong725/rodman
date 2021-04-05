package org.rodman.framework.ioc;

import java.lang.annotation.Annotation;

/**
 * @author 余勇
 * @date 2021年03月24日 13:26:00
 */
public @interface EnableAutoConfiguration {

	/**
	 * 要被引入的配置类
	 */
	Class<? extends Annotation>[] configClass() default {};

}
