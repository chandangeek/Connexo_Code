package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by akuryuk on 07.04.2016.
 */
public class UsagePointMetrologyConfigurationInfos {
    public int total;
    public List<UsagePointMetrologyConfigurationInfo> metrologyConfigurations = new ArrayList<>();

    UsagePointMetrologyConfigurationInfos(Iterable<? extends UsagePointMetrologyConfiguration> usagePointMetrologyConfigurations) {
        addAll(usagePointMetrologyConfigurations);
    }

    UsagePointMetrologyConfigurationInfo add(UsagePointMetrologyConfiguration usagePointMetrologyConfiguration) {
        UsagePointMetrologyConfigurationInfo result = new UsagePointMetrologyConfigurationInfo(usagePointMetrologyConfiguration);
        metrologyConfigurations.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends UsagePointMetrologyConfiguration> usagePointMetrologyConfigurations) {
        for (UsagePointMetrologyConfiguration each : usagePointMetrologyConfigurations) {
            add(each);
        }
    }
}
