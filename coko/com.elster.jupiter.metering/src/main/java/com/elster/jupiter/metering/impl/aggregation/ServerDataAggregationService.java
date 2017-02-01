/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.aggregation.DataAggregationService;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

public interface ServerDataAggregationService extends DataAggregationService {

    List<MeterActivationSet> getMeterActivationSets(UsagePoint usagePoint, Range<Instant> period);

    List<MeterActivationSet> getMeterActivationSets(UsagePoint usagePoint, Instant when);

}