package com.elster.insight.usagepoint.config.rest;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.metering.config.MetrologyConfiguration;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public void updateCustomPropertySets(MetrologyConfiguration metrologyConfiguration, Function<String, RegisteredCustomPropertySet> rcpsProvider) {
        if (this.customPropertySets != null) {
            Map<String, RegisteredCustomPropertySet> actualCustomPropertySets = metrologyConfiguration.getCustomPropertySets()
                    .stream()
                    .collect(Collectors.toMap(rcps -> rcps.getCustomPropertySet().getId(), Function.identity()));
            this.customPropertySets
                    .stream()
                    .map(cpsInfo -> rcpsProvider.apply(cpsInfo.customPropertySetId))
                    .forEach(rcps -> {
                        if (actualCustomPropertySets.remove(rcps.getCustomPropertySet().getId()) == null){
                            metrologyConfiguration.addCustomPropertySet(rcps);
                        }
                    });
            actualCustomPropertySets.values()
                    .stream()
                    .forEach(metrologyConfiguration::removeCustomPropertySet);
        }
    }
}