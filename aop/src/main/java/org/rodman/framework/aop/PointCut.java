package org.rodman.framework.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import lombok.Data;

/**
 * @author yuy
 * @date 2021-03-05 11:30
 **/
@Data
public class PointCut {

    private Class<? extends Annotation> annotationClass;

    public static PointCut newInstance(Class<? extends Annotation> annotationClass) {
        PointCut pointCut = new PointCut();
        pointCut.setAnnotationClass(annotationClass);
        return pointCut;
    }

    public boolean match(Class<?> targetClass){
        Method[] declaredMethods = targetClass.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.isAnnotationPresent(annotationClass)) {
                return true;
            }
        }
        return false;
    }

}
