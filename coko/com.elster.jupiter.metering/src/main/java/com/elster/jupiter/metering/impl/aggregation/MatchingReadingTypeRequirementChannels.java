/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Models all the {@link Channel}s that match the {@link ReadingTypeRequirement}s
 * on the different Meters that have been activated on a UsagPoint.
 * The matches are organized firstly by the meter activation period
 * and then basically lists which Channels match which ReadingTypeRequirement.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-04 (09:34)
 */
class MatchingReadingTypeRequirementChannels {

    private final Range<Instant> range;
    private final Map<ReadingTypeRequirement, List<Channel>> matchingChannels = new HashMap<>();

    MatchingReadingTypeRequirementChannels(Range<Instant> range) {
        super();
        this.range = range;
    }

    Range<Instant> getRange() {
        return this.range;
    }

    /**
     * Adds the specified {@link Channel} as a match for the specified {@link ReadingTypeRequirement}.
     *
     * @param requirement The ReadingTypeRequirement
     * @param channel The Channel
     */
    void addMatch(ReadingTypeRequirement requirement, Channel channel) {
        this.matchingChannels.merge(requirement, Collections.singletonList(channel), this::mergeLists);
    }

    private List<Channel> mergeLists(List<Channel> first, List<Channel> second) {
        List<Channel> merged = new ArrayList<>(first);
        merged.addAll(second);
        return merged;
    }

    /**
     * Returns the List of matching {@link Channel}s that were added so far
     * for the specified {@link ReadingTypeRequirement}.
     *
     * @param requirement The ReadingTypeRequirement
     * @return The List of Channel
     */
    List<Channel> getMatchingChannels(ReadingTypeRequirement requirement) {
        return this.matchingChannels.getOrDefault(requirement, Collections.emptyList());
    }

}