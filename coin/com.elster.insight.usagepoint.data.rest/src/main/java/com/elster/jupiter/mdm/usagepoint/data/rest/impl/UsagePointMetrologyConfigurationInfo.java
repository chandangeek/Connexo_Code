package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;

import java.util.ArrayList;
import java.util.List;


public class UsagePointMetrologyConfigurationInfo {
    public long id;
    public String name;
    public List<RegisteredCustomPropertySet> customPropertySets = new ArrayList<>();

    public UsagePointMetrologyConfigurationInfo(UsagePointMetrologyConfiguration usagePointMetrologyConfiguration) {
        this.id = usagePointMetrologyConfiguration.getId();
        this.name = usagePointMetrologyConfiguration.getName();
        this.customPropertySets = usagePointMetrologyConfiguration.getCustomPropertySets();
    }
}
