package org.rodman.framework.aop;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import cn.hutool.core.collection.CollUtil;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * @author yuy
 * @date 2021-03-05 17:14
 **/
public class CglibDynamicAopProxy implements MethodInterceptor {

    private List<Advisor> advisors;

    public static CglibDynamicAopProxy newInstance(List<Advisor> advisors){
        CglibDynamicAopProxy aopProxy = new CglibDynamicAopProxy();
        aopProxy.advisors = advisors;
        return aopProxy;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Advisor advisor = null;
        while (CollUtil.isNotEmpty(advisors)) {
            advisor = advisors.remove(0);
            if (advisor.getPointCut().match(method.getDeclaringClass())) {
                break;
            }
            advisor = null;
        }

        if (Objects.isNull(advisor)) {
            return methodProxy.invokeSuper(o, objects);
        }else {
            JoinPoint joinPoint = JoinPoint.newInstance(o, methodProxy, objects);
            return advisor.getAdviceMethod().invoke(advisor.getAspectBean(), joinPoint);
        }

    }
}
