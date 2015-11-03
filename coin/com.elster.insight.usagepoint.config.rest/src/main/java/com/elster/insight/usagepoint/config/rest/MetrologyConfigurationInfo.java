package com.elster.insight.usagepoint.config.rest;

import java.time.Instant;

import javax.xml.bind.annotation.XmlRootElement;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;

@XmlRootElement
public class MetrologyConfigurationInfo {

    public long id;
    public long version;
    public String name;

    public Instant createTime;
    public Instant modTime;
    public String userName;

    public MetrologyConfigurationInfo() {
    }

    public MetrologyConfigurationInfo(MetrologyConfiguration metrologyConfiguration) {
        this.id = metrologyConfiguration.getId();
        this.version = metrologyConfiguration.getVersion();
        this.name = metrologyConfiguration.getName();
        this.createTime = metrologyConfiguration.getCreateTime();
        this.modTime = metrologyConfiguration.getModTime();
        this.userName = metrologyConfiguration.getUserName();
    }
    
    public void writeTo(MetrologyConfiguration metrologyConfiguration) {
        metrologyConfiguration.updateName(this.name);
    }
    
}