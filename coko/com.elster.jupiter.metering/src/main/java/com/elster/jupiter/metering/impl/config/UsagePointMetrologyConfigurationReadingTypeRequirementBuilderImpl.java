package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.FullySpecifiedReadingType;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.PartiallySpecifiedReadingType;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeTemplate;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;

class UsagePointMetrologyConfigurationReadingTypeRequirementBuilderImpl extends MetrologyConfigurationReadingTypeRequirementBuilderImpl implements UsagePointMetrologyConfiguration.MetrologyConfigurationReadingTypeRequirementBuilder {
    private final ServerMetrologyConfigurationService metrologyConfigurationService;
    private final UsagePointMetrologyConfigurationImpl metrologyConfiguration;

    private MeterRole meterRole;

    UsagePointMetrologyConfigurationReadingTypeRequirementBuilderImpl(ServerMetrologyConfigurationService metrologyConfigurationService, UsagePointMetrologyConfigurationImpl metrologyConfiguration) {
        super(metrologyConfigurationService, metrologyConfiguration);
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.metrologyConfiguration = metrologyConfiguration;
    }

    @Override
    public UsagePointMetrologyConfiguration.MetrologyConfigurationReadingTypeRequirementBuilder withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public UsagePointMetrologyConfiguration.MetrologyConfigurationReadingTypeRequirementBuilder withMeterRole(MeterRole meterRole) {
        this.meterRole = meterRole;
        return this;
    }

    @Override
    public FullySpecifiedReadingType withReadingType(ReadingType readingType) {
        return addReadingTypeRequirementToMeterRoleReference(super.withReadingType(readingType));
    }

    @Override
    public PartiallySpecifiedReadingType withReadingTypeTemplate(ReadingTypeTemplate readingTypeTemplate) {
        return addReadingTypeRequirementToMeterRoleReference(super.withReadingTypeTemplate(readingTypeTemplate));
    }

    private <T extends ReadingTypeRequirement> T addReadingTypeRequirementToMeterRoleReference(T readingTypeRequirement) {
        UsagePointMetrologyConfigurationRequirementRoleReference reference = this.metrologyConfigurationService.getDataModel()
                .getInstance(UsagePointMetrologyConfigurationRequirementRoleReference.class)
                .init(this.metrologyConfiguration, this.meterRole, readingTypeRequirement);
        this.metrologyConfiguration.addReadingTypeRequirementToMeterRoleReference(reference);
        return readingTypeRequirement;
    }
}
