package org.rodman.framework.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import lombok.Data;

/**
 * @author 余勇
 * @date 2021年03月04日 23:30:00
 */
@Data
public class Advisor {

    private Object aspectBean;
    private Method adviceMethod;
    private PointCut pointCut;


    public static Advisor newInstance(Object aspectBean, Method adviceMethod, Class<? extends Annotation> annotationClass) {
        Advisor advisor = new Advisor();
        advisor.setAdviceMethod(adviceMethod);
        advisor.setPointCut(PointCut.newInstance(annotationClass));
        advisor.setAspectBean(aspectBean);
        return advisor;
    }
}
