package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsagePointMetrologyConfigurationInfo {
    public long id;
    public String name;

    public List<CustomPropertySetInfo> customPropertySets = new ArrayList<>();

    public UsagePointMetrologyConfigurationInfo() {
    }

    public UsagePointMetrologyConfigurationInfo(UsagePointMetrologyConfiguration usagePointMetrologyConfiguration, List<CustomPropertySetInfo> customPropertySets) {
        this.id = usagePointMetrologyConfiguration.getId();
        this.name = usagePointMetrologyConfiguration.getName();
        this.customPropertySets = customPropertySets;
    }
}
