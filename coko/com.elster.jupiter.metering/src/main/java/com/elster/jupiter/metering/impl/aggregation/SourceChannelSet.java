package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.util.streams.Functions;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class SourceChannelSet {

    private final MeteringService meteringService;

    private Set<Long> sourceChannelIds;

    SourceChannelSet(MeteringService meteringService, Set<Long> sourceChannelIds) {
        this.meteringService = meteringService;
        this.sourceChannelIds = sourceChannelIds;
    }

    Set<Long> getSourceChannelIds() {
        return Collections.unmodifiableSet(this.sourceChannelIds);
    }

    public Stream<ReadingQualityRecord> fetchReadingQualities(Range<Instant> timePeriod) {
        return getSourceChannelIds().stream()
                .map(meteringService::findChannel)
                .flatMap(Functions.asStream())
                .flatMap(channel -> fetchReadingQualities(channel, timePeriod).stream());
    }

    private List<ReadingQualityRecord> fetchReadingQualities(Channel channel, Range<Instant> timePeriod) {
        return channel.findReadingQualities()
                .actual()
                .inTimeInterval(timePeriod)
                .collect();
    }
}
