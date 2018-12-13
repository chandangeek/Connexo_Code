/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.ReadingType;

import java.time.temporal.TemporalAmount;

/**
 * Produces TemporalAmounts from the information contained in a {@link ReadingType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (15:14)
 */
interface TemporalAmountFactory {

    TemporalAmount from(ReadingType readingType);

}