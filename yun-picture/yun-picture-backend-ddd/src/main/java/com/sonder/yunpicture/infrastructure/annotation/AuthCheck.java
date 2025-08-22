package com.sonder.yunpicture.infrastructure.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // 注解作用在方法上
@Retention(RetentionPolicy.RUNTIME) // 运行时生效
public @interface AuthCheck {

    /**
     * 必须有该角色
     */
    String mustRole() default "";
}
