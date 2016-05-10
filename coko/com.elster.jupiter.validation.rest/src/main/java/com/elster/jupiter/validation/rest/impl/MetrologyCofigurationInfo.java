package com.elster.jupiter.validation.rest.impl;


import com.elster.jupiter.metering.config.MetrologyConfiguration;

public class MetrologyCofigurationInfo {
    public long id;
    public String name;

    public MetrologyCofigurationInfo() {
    }

    public MetrologyCofigurationInfo(MetrologyConfiguration metrologyConfiguration) {
        id = metrologyConfiguration.getId();
        name = metrologyConfiguration.getName();
    }
}
