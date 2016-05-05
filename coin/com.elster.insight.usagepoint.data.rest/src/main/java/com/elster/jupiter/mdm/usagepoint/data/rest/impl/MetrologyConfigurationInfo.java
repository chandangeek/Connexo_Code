package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MetrologyConfigurationInfo {
    public Long id;
    public String name;
    public Long version;
    public Instant activationTime;
    public List<CustomPropertySetInfo> customPropertySets = new ArrayList<>();


    public MetrologyConfigurationInfo() {
    }

    public MetrologyConfigurationInfo(MetrologyConfiguration metrologyConfiguration, UsagePoint usagePoint) {
        this.id = metrologyConfiguration.getId();
        this.name = metrologyConfiguration.getName();
        this.version = metrologyConfiguration.getVersion();
        this.activationTime = usagePoint.getInstallationTime();
    }

    public MetrologyConfigurationInfo(UsagePointMetrologyConfiguration usagePointMetrologyConfiguration, List<CustomPropertySetInfo> customPropertySets) {
        this.id = usagePointMetrologyConfiguration.getId();
        this.name = usagePointMetrologyConfiguration.getName();
        this.version = usagePointMetrologyConfiguration.getVersion();
        this.customPropertySets = customPropertySets;
    }
}
