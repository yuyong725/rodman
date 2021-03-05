package org.rodman.framework.aop;

import java.lang.reflect.Method;

import lombok.Data;

/**
 * @author yuy
 * @date 2021-03-05 11:28
 * 切面逻辑
 **/
@Data
public abstract class AroundAdvice {

    private Class<?> aspectClass;
    private Method invokeMethod;

    abstract void before(Method method, Object[] args, Object target);
}
