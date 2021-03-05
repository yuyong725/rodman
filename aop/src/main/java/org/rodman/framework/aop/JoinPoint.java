package org.rodman.framework.aop;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;

/**
 * @author yuy
 * @date 2021-03-05 11:28
 * 连接点
 **/
public class JoinPoint {

    private Object proxyBean;
    private MethodProxy proxyMethod;
    private Object[] args;

    public static JoinPoint newInstance(Object proxyBean, MethodProxy proxyMethod, Object[] args) {
        JoinPoint joinPoint = new JoinPoint();
        joinPoint.proxyBean = proxyBean;
        joinPoint.proxyMethod = proxyMethod;
        joinPoint.args = args;
        return joinPoint;
    }

    public Object proceed(){
        try {
            return proxyMethod.invoke(proxyBean, args);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }
}
