/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.Pair;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface UsagePointMetrologyConfiguration extends MetrologyConfiguration {

    void addMeterRole(MeterRole meterRole);

    void removeMeterRole(MeterRole meterRole);

    List<MeterRole> getMeterRoles();

    Optional<MeterRole> getMeterRoleFor(ReadingTypeRequirement readingTypeRequirement);

    List<ReadingTypeRequirement> getRequirements(MeterRole meterRole);

    void validateMeterCapabilities(List<Pair<MeterRole, Meter>> meters);

    /**
     * Tests if this UsagePointMetrologyConfiguration requires that
     * a {@link com.elster.jupiter.calendar.Calendar} is added
     * to the {@link com.elster.jupiter.metering.UsagePoint}s
     * to which this configuration is applied.
     *
     * @return <code>true</code> iff a Calendar will need to be added to a UsagePoint once this configuration is applied to it
     */
    boolean requiresCalendarOnUsagePoint();

    /**
     * Will throw an UnsupportedOperationException because a {@link MeterRole} is required
     * when creating a {@link ReadingTypeRequirement} on a UsagePointMetrologyConfiguration.
     *
     * @see #newReadingTypeRequirement(String, MeterRole)
     */
    @Override
    MetrologyConfigurationReadingTypeRequirementBuilder newReadingTypeRequirement(String name);

    MetrologyConfigurationReadingTypeRequirementBuilder newReadingTypeRequirement(String name, MeterRole role);

    List<UsagePointRequirement> getUsagePointRequirements();

    UsagePointRequirement addUsagePointRequirement(SearchablePropertyValue.ValueBean valueBean);

    void removeUsagePointRequirement(UsagePointRequirement requirement);

}