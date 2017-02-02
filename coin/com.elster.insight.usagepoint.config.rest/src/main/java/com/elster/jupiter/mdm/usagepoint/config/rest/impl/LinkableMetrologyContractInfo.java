package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;

import java.util.List;

public class LinkableMetrologyContractInfo {

    private IdWithNameInfo metrologyConfigurationInfo;
    private boolean active;
    private List<OutputMatchesInfo> outputs;
    private long id;
    private long version;
    private String name;

    public void setMetrologyConfigurationInfo(IdWithNameInfo metrologyConfigurationInfo) {
        this.metrologyConfigurationInfo = metrologyConfigurationInfo;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setOutputs(List<OutputMatchesInfo> outputs) {
        this.outputs = outputs;
    }

    public boolean isActive() {
        return active;
    }

    public List<OutputMatchesInfo> getOutputs() {
        return outputs;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IdWithNameInfo getMetrologyConfigurationInfo() {
        return metrologyConfigurationInfo;
    }
}
