package com.elster.insight.usagepoint.config.rest;

import java.time.Instant;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;

@XmlRootElement
public class MetrologyConfigurationInfo {

    public long id;
    public String name;
    public boolean active;
    public List<CustomPropertySetInfo> customPropertySets;

    public long version;
    public Instant createTime;
    public Instant modTime;
    public String userName;

    public MetrologyConfigurationInfo() {
    }

    public MetrologyConfigurationInfo(MetrologyConfiguration metrologyConfiguration) {
        this.id = metrologyConfiguration.getId();
        this.active = metrologyConfiguration.isActive();
        this.name = metrologyConfiguration.getName();
        this.version = metrologyConfiguration.getVersion();
        this.createTime = metrologyConfiguration.getCreateTime();
        this.modTime = metrologyConfiguration.getModTime();
        this.userName = metrologyConfiguration.getUserName();
    }
    
    public void writeTo(MetrologyConfiguration metrologyConfiguration) {
        metrologyConfiguration.updateName(this.name);
    }
    
}