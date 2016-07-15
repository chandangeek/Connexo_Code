/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.util.time.DayMonthTime;

/**
 * Models the options for the gas day feature.
 * These options are typically defined at system installation time and
 * are not likely to change because of the impact it has on the market.
 * This is intended to be a singleton, i.e. it is expected that there
 * is only one entity in the system.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-07-15 (12:18)
 */
public interface GasDayOptions {
    /**
     * Get the start of a gas year as it was configured at system installation time.
     *
     * @return The start of a gas year
     */
    DayMonthTime getYearStart();
}