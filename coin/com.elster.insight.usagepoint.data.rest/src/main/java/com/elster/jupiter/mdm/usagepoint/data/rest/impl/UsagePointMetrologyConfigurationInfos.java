package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by akuryuk on 07.04.2016.
 */
public class UsagePointMetrologyConfigurationInfos {
    public int total;
    public List<UsagePointMetrologyConfigurationInfo> metrologyConfigurations = new ArrayList<>();

    UsagePointMetrologyConfigurationInfos(Iterable<? extends UsagePointMetrologyConfigurationInfo> usagePointMetrologyConfigurations) {
        addAll(usagePointMetrologyConfigurations);
    }

    UsagePointMetrologyConfigurationInfo add(UsagePointMetrologyConfigurationInfo usagePointMetrologyConfiguration) {
        metrologyConfigurations.add(usagePointMetrologyConfiguration);
        total++;
        return usagePointMetrologyConfiguration;
    }

    void addAll(Iterable<? extends UsagePointMetrologyConfigurationInfo> usagePointMetrologyConfigurations) {
        for (UsagePointMetrologyConfigurationInfo each : usagePointMetrologyConfigurations) {
            add(each);
        }
    }
}
