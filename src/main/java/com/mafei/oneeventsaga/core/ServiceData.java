package com.mafei.oneeventsaga.core;

import com.mafei.oneeventsaga.process.ProcessStatus;
import com.mafei.oneeventsaga.process.ServiceTypes;

import java.util.Arrays;

public class ServiceData {
    private ServiceMetaData metadata;
    private Class<?> serviceClass;
    private ServiceTypes serviceType;
    private ProcessStatus processStatus;
    private String beanName;
    private Object[] args;

    public ServiceMetaData getMetadata() {
        return metadata;
    }

    protected void setMetadata(ServiceMetaData metadata) {
        this.metadata = metadata;
    }

    public Class<?> getServiceClass() {
        return serviceClass;
    }

    protected void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public ServiceTypes getServiceType() {
        return serviceType;
    }

    protected void setServiceType(ServiceTypes serviceType) {
        this.serviceType = serviceType;
    }

    public ProcessStatus getProcessStatus() {
        return processStatus;
    }

    protected void setProcessStatus(ProcessStatus processStatus) {
        this.processStatus = processStatus;
    }

    public String getBeanName() {
        return beanName;
    }

    protected void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Object[] getArgs() {
        return args;
    }

    protected void setArgs(Object[] args) {
        this.args = args;
    }

    @Override
    public String toString() {
        return "ServiceData{" +
                "metadata=" + metadata +
                ", serviceClass=" + serviceClass +
                ", serviceType=" + serviceType +
                ", processStatus=" + processStatus +
                ", beanName='" + beanName + '\'' +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
