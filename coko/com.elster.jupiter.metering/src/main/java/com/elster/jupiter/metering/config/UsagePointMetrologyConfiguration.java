package com.elster.jupiter.metering.config;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

public interface UsagePointMetrologyConfiguration extends MetrologyConfiguration {

    void addMeterRole(MeterRole meterRole);

    void removeMeterRole(MeterRole meterRole);

    List<MeterRole> getMeterRoles();

    MetrologyConfigurationReadingTypeRequirementBuilder addReadingTypeRequirement(String name);

    @ProviderType
    interface MetrologyConfigurationReadingTypeRequirementBuilder extends MetrologyConfiguration.MetrologyConfigurationReadingTypeRequirementBuilder {

        @Override
        MetrologyConfigurationReadingTypeRequirementBuilder withName(String name);

        MetrologyConfigurationReadingTypeRequirementBuilder withMeterRole(MeterRole meterRole);
    }
}
