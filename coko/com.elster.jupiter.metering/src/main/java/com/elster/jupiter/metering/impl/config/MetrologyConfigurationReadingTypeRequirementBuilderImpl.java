package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.FullySpecifiedReadingType;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.PartiallySpecifiedReadingType;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;

class MetrologyConfigurationReadingTypeRequirementBuilderImpl implements MetrologyConfiguration.MetrologyConfigurationReadingTypeRequirementBuilder {
    private final ServerMetrologyConfigurationService metrologyConfigurationService;
    private final MetrologyConfigurationImpl metrologyConfiguration;

    private String name;

    MetrologyConfigurationReadingTypeRequirementBuilderImpl(ServerMetrologyConfigurationService metrologyConfigurationService, MetrologyConfigurationImpl metrologyConfiguration, String name) {
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.metrologyConfiguration = metrologyConfiguration;
        this.name = name;
    }

    @Override
    public FullySpecifiedReadingType withReadingType(ReadingType readingType) {
        FullySpecifiedReadingTypeImpl fullySpecifiedReadingType = this.metrologyConfigurationService.getDataModel().getInstance(FullySpecifiedReadingTypeImpl.class)
                .init(this.metrologyConfiguration, this.name, readingType);
        this.metrologyConfiguration.addReadingTypeRequirement(fullySpecifiedReadingType);
        return fullySpecifiedReadingType;
    }

    @Override
    public PartiallySpecifiedReadingType withReadingTypeTemplate(ReadingTypeTemplate readingTypeTemplate) {
        PartiallySpecifiedReadingType partiallySpecifiedReadingType = this.metrologyConfigurationService.getDataModel().getInstance(PartiallySpecifiedReadingTypeImpl.class)
                .init(this.metrologyConfiguration, this.name, readingTypeTemplate);
        this.metrologyConfiguration.addReadingTypeRequirement(partiallySpecifiedReadingType);
        return partiallySpecifiedReadingType;
    }
}
