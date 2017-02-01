/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.PartiallySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;

class UsagePointMetrologyConfigurationReadingTypeRequirementBuilderImpl extends MetrologyConfigurationReadingTypeRequirementBuilderImpl {
    private final ServerMetrologyConfigurationService metrologyConfigurationService;
    private final UsagePointMetrologyConfigurationImpl metrologyConfiguration;
    private final MeterRole meterRole;

    UsagePointMetrologyConfigurationReadingTypeRequirementBuilderImpl(ServerMetrologyConfigurationService metrologyConfigurationService, UsagePointMetrologyConfigurationImpl metrologyConfiguration, String name, MeterRole role) {
        super(metrologyConfigurationService, metrologyConfiguration, name);
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.metrologyConfiguration = metrologyConfiguration;
        this.meterRole = role;
    }

    @Override
    public FullySpecifiedReadingTypeRequirement withReadingType(ReadingType readingType) {
        return addReadingTypeRequirementToMeterRoleReference(super.withReadingType(readingType));
    }

    @Override
    public PartiallySpecifiedReadingTypeRequirement withReadingTypeTemplate(ReadingTypeTemplate readingTypeTemplate) {
        return addReadingTypeRequirementToMeterRoleReference(super.withReadingTypeTemplate(readingTypeTemplate));
    }

    private <T extends ReadingTypeRequirement> T addReadingTypeRequirementToMeterRoleReference(T readingTypeRequirement) {
        ReadingTypeRequirementMeterRoleUsage reference = this.metrologyConfigurationService.getDataModel()
                .getInstance(ReadingTypeRequirementMeterRoleUsage.class)
                .init(this.meterRole, readingTypeRequirement);
        this.metrologyConfiguration.addReadingTypeRequirementMeterRoleUsage(reference);
        return readingTypeRequirement;
    }

}