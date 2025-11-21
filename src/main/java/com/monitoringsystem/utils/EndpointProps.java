package com.monitoringsystem.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface EndpointProps
{
    String prefixPath();
    String templatePath();
    String httpMethod();
    String[] allowedRoles() default {"ADMIN"};
}
