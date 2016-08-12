/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.DayMonthTime;

import java.time.LocalTime;
import java.time.MonthDay;

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

    GasDayOptionsImpl() {
        super();
    }

    GasDayOptionsImpl init(int id, DayMonthTime yearStart) {
        this.id = id;
        this.month = yearStart.getMonthValue();
        this.day = yearStart.getDayOfMonth();
        this.hour = yearStart.getHour();
        return this;
    }

}