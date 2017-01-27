package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.mdm.usagepoint.config.rest.ReadingTypeDeliverableFactory;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationStatus;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.search.rest.SearchCriteriaVisualizationInfo;

import javax.inject.Inject;
import java.util.stream.Collectors;

public class MetrologyConfigurationInfoFactory {

    private final CustomPropertySetInfoFactory customPropertySetInfoFactory;
    private final Thesaurus thesaurus;
    private final ReadingTypeDeliverableFactory readingTypeDeliverableFactory;

    @Inject
    public MetrologyConfigurationInfoFactory(Thesaurus thesaurus, CustomPropertySetInfoFactory customPropertySetInfoFactory, ReadingTypeDeliverableFactory readingTypeDeliverableFactory) {
        this.thesaurus = thesaurus;
        this.customPropertySetInfoFactory = customPropertySetInfoFactory;
        this.readingTypeDeliverableFactory = readingTypeDeliverableFactory;
    }

    public MetrologyConfigurationInfo asInfo(UsagePointMetrologyConfiguration metrologyConfiguration) {
        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo();
        info.id = metrologyConfiguration.getId();
        info.name = metrologyConfiguration.getName();
        info.description = metrologyConfiguration.getDescription();
        info.status = asInfo(metrologyConfiguration.getStatus());
        info.serviceCategory = asInfo(metrologyConfiguration.getServiceCategory());
        info.version = metrologyConfiguration.getVersion();
        info.meterRoles = metrologyConfiguration.getMeterRoles().stream().map(this::asInfo).collect(Collectors.toList());
        info.purposes = metrologyConfiguration.getContracts()
                .stream()
                .sorted((a, b) -> Boolean.compare(a.isMandatory(), b.isMandatory()))
                .sorted((a, b) -> a.getMetrologyPurpose().getName().compareTo(b.getMetrologyPurpose().getName()))
                .map(this::asInfo)
                .collect(Collectors.toList());
        info.usagePointRequirements = metrologyConfiguration.getUsagePointRequirements()
                .stream()
                .map(requirement -> SearchCriteriaVisualizationInfo.from(requirement.getSearchableProperty(), requirement.toValueBean()))
                .collect(Collectors.toList());
        return info;
    }

    public MetrologyConfigurationInfo asDetailedInfo(UsagePointMetrologyConfiguration metrologyConfiguration) {
        MetrologyConfigurationInfo info = asInfo(metrologyConfiguration);
        info.metrologyContracts = metrologyConfiguration.getContracts()
                .stream()
                .sorted((a, b) -> Boolean.compare(a.isMandatory(), b.isMandatory()))
                .sorted((a, b) -> a.getMetrologyPurpose().getName().compareTo(b.getMetrologyPurpose().getName()))
                .map(this::asDetailedInfo).collect(Collectors.toList());
        info.customPropertySets = metrologyConfiguration.getCustomPropertySets()
                .stream()
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .map(this.customPropertySetInfoFactory::getGeneralAndPropertiesInfo)
                .collect(Collectors.toList());
        return info;
    }

    private IdWithNameInfo asInfo(MetrologyConfigurationStatus status) {
        IdWithNameInfo info = new IdWithNameInfo();
        info.id = status.getId();
        info.name = thesaurus.getFormat(status.getTranslationKey()).format();
        return info;
    }

    private IdWithNameInfo asInfo(ServiceCategory serviceCategory) {
        IdWithNameInfo info = new IdWithNameInfo();
        info.id = serviceCategory.getKind().name();
        info.name = serviceCategory.getName();
        return info;
    }

    private IdWithNameInfo asInfo(MeterRole meterRole) {
        IdWithNameInfo info = new IdWithNameInfo();
        info.id = meterRole.getKey();
        info.name = meterRole.getDisplayName();
        return info;
    }

    private IdWithNameInfo asInfo(MetrologyContract metrologyContract) {
        IdWithNameInfo info = new IdWithNameInfo();
        info.id = metrologyContract.getId();
        info.name = metrologyContract.getMetrologyPurpose().getName();
        return info;
    }

    private MetrologyContractInfo asDetailedInfo(MetrologyContract metrologyContract) {
        MetrologyContractInfo info = new MetrologyContractInfo();
        info.id = metrologyContract.getId();
        info.name = metrologyContract.getMetrologyPurpose().getName();
        info.description = metrologyContract.getMetrologyPurpose().getDescription();
        info.mandatory = metrologyContract.isMandatory();
        info.readingTypeDeliverables = metrologyContract.getDeliverables()
                .stream()
                .map(deliverable -> readingTypeDeliverableFactory.asInfo(deliverable, metrologyContract.getMetrologyConfiguration()))
                .collect(Collectors.toList());
        return info;
    }
}