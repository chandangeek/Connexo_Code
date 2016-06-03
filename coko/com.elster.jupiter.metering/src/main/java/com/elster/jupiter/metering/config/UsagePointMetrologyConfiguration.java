package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.Pair;

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

    void validateMeterCapabilities(List<Pair<MeterRole, Meter>> meters);

    List<UsagePointRequirement> getUsagePointRequirements();

    @ProviderType
    interface MetrologyConfigurationReadingTypeRequirementBuilder extends MetrologyConfiguration.MetrologyConfigurationReadingTypeRequirementBuilder {

        MetrologyConfigurationReadingTypeRequirementBuilder withMeterRole(MeterRole meterRole);
    }
}
