/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import java.time.Instant;
import java.time.ZoneId;

/**
 * Truncates Instants to {@link IntervalLength}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-08-05 (11:54)
 */
interface InstantTruncater {
    Instant truncate(Instant instant, IntervalLength intervalLength, ZoneId zoneId);
}