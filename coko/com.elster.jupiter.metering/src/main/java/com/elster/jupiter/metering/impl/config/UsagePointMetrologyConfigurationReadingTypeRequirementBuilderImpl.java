package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.PartiallySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;

class UsagePointMetrologyConfigurationReadingTypeRequirementBuilderImpl extends MetrologyConfigurationReadingTypeRequirementBuilderImpl implements UsagePointMetrologyConfiguration.MetrologyConfigurationReadingTypeRequirementBuilder {
    private final ServerMetrologyConfigurationService metrologyConfigurationService;
    private final UsagePointMetrologyConfigurationImpl metrologyConfiguration;

    private MeterRole meterRole;

    UsagePointMetrologyConfigurationReadingTypeRequirementBuilderImpl(ServerMetrologyConfigurationService metrologyConfigurationService, UsagePointMetrologyConfigurationImpl metrologyConfiguration, String name) {
        super(metrologyConfigurationService, metrologyConfiguration, name);
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.metrologyConfiguration = metrologyConfiguration;
    }

    @Override
    public UsagePointMetrologyConfiguration.MetrologyConfigurationReadingTypeRequirementBuilder withMeterRole(MeterRole meterRole) {
        this.meterRole = meterRole;
        return this;
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
