package com.elster.insight.usagepoint.config;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Models a requirement that a Meter should provide measurement
 * data for a {@link com.elster.jupiter.metering.ReadingType}.
 * This requirement can be absolute, i.e. all the details of
 * the ReadingType are specified or the requirement can be
 * partially specified, i.e. some details of the ReadingType have wildcards.
 * The latter is especially interesting if the requirement
 * is for Wh measurement data but you don't care about the
 * data collection frequency (15min, 30min, hourly,...)
 * or about the multiplier (Wh, kWh, MWH,...).
 * <p>
 * A ReadingTypeRequirement has a name so that it can referenced
 * in a {@link ReadingTypeDeliverable}'s formula.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-04 (08:53)
 */
@ProviderType
public interface ReadingTypeRequirement extends HasId, HasName {

    /**
     * Returns the {@link MetrologyConfiguration} that defined
     * this ReadingTypeRequirement.
     *
     * @return The MetrologyConfiguration
     */
    MetrologyConfiguration getMetrologyConfiguration();

    /**
     * Return the List of {@link ReadingType}s from the
     * specified {@link MeterActivation} that match this requirement.
     *
     * @param meterActivation The MeterActivation
     * @return The List of matching ReadingTypes
     */
    default List<ReadingType> getMatchesFor(MeterActivation meterActivation) {
        return this.getMatchingChannelsFor(meterActivation)
                .stream()
                .map(Channel::getMainReadingType)
                .collect(Collectors.toList());
    }

    /**
     * Return the List of {@link Channel}s from the specified
     * {@link MeterActivation} that can provide data for this requirement.
     *
     * @param meterActivation The MeterActivation
     * @return The List of matching ReadingTypes
     */
    List<Channel> getMatchingChannelsFor(MeterActivation meterActivation);

}