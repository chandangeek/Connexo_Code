package com.elster.insight.usagepoint.config.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;

@XmlRootElement
public class MetrologyConfigurationInfo {

    public long id;
    public long version;
    public String name;

    public MetrologyConfigurationInfo() {
    }

    public MetrologyConfigurationInfo(MetrologyConfiguration metrologyConfiguration) {
        this.id = metrologyConfiguration.getId();
        this.version = metrologyConfiguration.getVersion();
        this.name = metrologyConfiguration.getName();
    }
    
    public void writeTo(MetrologyConfiguration metrologyConfiguration) {
        metrologyConfiguration.setName(this.name);
    }
    
}