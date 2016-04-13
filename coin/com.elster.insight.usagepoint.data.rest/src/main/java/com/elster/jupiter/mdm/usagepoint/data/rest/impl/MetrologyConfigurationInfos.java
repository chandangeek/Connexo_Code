package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class MetrologyConfigurationInfos {
    public int total = 0;
    public List<MetrologyConfigurationInfo> metrologyConfigurations = new ArrayList<MetrologyConfigurationInfo>();

    MetrologyConfigurationInfos(Iterable<? extends MetrologyConfigurationInfo> metrologyConfigurations) {
        addAll(metrologyConfigurations);
    }

    public MetrologyConfigurationInfos() {
    }

    MetrologyConfigurationInfo add(MetrologyConfigurationInfo metrologyConfiguration) {
        metrologyConfigurations.add(metrologyConfiguration);
        total++;
        return metrologyConfiguration;
    }

    void addAll(Iterable<? extends MetrologyConfigurationInfo> metrologyConfigurations) {
        for (MetrologyConfigurationInfo each : metrologyConfigurations) {
            add(each);
        }
    }
}
