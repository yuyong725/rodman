package org.rodman.framework.aop.test.components;

import cn.hutool.core.lang.Console;
import org.rodman.framework.aop.Around;
import org.rodman.framework.aop.Aspect;
import org.rodman.framework.aop.JoinPoint;
import org.rodman.framework.ioc.Component;

/**
 * @author 余勇
 * @date 2021年03月05日 21:41:00
 */
@Aspect
@Component
public class HelloAspect {

	@Around(annotationClass = {AopLog.class, IocLog.class})
	public Object mixLog(JoinPoint jp) {
		Console.log("HelloAspect ===> mixLog 进入");
		Object proceed = jp.proceed();
		Console.log("HelloAspect ===> mixLog 出来");
		return proceed;
	}

	@Around(annotationClass = {AopLog.class})
	public Object apiLog(JoinPoint jp) {
		Console.log("HelloAspect ===> apiLog 进入");
		Object proceed = jp.proceed();
		Console.log("HelloAspect ===> apiLog 出来");
		return proceed;
	}

	@Around(annotationClass = {IocLog.class})
	public Object iocLog(JoinPoint jp) {
		Console.log("HelloAspect ===> iocLog 进入");
		Object proceed = jp.proceed();
		Console.log("HelloAspect ===> iocLog 出来");
		return proceed;
	}

}
