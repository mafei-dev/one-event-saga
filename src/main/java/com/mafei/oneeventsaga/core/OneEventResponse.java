package com.mafei.oneeventsaga.core;


import java.util.TreeMap;

public class OneEventResponse {
    private TreeMap<Double, ServiceData> completedService;
    private Class<?> lastCompletedService;
    private Class<?> errorService;
    private Boolean isCompleted = true;
    private Exception lastError;

    public TreeMap<Double, ServiceData> getCompletedService() {
        return completedService;
    }

    protected void setCompletedService(TreeMap<Double, ServiceData> completedService) {
        this.completedService = completedService;
    }

    public Class<?> getLastCompletedService() {
        return lastCompletedService;
    }

    protected void setLastCompletedService(Class<?> lastCompletedService) {
        this.lastCompletedService = lastCompletedService;
    }

    public Class<?> getErrorService() {
        return errorService;
    }

    protected void setErrorService(Class<?> errorService) {
        this.errorService = errorService;
    }

    public Boolean getCompleted() {
        return isCompleted;
    }

    protected void setCompleted(Boolean completed) {
        isCompleted = completed;
    }

    public Exception getLastError() {
        return lastError;
    }

    protected void setLastError(Exception lastError) {
        this.lastError = lastError;
    }

    @Override
    public String toString() {
        return "OneEventResponse{" +
                "completedService=" + completedService +
                ", lastCompletedService=" + lastCompletedService +
                ", errorService=" + errorService +
                ", isCompleted=" + isCompleted +
                ", lastError=" + lastError +
                '}';
    }
}
