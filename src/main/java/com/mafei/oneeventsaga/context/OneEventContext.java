package com.mafei.oneeventsaga.context;

import com.mafei.oneeventsaga.annotations.Secondary;
import com.mafei.oneeventsaga.exception.CurrentStepNotFountException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Arrays;
import java.util.Objects;

public class OneEventContext {

    private final static String CURRENT_STATE = "CURRENT_STATE";

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

    public static Secondary getCurrentStep() throws CurrentStepNotFountException {
        return (Secondary) RequestContextHolder.getRequestAttributes().getAttribute(CURRENT_STATE, RequestAttributes.SCOPE_REQUEST);
    }

    public static void setCurrentStep(Secondary secondary) {
        Objects.requireNonNull(RequestContextHolder.getRequestAttributes()).setAttribute(CURRENT_STATE, secondary, RequestAttributes.SCOPE_REQUEST);
    }

}
