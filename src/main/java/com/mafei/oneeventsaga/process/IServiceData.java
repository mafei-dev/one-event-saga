package com.mafei.oneeventsaga.process;

import com.mafei.oneeventsaga.annotations.Secondary;
import com.mafei.oneeventsaga.annotations.Start;

public class IServiceData {
    private Class<?> serviceClass;
    private ServiceTypes serviceType;
    private ProcessStatus processStatus = ProcessStatus.NOT_YET;
    private Start start;
    private Secondary secondary;
    private Object fullObject;
    private String beanName;

    public Class<?> getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public ServiceTypes getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceTypes serviceType) {
        this.serviceType = serviceType;
    }

    public Start getStart() {
        return start;
    }

    public void setStart(Start start) {
        this.start = start;
    }

    public Secondary getSecondary() {
        return secondary;
    }

    public void setSecondary(Secondary secondary) {
        this.secondary = secondary;
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

    public ProcessStatus getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(ProcessStatus processStatus) {
        this.processStatus = processStatus;
    }
}
