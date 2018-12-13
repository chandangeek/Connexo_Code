/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.kpi.KpiMember;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

interface IKpiMember extends KpiMember {

    boolean hasTimeSeries();

    TimeSeries getTimeSeries();

    void setTimeSeries(TimeSeries timeSeries);

    Range<Instant> addScores(TimeSeriesDataStorer storer, Map<Instant, BigDecimal> scores);

    void checkKpiScores(Range<Instant> range);
}
