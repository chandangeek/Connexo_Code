package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.util.List;

public class LinkableMetrologyContractInfo {

    private IdWithNameInfo metrologyConfigurationInfo;
    private boolean isActive;
    private String purpose;
    private List<OutputMatchesInfo> outputs;
    private MetrologyContractInfo metrologyContractInfo;

    public void setMetrologyConfigurationInfo(IdWithNameInfo metrologyConfigurationInfo) {
        this.metrologyConfigurationInfo = metrologyConfigurationInfo;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public void setOutputs(List<OutputMatchesInfo> outputs) {
        this.outputs = outputs;
    }

    public void setMetrologyContractInfo(MetrologyContractInfo metrologyContractInfo) {
        this.metrologyContractInfo = metrologyContractInfo;
    }

    public IdWithNameInfo getMetrologyConfigurationInfo() {
        return metrologyConfigurationInfo;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getPurpose() {
        return purpose;
    }

    public List<OutputMatchesInfo> getOutputs() {
        return outputs;
    }

    public MetrologyContractInfo getMetrologyContractInfo() {
        return metrologyContractInfo;
    }
}
