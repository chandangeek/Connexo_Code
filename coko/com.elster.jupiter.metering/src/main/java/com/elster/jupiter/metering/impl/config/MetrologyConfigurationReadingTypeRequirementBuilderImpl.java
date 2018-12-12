/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.PartiallySpecifiedReadingTypeRequirement;
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
    public FullySpecifiedReadingTypeRequirement withReadingType(ReadingType readingType) {
        FullySpecifiedReadingTypeRequirementImpl fullySpecifiedReadingType = this.metrologyConfigurationService.getDataModel().getInstance(FullySpecifiedReadingTypeRequirementImpl.class)
                .init(this.metrologyConfiguration, this.name, readingType);
        this.metrologyConfiguration.addReadingTypeRequirement(fullySpecifiedReadingType);
        return fullySpecifiedReadingType;
    }

    @Override
    public PartiallySpecifiedReadingTypeRequirement withReadingTypeTemplate(ReadingTypeTemplate readingTypeTemplate) {
        PartiallySpecifiedReadingTypeRequirement partiallySpecified = this.metrologyConfigurationService.getDataModel().getInstance(PartiallySpecifiedReadingTypeRequirementImpl.class)
                .init(this.metrologyConfiguration, this.name, readingTypeTemplate);
        this.metrologyConfiguration.addReadingTypeRequirement(partiallySpecified);
        return partiallySpecified;
    }
}
