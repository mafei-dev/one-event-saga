package com.mafei.oneeventsaga.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.oneevent")
public class OneEventProperties {

    private Integer port;
    private String componentScan;
    private String path;

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getComponentScan() {
        return componentScan;
    }

    public void setComponentScan(String componentScan) {
        this.componentScan = componentScan;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
