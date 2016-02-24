package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

/**
 * Produces {@link VirtualReadingTypeRequirement}s and {@link VirtualReadingTypeDeliverable}s,
 * making sure that they are produced only once per {@link MeterActivation}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (08:38)
 */
public interface VirtualFactory {

    /**
     * Returns a new {@link VirtualReadingTypeRequirement} for the specified
     * {@link ReadingTypeRequirement}, {@link ReadingTypeDeliverable} and interval length
     * or one that was already produced before if such a node has already
     * been produced before.
     *
     * @param requirement The ReadingTypeRequirement
     * @param deliverable The ReadingTypeDeliverable
     * @param intervalLength The IntervalLength
     * @return The VirtualRequirementNode
     */
    VirtualReadingTypeRequirement requirementFor(ReadingTypeRequirement requirement, ReadingTypeDeliverable deliverable, IntervalLength intervalLength);

    /**
     * Returns a new {@link VirtualDeliverableNode} for the specified
     * {@link ReadingTypeDeliverableForMeterActivation} and interval length
     * or one that was already produced before if such a node has already
     * been produced before.
     *
     * @param deliverable The ReadingTypeDeliverable
     * @param intervalLength The IntervalLength
     * @return The VirtualDeliverableNode
     */
    VirtualReadingTypeDeliverable deliverableFor(ReadingTypeDeliverableForMeterActivation deliverable, IntervalLength intervalLength);

    /**
     * Returns all the {@link VirtualReadingTypeRequirement}s that were produced by this factory.
     *
     * @return The List of all VirtualReadingTypeRequirement
     */
    List<VirtualReadingTypeRequirement> allRequirements();

    /**
     * Returns all the {@link VirtualReadingTypeDeliverable}s that were produced by this factory.
     *
     * @return The List of all VirtualReadingTypeDeliverable
     */
    List<VirtualReadingTypeDeliverable> allDeliverables();

    /**
     * Notifies this VirtualNodeFactory that the processing of
     * the next {@link MeterActivation} has started.
     *
     * @param meterActivation The next MeterActivation
     * @param requestedPeriod The complete period that was requested
     */
    void nextMeterActivation(MeterActivation meterActivation, Range<Instant> requestedPeriod);

    /**
     * Returns the sequence number of the current {@link MeterActivation}.
     * Note that there is no MeterActivation by default
     * so you will need to call {@link #nextMeterActivation(MeterActivation, Range)}
     * at least once otherwise this method will return 0 (zero).
     *
     * @return The sequence number of the current MeterActivation
     */
    int meterActivationSequenceNumber();

}