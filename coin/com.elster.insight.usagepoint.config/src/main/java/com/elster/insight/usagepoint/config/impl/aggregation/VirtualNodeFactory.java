package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.metering.MeterActivation;

import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;
import com.elster.insight.usagepoint.config.ReadingTypeRequirement;

import java.time.temporal.TemporalAmount;
import java.util.List;

/**
 * Produces {@link VirtualReadingTypeRequirement}s and {@link VirtualDeliverableNode}s,
 * making sure that they are produced only once per {@link MeterActivation}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (08:38)
 */
interface VirtualNodeFactory {

    /**
     * Returns a new {@link VirtualReadingTypeRequirement} for the specified
     * {@link ReadingTypeRequirement}, {@link ReadingTypeDeliverable} and interval length
     * or one that was already produced before if such a node has already
     * been produced before.
     *
     * @param requirement The ReadingTypeRequirement
     * @param deliverable The ReadingTypeDeliverable
     * @param intervalLength The TemporalAmount
     * @return The VirtualRequirementNode
     */
    VirtualReadingTypeRequirement requirementNodeFor(ReadingTypeRequirement requirement, ReadingTypeDeliverable deliverable, TemporalAmount intervalLength);

    /**
     * Returns a new {@link VirtualDeliverableNode} for the specified
     * {@link ReadingTypeDeliverable} and interval length
     * or one that was already produced before if such a node has already
     * been produced before.
     *
     * @param deliverable The ReadingTypeDeliverable
     * @param intervalLength The TemporalAmount
     * @return The VirtualDeliverableNode
     */
    VirtualDeliverableNode deliverableNodeFor(ReadingTypeDeliverable deliverable, TemporalAmount intervalLength);

    /**
     * Returns all the {@link VirtualReadingTypeRequirement}s that were produced by this factory.
     *
     * @return The List of all VirtualRequirementNode
     */
    List<VirtualReadingTypeRequirement> allNodes();

    /**
     * Notifies this VirtualNodeFactory that the processing of
     * the next {@link MeterActivation} has started.
     *
     * @param meterActivation The next MeterActivation
     */
    void nextMeterActivation(MeterActivation meterActivation);

    /**
     * Returns the sequence number of the current {@link MeterActivation}.
     * Note that there is no MeterActivation by default
     * so you will need to call {@link #nextMeterActivation(MeterActivation)}
     * at least once otherwise this method will return 0 (zero).
     *
     * @return The sequence number of the current MeterActivation
     */
    int meterActivationSequenceNumber();

}