package com.elster.insight.usagepoint.config.rest.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.rest.MetrologyConfigurationInfo;

import javax.inject.Inject;
import java.util.stream.Collectors;

public class MetrologyConfigurationInfoFactory {
    private final CustomPropertySetInfoFactory customPropertySetInfoFactory;

    @Inject
    public MetrologyConfigurationInfoFactory(CustomPropertySetInfoFactory customPropertySetInfoFactory) {
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
    }

    public MetrologyConfigurationInfo asInfo(MetrologyConfiguration meterConfiguration) {
        return new MetrologyConfigurationInfo(meterConfiguration);
    }

    public MetrologyConfigurationInfo asDetailedInfo(MetrologyConfiguration meterConfiguration) {
        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo(meterConfiguration);
        info.customPropertySets = meterConfiguration.getCustomPropertySets()
                .stream()
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .map(this.customPropertySetInfoFactory::getGeneralAndPropertiesInfo)
                .collect(Collectors.toList());
        return info;
    }

}