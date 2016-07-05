package com.elster.jupiter.metering.config;

import com.elster.jupiter.search.SearchablePropertyValue;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface UsagePointMetrologyConfiguration extends MetrologyConfiguration {

    void addMeterRole(MeterRole meterRole);

    void removeMeterRole(MeterRole meterRole);

    List<MeterRole> getMeterRoles();

    List<ReadingTypeRequirement> getRequirements(MeterRole meterRole);

    Optional<MeterRole> getMeterRoleFor(ReadingTypeRequirement readingTypeRequirement);

    /**
     * Will throw an UnsupportedOperationException because a {@link MeterRole} is required
     * when creating a {@link ReadingTypeRequirement} on a UsagePointMetrologyConfiguration.
     *
     * @see #newReadingTypeRequirement(String, MeterRole)
     */
    @Override
    MetrologyConfigurationReadingTypeRequirementBuilder newReadingTypeRequirement(String name);

    MetrologyConfigurationReadingTypeRequirementBuilder newReadingTypeRequirement(String name, MeterRole role);

    UsagePointRequirement addUsagePointRequirement(SearchablePropertyValue.ValueBean valueBean);

    void removeUsagePointRequirement(UsagePointRequirement requirement);

    List<UsagePointRequirement> getUsagePointRequirements();

}