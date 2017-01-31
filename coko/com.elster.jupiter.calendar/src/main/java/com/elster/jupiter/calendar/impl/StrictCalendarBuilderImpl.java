/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.nls.Thesaurus;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.util.streams.Predicates.on;

class StrictCalendarBuilderImpl implements CalendarService.StrictCalendarBuilder {
    private final CalendarImpl updating;
    private final List<StrictExceptionBuilderImpl> exceptionsToAdd = new ArrayList<>();
    private final Clock clock;
    private final Thesaurus thesaurus;

    public StrictCalendarBuilderImpl(Clock clock, CalendarImpl calendar, Thesaurus thesaurus) {
        this.clock = clock;
        this.updating = calendar;
        this.thesaurus = thesaurus;
    }

    @Override
    public CalendarService.StrictExceptionBuilder except(String dayTypeName) {
        return new StrictExceptionBuilderImpl(dayTypeName);
    }

    @Override
    public Calendar add() {
        exceptionsToAdd.forEach(StrictExceptionBuilderImpl::build);
        updating.save();
        return updating;
    }

    private class StrictExceptionBuilderImpl implements CalendarService.StrictExceptionBuilder {

        private final DayTypeImpl dayTypeImpl;
        private LocalDate localDate;

        public StrictExceptionBuilderImpl(DayTypeImpl dayType) {
            this.dayTypeImpl = dayType;
        }

            public StrictExceptionBuilderImpl(String dayTypeName) {
            this.dayTypeImpl = (DayTypeImpl) updating.getDayTypes()
                    .stream()
                    .filter(on(DayType::getName).test(dayTypeName::equals))
                    .findAny()
                    .orElseThrow(() -> new NoSuchDayType(thesaurus, dayTypeName));
        }

        @Override
        public CalendarService.StrictExceptionBuilder occursOnceOn(LocalDate date) {
            if (!LocalDate.now(clock).isBefore(date)) {
                throw new CannotAddPastExceptionsToActiveCalendar(thesaurus, dayTypeImpl.getName(), date);
            }
            this.localDate = date;
            exceptionsToAdd.add(this);
            return new StrictExceptionBuilderImpl(dayTypeImpl);
        }

        @Override
        public CalendarService.StrictCalendarBuilder add() {
            return StrictCalendarBuilderImpl.this;
        }

        private void build() {
            updating.addFixedExceptionalOccurrence(this.dayTypeImpl, localDate);
        }
    }

}
