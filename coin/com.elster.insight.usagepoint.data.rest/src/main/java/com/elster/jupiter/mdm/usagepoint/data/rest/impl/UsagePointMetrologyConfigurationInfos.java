package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class UsagePointMetrologyConfigurationInfos {
    public int total = 0;
    public List<UsagePointMetrologyConfigurationInfo> metrologyConfigurations = new ArrayList<UsagePointMetrologyConfigurationInfo>();

    UsagePointMetrologyConfigurationInfos(Iterable<? extends UsagePointMetrologyConfigurationInfo> usagePointMetrologyConfigurations) {
        addAll(usagePointMetrologyConfigurations);
    }

    public UsagePointMetrologyConfigurationInfos() {
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
