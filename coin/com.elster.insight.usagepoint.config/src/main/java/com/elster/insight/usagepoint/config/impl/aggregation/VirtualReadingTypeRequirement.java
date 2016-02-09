package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;

import com.elster.insight.usagepoint.config.ReadingTypeRequirement;

import java.util.Collections;
import java.util.List;

/**
 * Models a {@link ReadingTypeRequirement} in the context of
 * one {@link MeterActivation} of the usage point.
 * It contains the {@link Channel}s of the related meter
 * that match the ReadingTypeRequirement.
 * <p>
 * It is capable of selecting the most appropriate Channel
 * to produce values of the target reading type.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-04 (15:24)
 */
public class VirtualReadingTypeRequirement {

    private final ReadingTypeRequirement requirement;
    private final List<Channel> matchingChannels;
    private final ReadingType targetReadingType;
    private final int meterActivationSequenceNumber;
    private Channel preferredChannel;   // Lazy from the list of matching channels and the targetReadingType

    public VirtualReadingTypeRequirement(ReadingTypeRequirement requirement, List<Channel> matchingChannels, ReadingType targetReadingType, int meterActivationSequenceNumber) {
        super();
        this.requirement = requirement;
        this.matchingChannels = Collections.unmodifiableList(matchingChannels);
        this.targetReadingType = targetReadingType;
        this.meterActivationSequenceNumber = meterActivationSequenceNumber;
    }

    public List<Channel> getMatchingChannels() {
        return matchingChannels;
    }

}