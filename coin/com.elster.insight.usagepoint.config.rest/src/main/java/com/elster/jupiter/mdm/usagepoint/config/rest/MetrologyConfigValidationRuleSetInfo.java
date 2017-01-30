package com.elster.jupiter.mdm.usagepoint.config.rest;

import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.util.List;

public class MetrologyConfigValidationRuleSetInfo {

    public IdWithNameInfo metrologyConfigurationInfo;
    public boolean isActive;
    public String purpose;
    public List<OutputMatchesInfo> outputs;
    public long metrologyContractId;

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
}
