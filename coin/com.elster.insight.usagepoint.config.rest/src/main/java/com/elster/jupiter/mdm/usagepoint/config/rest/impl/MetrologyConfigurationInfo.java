/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.search.rest.SearchCriteriaVisualizationInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetrologyConfigurationInfo {

    public long id;
    public String name;
    public String description;
    public boolean isGapAllowed;
    public IdWithNameInfo status;
    public IdWithNameInfo serviceCategory;
    public List<IdWithNameInfo> meterRoles;
    public List<IdWithNameInfo> purposes;
    public List<MetrologyContractInfo> metrologyContracts;
    public List<SearchCriteriaVisualizationInfo> usagePointRequirements;
    public List<CustomPropertySetInfo> customPropertySets;
    public long version;

    public void updateCustomPropertySets(MetrologyConfiguration metrologyConfiguration, Function<String, RegisteredCustomPropertySet> rcpsProvider) {
        if (this.customPropertySets != null) {
            Map<String, RegisteredCustomPropertySet> actualCustomPropertySets = metrologyConfiguration.getCustomPropertySets()
                    .stream()
                    .collect(Collectors.toMap(rcps -> rcps.getCustomPropertySet().getId(), Function.identity()));
            this.customPropertySets
                    .stream()
                    .map(cpsInfo -> rcpsProvider.apply(cpsInfo.customPropertySetId))
                    .forEach(rcps -> {
                        if (actualCustomPropertySets.remove(rcps.getCustomPropertySet().getId()) == null) {
                            metrologyConfiguration.addCustomPropertySet(rcps);
                        }
                    });
            actualCustomPropertySets.values()
                    .forEach(metrologyConfiguration::removeCustomPropertySet);
        }
    }
}
