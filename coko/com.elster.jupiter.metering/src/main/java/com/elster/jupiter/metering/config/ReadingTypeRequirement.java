/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.units.Dimension;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Set;
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

    long getVersion();

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
     * @param channelsContainer The ChannelsContainer
     * @return The List of matching ReadingTypes
     */
    default List<ReadingType> getMatchesFor(ChannelsContainer channelsContainer) {
        return this.getMatchingChannelsFor(channelsContainer)
                .stream()
                .map(Channel::getMainReadingType)
                .collect(Collectors.toList());
    }

    /**
     * Return the List of {@link Channel}s from the specified
     * {@link MeterActivation} that can provide data for this requirement.
     *
     * @param channelsContainer The ChannelsContainer
     * @return The List of matching ReadingTypes
     */
    List<Channel> getMatchingChannelsFor(ChannelsContainer channelsContainer);

    /**
     * <p>
     * For {@link FullySpecifiedReadingTypeRequirement} checks that candidate is the same reading type as
     * {@link FullySpecifiedReadingTypeRequirement#getReadingType()}
     * </p>
     * <p>
     * For {@link PartiallySpecifiedReadingTypeRequirement} checks that each candidate's attribute:
     * <ul>
     * <li>is equal to overridden attribute value (if it was overridden,
     * see {@link PartiallySpecifiedReadingTypeRequirement#overrideAttribute(ReadingTypeTemplateAttributeName, int)})</li>
     * <li>or is equal to template attribute value (if attribute has code or possible values,
     * see {@link ReadingTypeTemplateAttribute#matches(ReadingType)})</li>
     * <li>or has one of system allowed values, see
     * {@link ReadingTypeTemplateAttributeName.ReadingTypeAttribute#getPossibleValues()}</li>
     * </ul>
     * </p>
     *
     * @param readingType reading type for check
     * @return <code>true</code> if all attributes are within limits
     */
    boolean matches(ReadingType readingType);

    /**
     * Returns the {@link Dimension} that is represented by this ReadingTypeRequirement.
     *
     * @return The Dimension
     */
    Dimension getDimension();

    boolean isRegular();

    Set<ReadingTypeUnit> getUnits();

    int getTou();

    String getDescription();

}