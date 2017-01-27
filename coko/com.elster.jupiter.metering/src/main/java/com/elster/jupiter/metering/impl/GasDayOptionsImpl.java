/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.GasDayOptions;
import com.elster.jupiter.metering.impl.upgraders.DefaultRelativePeriodDefinition;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.DayMonthTime;

import com.google.inject.Inject;

import java.time.LocalTime;
import java.time.MonthDay;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link GasDayOptions} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-07-15 (12:23)
 */
class GasDayOptionsImpl implements GasDayOptions {
    static final int SINGLETON_ID = 1;

    @SuppressWarnings("unused")
    private long id;
    private int month;
    private int day;
    private int hour;

    private final TimeService timeService;
    @Override
    public DayMonthTime getYearStart() {
        return DayMonthTime.from(MonthDay.of(this.month, this.day), LocalTime.of(this.hour, 0));
    }

    static GasDayOptionsImpl create(DataModel dataModel, DayMonthTime yearStart) {
        if (dataModel.mapper(GasDayOptions.class).getOptional(SINGLETON_ID).isPresent()) {
            throw new IllegalStateException("Gas day options have already been configured");
        }
        GasDayOptionsImpl gasDayOptions = dataModel.getInstance(GasDayOptionsImpl.class).init(SINGLETON_ID, yearStart);
        dataModel.persist(gasDayOptions);
        return gasDayOptions;
    }

    @Inject
    GasDayOptionsImpl(TimeService timeService) {
        super();
        this.timeService = timeService;
    }

    GasDayOptionsImpl init(int id, DayMonthTime yearStart) {
        this.id = id;
        this.month = yearStart.getMonthValue();
        this.day = yearStart.getDayOfMonth();
        this.hour = yearStart.getHour();
        return this;
    }

    @Override
    public List<RelativePeriod> getRelativePeriods() {
        return Stream
                .of(DefaultRelativePeriodDefinition.values())
                .map(def -> this.timeService.findRelativePeriodByName(def.getPeriodName()))
                .flatMap(Functions.asStream())
                .collect(Collectors.toList());
    }

}