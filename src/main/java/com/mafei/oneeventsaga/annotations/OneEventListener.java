package com.mafei.oneeventsaga.annotations;

import com.mafei.oneeventsaga.process.RunningMode;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Component
public @interface OneEventListener {
    String value() default "";

    int order() default 1;

    RunningMode mode() default RunningMode.Async;
}
