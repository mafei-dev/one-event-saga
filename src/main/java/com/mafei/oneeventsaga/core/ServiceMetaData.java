package com.mafei.oneeventsaga.core;

import com.mafei.oneeventsaga.process.ServiceTypes;

public class ServiceMetaData {
    private ServiceTypes serviceType;
    private String version;
    private String description;
    private double step;

    public ServiceTypes getServiceType() {
        return serviceType;
    }

    protected void setServiceType(ServiceTypes serviceType) {
        this.serviceType = serviceType;
    }

    public String getVersion() {
        return version;
    }

    protected void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    public double getStep() {
        return step;
    }

    protected void setStep(double step) {
        this.step = step;
    }
}
