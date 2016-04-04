package com.elster.jupiter.metering.config;

import com.elster.jupiter.search.SearchablePropertyValue;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

public interface UsagePointMetrologyConfiguration extends MetrologyConfiguration {

    void addMeterRole(MeterRole meterRole);

    void removeMeterRole(MeterRole meterRole);

    List<MeterRole> getMeterRoles();

    List<ReadingTypeRequirement> getRequirements(MeterRole meterRole);

    MetrologyConfigurationReadingTypeRequirementBuilder newReadingTypeRequirement(String name);

    UsagePointRequirement addUsagePointRequirement(SearchablePropertyValue.ValueBean valueBean);

    void removeUsagePointRequirement(UsagePointRequirement requirement);

    List<UsagePointRequirement> getUsagePointRequirements();

    @ProviderType
    interface MetrologyConfigurationReadingTypeRequirementBuilder extends MetrologyConfiguration.MetrologyConfigurationReadingTypeRequirementBuilder {

        MetrologyConfigurationReadingTypeRequirementBuilder withMeterRole(MeterRole meterRole);
    }
}
