package com.elster.jupiter.mdm.usagepoint.config.rest;

import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.util.List;

public class MetrologyConfigValidationRuleSetInfo {

    private IdWithNameInfo metrologyConfigurationInfo;
    private boolean isActive;
    private String purpose;
    private List<OutputMatchesInfo> outputs;
    private long metrologyContractId;

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

    public void setMetrologyContractId(long metrologyContractId) {
        this.metrologyContractId = metrologyContractId;
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

    public long getMetrologyContractId() {
        return metrologyContractId;
    }
}
