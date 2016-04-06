package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;

import java.time.Instant;

public class MetrologyConfigurationInfo {
    public Long id;
    public String name;
    public Long version;
    public Instant activationTime;

    public MetrologyConfigurationInfo() {
    }

    public MetrologyConfigurationInfo(MetrologyConfiguration metrologyConfiguration, UsagePoint usagePoint) {
        this.id = metrologyConfiguration.getId();
        this.name = metrologyConfiguration.getName();
        this.version = metrologyConfiguration.getVersion();
        this.activationTime = usagePoint.getInstallationTime();
    }
}
