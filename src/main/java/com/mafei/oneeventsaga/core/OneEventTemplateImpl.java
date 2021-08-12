package com.mafei.oneeventsaga.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

@Configuration
public class OneEventTemplateImpl<Aggregate> {

    Logger logger = LoggerFactory.getLogger(OneEventTemplateImpl.class);

    @Bean
    public OneEventTemplate<Aggregate> oneEventTemplate(ApplicationContext context) {
        return (aClass, aggregate, args) -> {
            Class<?>[] declaredClasses = aClass.getDeclaredClasses();
            Arrays.stream(declaredClasses).forEach(aClass1 -> {
                logger.info("sub aClass {}", (Object) aClass1.getName().split("\\$"));
                Object bean = context.getBean(aClass1);
                Method[] methods = bean.getClass().getMethods();
                Optional<Method> process = Arrays.stream(methods).filter(method -> method.getName().equals("process")).findFirst();

                process.ifPresent(method -> {
                    Arrays.stream(method.getParameterTypes()).forEach(_method -> {
                        System.out.println("_method = " + _method.getName());
                    });
                    try {
                        System.out.println("OneEventTemplate > Thread name" + Thread.currentThread().getName());
                        Object invoke = method.invoke(bean, aggregate, args);
                        System.out.println("OneEventTemplateImpl.oneEventTemplate > " + invoke);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });

            });
        };
    }
}
