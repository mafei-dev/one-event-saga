package com.mafei.oneeventsaga.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.oneevent")
public class OneEventProperties {

    private String[] componentScan;
    private String path;
    private boolean showStartUpSummary = true;
    private boolean showStartUpConfiguredSteps = true;
    private boolean showTransactionSummary = true;
    private boolean generateUi = true;


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public boolean isShowTransactionSummary() {
        return showTransactionSummary;
    }

    public void setShowTransactionSummary(boolean showTransactionSummary) {
        this.showTransactionSummary = showTransactionSummary;
    }

    public boolean isShowStartUpSummary() {
        return showStartUpSummary;
    }

    public void setShowStartUpSummary(boolean showStartUpSummary) {
        this.showStartUpSummary = showStartUpSummary;
    }

    public boolean isShowStartUpConfiguredSteps() {
        return showStartUpConfiguredSteps;
    }

    public void setShowStartUpConfiguredSteps(boolean showStartUpConfiguredSteps) {
        this.showStartUpConfiguredSteps = showStartUpConfiguredSteps;
    }

    public String[] getComponentScan() {
        return componentScan;
    }

    public void setComponentScan(String[] componentScan) {
        this.componentScan = componentScan;
    }

    public boolean isGenerateUi() {
        return generateUi;
    }

    public void setGenerateUi(boolean generateUi) {
        this.generateUi = generateUi;
    }
}
