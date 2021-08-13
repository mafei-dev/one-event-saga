package com.mafei.oneeventsaga.process;

import com.mafei.oneeventsaga.annotations.OneEventListener;

public class ServiceListenerData {
    private Class<?> listenerClass;
    private Object fullObject;
    private String beanName;
    private OneEventListener oneEventListener;

    public Class<?> getListenerClass() {
        return listenerClass;
    }

    public void setListenerClass(Class<?> listenerClass) {
        this.listenerClass = listenerClass;
    }

    public Object getFullObject() {
        return fullObject;
    }

    public void setFullObject(Object fullObject) {
        this.fullObject = fullObject;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public OneEventListener getOneEventListener() {
        return oneEventListener;
    }

    public void setOneEventListener(OneEventListener oneEventListener) {
        this.oneEventListener = oneEventListener;
    }
}
