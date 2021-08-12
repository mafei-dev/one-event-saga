package com.mafei.oneeventsaga.context;

public class ContextObjectSaveData {
    private String className;
    private Object data;

    public ContextObjectSaveData() {
    }

    public ContextObjectSaveData(String className, Object data) {
        this.className = className;
        this.data = data;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
