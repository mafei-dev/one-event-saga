package com.mafei.oneeventsaga.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Component
public @interface Secondary {

    String value() default "";

    double step();

    String version();

    String description() default "";

//    Class<?> with();


}
