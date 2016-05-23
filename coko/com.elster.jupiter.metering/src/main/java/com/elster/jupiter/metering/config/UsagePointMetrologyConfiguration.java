package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.search.SearchablePropertyValue;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

public interface UsagePointMetrologyConfiguration extends MetrologyConfiguration {

    void addMeterRole(MeterRole meterRole);

    void removeMeterRole(MeterRole meterRole);

    List<MeterRole> getMeterRoles();

    List<ReadingTypeRequirement> getRequirements(MeterRole meterRole);

    Optional<MeterRole> getMeterRoleFor(ReadingTypeRequirement readingTypeRequirement);

    MetrologyConfigurationReadingTypeRequirementBuilder newReadingTypeRequirement(String name);

    UsagePointRequirement addUsagePointRequirement(SearchablePropertyValue.ValueBean valueBean);

    void removeUsagePointRequirement(UsagePointRequirement requirement);

    List<UsagePointRequirement> getUsagePointRequirements();

    List<MeterActivation> getMetersForRole(MeterRole meterRole);

    @ProviderType
    interface MetrologyConfigurationReadingTypeRequirementBuilder extends MetrologyConfiguration.MetrologyConfigurationReadingTypeRequirementBuilder {

        MetrologyConfigurationReadingTypeRequirementBuilder withMeterRole(MeterRole meterRole);
    }
}
