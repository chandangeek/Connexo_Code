package com.elster.jupiter.kore.api.impl;

public class MetrologyConfigurationPurposeInfo {
    public Long id;
    public String name;
    public Boolean required;
    public String status;

    public MetrologyConfigurationPurposeInfo() {
    }

    public MetrologyConfigurationPurposeInfo(Long id, String name, Boolean required, String status) {
        this.id = id;
        this.name = name;
        this.required = required;
        this.status = status;
    }
}
