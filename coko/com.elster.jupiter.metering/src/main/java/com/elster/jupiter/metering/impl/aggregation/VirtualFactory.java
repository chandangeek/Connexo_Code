/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

/**
 * Produces {@link VirtualReadingTypeRequirement}s, making sure
 * that they are produced only once per {@link MeterActivation}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (08:38)
 */
public interface VirtualFactory {

    /**
     * Returns a new {@link VirtualReadingTypeRequirement} for the specified
     * {@link ReadingTypeRequirement}, {@link ReadingTypeDeliverable} and
     * {@link VirtualReadingType} or one that was already produced before
     * if such a node has already been produced before.
     *
     * @param mode The Formula.Mode
     * @param requirement The ReadingTypeRequirement
     * @param deliverable The ReadingTypeDeliverable
     * @param readingType The VirtualReadingType
     * @return The VirtualRequirementNode
     */
    VirtualReadingTypeRequirement requirementFor(Formula.Mode mode, ReadingTypeRequirement requirement, ReadingTypeDeliverable deliverable, VirtualReadingType readingType);

    /**
     * Returns all the {@link VirtualReadingTypeRequirement}s that were produced by this factory.
     *
     * @return The List of all VirtualReadingTypeRequirement
     */
    List<VirtualReadingTypeRequirement> allRequirements();

    /**
     * Notifies this VirtualNodeFactory that the processing of
     * the next {@link MeterActivationSet} has started.
     *
     * @param meterActivationSet The next MeterActivationSet
     * @param requestedPeriod The complete period that was requested
     */
    void nextMeterActivationSet(MeterActivationSet meterActivationSet, Range<Instant> requestedPeriod);

    /**
     * Returns the sequence number of the current {@link MeterActivationSet}.
     * Note that there is no MeterActivationSet by default
     * so you will need to call {@link #nextMeterActivationSet(MeterActivationSet, Range)}
     * at least once otherwise this method will return 0 (zero).
     *
     * @return The sequence number of the current MeterActivationSet
     */
    int sequenceNumber();

}