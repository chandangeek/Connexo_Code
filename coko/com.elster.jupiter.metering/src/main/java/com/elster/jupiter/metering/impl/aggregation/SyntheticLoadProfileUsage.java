/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.slp.SyntheticLoadProfile;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Models the usage of {@link SyntheticLoadProfile}s from custom properties
 * within the same time period.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-13 (16:41)
 */
class SyntheticLoadProfileUsage {
    private final Range<Instant> range;
    private final Map<String, SyntheticLoadProfile> syntheticLoadProfiles;

    private SyntheticLoadProfileUsage(Range<Instant> range, Map<String, SyntheticLoadProfile> syntheticLoadProfiles) {
        this.range = range;
        this.syntheticLoadProfiles = syntheticLoadProfiles;
    }

    Range<Instant> getRange() {
        return range;
    }

    Set<String> getSyntheticLoadProfilePropertyNames() {
        return this.syntheticLoadProfiles.keySet();
    }

    SyntheticLoadProfile getSyntheticLoadProfile(String name) {
        return this.syntheticLoadProfiles.get(name);
    }

    static Builder builder(Range<Instant> period) {
        return new Builder(period);
    }

    static class Builder {
        private Range<Instant> range;
        private final Map<String, SyntheticLoadProfile> syntheticLoadProfiles = new HashMap<>();

        Builder(Range<Instant> range) {
            this.range = range;
        }

        void setRange(Range<Instant> range) {
            this.range = range.intersection(this.range);
        }

        public void add(String propertySpecName, SyntheticLoadProfile syntheticLoadProfile) {
            this.syntheticLoadProfiles.put(propertySpecName, syntheticLoadProfile);
        }


        public Optional<SyntheticLoadProfileUsage> build() {
            if (this.syntheticLoadProfiles.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(new SyntheticLoadProfileUsage(this.range, this.syntheticLoadProfiles));
            }
        }
    }
}