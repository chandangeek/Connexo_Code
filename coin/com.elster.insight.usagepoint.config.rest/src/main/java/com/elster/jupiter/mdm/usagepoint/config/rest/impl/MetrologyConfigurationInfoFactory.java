package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import javax.inject.Inject;
import java.util.Collections;
import java.util.stream.Collectors;

public class MetrologyConfigurationInfoFactory {

    private final CustomPropertySetInfoFactory customPropertySetInfoFactory;
    private final Thesaurus thesaurus;

    @Inject
    public MetrologyConfigurationInfoFactory(Thesaurus thesaurus, CustomPropertySetInfoFactory customPropertySetInfoFactory) {
        this.thesaurus = thesaurus;
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
    }

    public MetrologyConfigurationInfo asInfo(MetrologyConfiguration metrologyConfiguration) {
        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo();
        info.id = metrologyConfiguration.getId();
        info.name = metrologyConfiguration.getName();
        info.description = metrologyConfiguration.getDescription();
        info.status = asInfo(metrologyConfiguration.getStatus());
        info.serviceCategory = asInfo(metrologyConfiguration.getServiceCategory());
        info.version = metrologyConfiguration.getVersion();
        //TODO init
        info.meterRoles = Collections.emptyList();
        info.purposes = Collections.emptyList();
        info.usagePointCriteria = "";
        return info;
    }

    public MetrologyConfigurationInfo asDetailedInfo(MetrologyConfiguration meterConfiguration) {
        MetrologyConfigurationInfo info = asInfo(meterConfiguration);
        info.customPropertySets = meterConfiguration.getCustomPropertySets()
                .stream()
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .map(this.customPropertySetInfoFactory::getGeneralAndPropertiesInfo)
                .collect(Collectors.toList());
        return info;
    }

    private IdWithNameInfo asInfo(MetrologyConfigurationStatus status) {
        IdWithNameInfo info = new IdWithNameInfo();
        info.id = status.getId();
        info.name = thesaurus.getFormat(MetrologyConfigurationStatusTranslationKeys.getTranslatedName(status)).format();
        return info;
    }

    private IdWithNameInfo asInfo(ServiceCategory serviceCategory) {
        IdWithNameInfo info = new IdWithNameInfo();
        info.id = serviceCategory.getKind().name();
        info.name = serviceCategory.getName();
        return info;
    }
}