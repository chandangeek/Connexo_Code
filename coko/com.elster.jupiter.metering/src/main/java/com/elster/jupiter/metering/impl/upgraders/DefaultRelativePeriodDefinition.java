/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.metering.GasDayOptions;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativeOperation;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;

import java.time.DayOfWeek;
import java.time.temporal.WeekFields;
import java.util.Collections;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.time.RelativeField.DAY;
import static com.elster.jupiter.time.RelativeField.DAY_OF_WEEK;
import static com.elster.jupiter.time.RelativeField.HOUR;
import static com.elster.jupiter.time.RelativeField.MINUTES;
import static com.elster.jupiter.time.RelativeField.MONTH;
import static com.elster.jupiter.time.RelativeField.SECONDS;
import static com.elster.jupiter.time.RelativeField.WEEK;

public enum DefaultRelativePeriodDefinition {
    LAST_7_DAYS(GasDayOptions.RelativePeriodTranslationKey.LAST_7_DAYS) {
        @Override
        protected RelativeDate fromWith(GasDayOptions gasDayOptions) {
            return new RelativeDate(
                    Stream
                            .concat(
                                    Stream.of(DAY.minus(7)),
                                    startOfDayOperations(gasDayOptions))
                            .collect(Collectors.toList()));
        }

        @Override
        protected RelativeDate toWith(GasDayOptions gasDayOptions) {
            return atStartOfDay(gasDayOptions);
        }
    },
    PREVIOUS_MONTH(GasDayOptions.RelativePeriodTranslationKey.PREVIOUS_MONTH) {
        @Override
        protected RelativeDate fromWith(GasDayOptions gasDayOptions) {
            return atStartOfDay(
                    gasDayOptions,
                    MONTH.minus(1),
                    HOUR.equalTo(gasDayOptions.getYearStart().getHour()),
                    DAY.equalTo(1));
        }

        @Override
        protected RelativeDate toWith(GasDayOptions gasDayOptions) {
            return atStartOfDay(gasDayOptions, DAY.equalTo(1));
        }
    },
    THIS_MONTH(GasDayOptions.RelativePeriodTranslationKey.THIS_MONTH) {
        @Override
        protected RelativeDate fromWith(GasDayOptions gasDayOptions) {
            return atStartOfDay(gasDayOptions, MONTH.minus(0), DAY.equalTo(1));
        }

        @Override
        protected RelativeDate toWith(GasDayOptions gasDayOptions) {
            return atStartOfDay(gasDayOptions, DAY.plus(1));
        }
    },
    PREVIOUS_WEEK(GasDayOptions.RelativePeriodTranslationKey.PREVIOUS_WEEK) {
        @Override
        protected RelativeDate fromWith(GasDayOptions gasDayOptions) {
            return onFirstDayOfWeek(gasDayOptions, WEEK.minus(1));
        }

        @Override
        protected RelativeDate toWith(GasDayOptions gasDayOptions) {
            return onFirstDayOfWeek(gasDayOptions);
        }
    },
    THIS_WEEK(GasDayOptions.RelativePeriodTranslationKey.THIS_WEEK) {
        @Override
        protected RelativeDate fromWith(GasDayOptions gasDayOptions) {
            return onFirstDayOfWeek(gasDayOptions, WEEK.minus(0));
        }

        @Override
        protected RelativeDate toWith(GasDayOptions gasDayOptions) {
            return atStartOfDay(gasDayOptions, DAY.plus(1));
        }
    },
    YESTERDAY(GasDayOptions.RelativePeriodTranslationKey.YESTERDAY) {
        @Override
        protected RelativeDate fromWith(GasDayOptions gasDayOptions) {
            return atStartOfDay(gasDayOptions, DAY.minus(1));
        }

        @Override
        protected RelativeDate toWith(GasDayOptions gasDayOptions) {
            return atStartOfDay(gasDayOptions);
        }
    },
    TODAY(GasDayOptions.RelativePeriodTranslationKey.TODAY) {
        @Override
        protected RelativeDate fromWith(GasDayOptions gasDayOptions) {
            return atStartOfDay(gasDayOptions);
        }

        @Override
        protected RelativeDate toWith(GasDayOptions gasDayOptions) {
            return atStartOfDay(gasDayOptions, DAY.plus(1));
        }
    },
    THIS_YEAR(GasDayOptions.RelativePeriodTranslationKey.THIS_YEAR) {
        @Override
        protected RelativeDate fromWith(GasDayOptions gasDayOptions) {
            return new RelativeDate(
                    Stream
                            .concat(
                                    Stream.of(
                                            HOUR.minus(gasDayOptions.getYearStart().getHour()),
                                            MONTH.minus(gasDayOptions.getYearStart().getMonthValue() - 1),
                                            MONTH.equalTo(gasDayOptions.getYearStart().getMonthValue()),
                                            DAY.equalTo(1)),
                                    startOfDayOperations(gasDayOptions))
                            .collect(Collectors.toList()));
        }

        @Override
        protected RelativeDate toWith(GasDayOptions gasDayOptions) {
            return atStartOfDay(gasDayOptions, DAY.plus(1));
        }
    };

    private final GasDayOptions.RelativePeriodTranslationKey translationKey;

    DefaultRelativePeriodDefinition(GasDayOptions.RelativePeriodTranslationKey translationKey) {
        this.translationKey = translationKey;
    }

    public String getPeriodName() {
        return this.translationKey.getDefaultFormat();
    }

    public RelativePeriod create(TimeService timeService, GasDayOptions gasDayOptions) {
        return timeService.createDefaultRelativePeriod(this.getPeriodName(), this.fromWith(gasDayOptions), this.toWith(gasDayOptions), Collections.emptyList());
    }

    protected abstract RelativeDate fromWith(GasDayOptions gasDayOptions);

    protected abstract RelativeDate toWith(GasDayOptions gasDayOptions);

    private static RelativeDate onFirstDayOfWeek(GasDayOptions gasDayOptions, RelativeOperation... operations) {
        return new RelativeDate(
                Stream
                        .concat(
                                Stream.concat(
                                        Stream.of(
                                                HOUR.minus(gasDayOptions.getYearStart().getHour()),
                                                DAY_OF_WEEK.equalTo(getFirstDayOfWeek().getValue())),
                                        Stream.of(operations)),
                                startOfDayOperations(gasDayOptions))
                        .collect(Collectors.toList()));
    }

    private static RelativeDate atStartOfDay(GasDayOptions gasDayOptions, RelativeOperation... operations) {
        return new RelativeDate(
                Stream
                        .concat(
                                Stream.concat(
                                        Stream.of(HOUR.minus(gasDayOptions.getYearStart().getHour())),
                                        Stream.of(operations)),
                                startOfDayOperations(gasDayOptions))
                        .collect(Collectors.toList()));
    }

    private static Stream<RelativeOperation> startOfDayOperations(GasDayOptions gasDayOptions) {
        return Stream.of(
                SECONDS.equalTo(0),
                MINUTES.equalTo(0),
                HOUR.equalTo(gasDayOptions.getYearStart().getHour()));
    }

    private static DayOfWeek getFirstDayOfWeek() {
        return WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
    }

}