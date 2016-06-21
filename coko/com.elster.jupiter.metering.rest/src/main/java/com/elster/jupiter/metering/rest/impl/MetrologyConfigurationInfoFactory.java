package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationStatus;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import javax.inject.Inject;
import java.util.stream.Collectors;

public class MetrologyConfigurationInfoFactory {

    private final Thesaurus thesaurus;
    private final ReadingTypeInfoFactory readingTypeInfoFactory;

    @Inject
    public MetrologyConfigurationInfoFactory(Thesaurus thesaurus, ReadingTypeInfoFactory readingTypeInfoFactory) {
        this.thesaurus = thesaurus;
        this.readingTypeInfoFactory = readingTypeInfoFactory;
    }

    public MetrologyConfigurationInfo asInfo(UsagePointMetrologyConfiguration metrologyConfiguration) {
        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo();
        info.id = metrologyConfiguration.getId();
        info.name = metrologyConfiguration.getName();
        info.description = metrologyConfiguration.getDescription();
        info.status = asInfo(metrologyConfiguration.getStatus());
        info.serviceCategory = asInfo(metrologyConfiguration.getServiceCategory());
        info.version = metrologyConfiguration.getVersion();
        info.readingTypes = metrologyConfiguration.getDeliverables().stream().map(ReadingTypeDeliverable::getReadingType).map(readingTypeInfoFactory::from).collect(Collectors.toList());
        return info;
    }

    public MetrologyConfigurationInfo asShortInfo(UsagePointMetrologyConfiguration metrologyConfiguration) {
        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo();
        info.id = metrologyConfiguration.getId();
        info.name = metrologyConfiguration.getName();
        info.version = metrologyConfiguration.getVersion();
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

    public EffectiveMetrologyConfigurationOnUsagePointInfo asInfo(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration) {
        EffectiveMetrologyConfigurationOnUsagePointInfo info = new EffectiveMetrologyConfigurationOnUsagePointInfo();
        UsagePointMetrologyConfiguration config = effectiveMetrologyConfiguration.getMetrologyConfiguration();
        info.metrologyConfiguration = asInfo(config);
        info.start = effectiveMetrologyConfiguration.getStart() == null
                ? null
                : effectiveMetrologyConfiguration.getStart().toEpochMilli();
        info.end = effectiveMetrologyConfiguration.getEnd() == null
                ? null
                : effectiveMetrologyConfiguration.getEnd().toEpochMilli();
       return info;
    }
}
