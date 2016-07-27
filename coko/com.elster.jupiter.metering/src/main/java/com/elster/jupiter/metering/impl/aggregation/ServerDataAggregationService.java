package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.DataAggregationService;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.stream.Stream;

public interface ServerDataAggregationService extends DataAggregationService {

    Stream<MeterActivationSet> getMeterActivationSets(UsagePoint usagePoint, Range<Instant> period);

    Stream<MeterActivationSet> getMeterActivationSets(UsagePoint usagePoint, Instant when);
}
