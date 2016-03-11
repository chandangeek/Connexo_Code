package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.metering.Channel;

import com.elster.insight.usagepoint.config.ReadingTypeRequirement;
import com.google.common.collect.Range;

import java.time.Instant;

/**
 * Models a match for a {@link ReadingTypeRequirement} and a {@link Channel}
 * provided by a Meter that has been activated on a UsagPoint.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-04 (11:40)
 */
class ReadingTypeRequirementChannelMatch {

    private final Range<Instant> range;
    private final Channel channel;

    ReadingTypeRequirementChannelMatch(Range<Instant> range, Channel channel) {
        super();
        this.range = range;
        this.channel = channel;
    }

    Range<Instant> getRange() {
        return this.range;
    }

    Channel getChannel() {
        return this.channel;
    }

}