package com.mafei.oneeventsaga.context;

import com.mafei.oneeventsaga.annotations.Secondary;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Arrays;

public  class OneEventContext {


    public static void apply(Object aClass, Object... args) {
        System.out.println("stepClass " + aClass.getClass().getSimpleName());
        Secondary declaredAnnotation = aClass.getClass().getDeclaredAnnotation(Secondary.class);
        Arrays.stream(args).forEach(o -> {
            System.out.println("currentData.getClass().getSimpleName() = " + o.getClass().getName());
        });
        ContextObjectSaveData contextObjectSaveData = new ContextObjectSaveData();
//        contextObjectSaveData.setClassName(currentData.getClass().getName());
//        contextObjectSaveData.setData(currentData);
        RequestContextHolder.getRequestAttributes().setAttribute(Double.toString(declaredAnnotation.step()), args, RequestAttributes.SCOPE_REQUEST);
    }
}
