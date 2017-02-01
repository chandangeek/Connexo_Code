/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.time.DayMonthTime;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

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
@ProviderType
public interface GasDayOptions {

    enum RelativePeriodTranslationKey implements TranslationKey {
        LAST_7_DAYS("gas.relative.period.lastSevenDays", "Last 7 days (gas)"),
        PREVIOUS_MONTH("gas.relative.period.previouMonth", "Previous month (gas)"),
        THIS_MONTH("gas.relative.period.thisMonth", "This month (gas)"),
        PREVIOUS_WEEK("gas.relative.period.PreviousWeek", "Previous week (gas)"),
        THIS_WEEK("gas.relative.period.thisWeek", "This week (gas)"),
        YESTERDAY("gas.relative.period.yesterday", "Yesterday (gas)"),
        TODAY("gas.relative.period.today", "Today (gas)"),
        THIS_YEAR("gas.relative.period.thisYear", "This year (gas)");

        private final String id;
        private final String defaultFormat;

        RelativePeriodTranslationKey(String id, String defaultFormat) {
            this.id = id;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return this.id;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }
    }

    /**
     * Get the start of a gas year as it was configured at system installation time.
     *
     * @return The start of a gas year
     */
    DayMonthTime getYearStart();

    /**
     * Gets the List of {@link RelativePeriod}s that relate to the gas market
     * and rely on these GasDayOptions.
     *
     * @return The List of RelativePeriod
     */
    List<RelativePeriod> getRelativePeriods();

}