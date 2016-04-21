package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.orm.DataModel;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.util.TimeZone;

/**
 * Created by igh on 21/04/2016.
 */
public class CalendarBuilderImpl implements CalendarService.CalendarBuilder {

    private DataModel dataModel;
    private CalendarImpl underConstruction;

    public CalendarBuilderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
        this.underConstruction = this.dataModel.getInstance(CalendarImpl.class);
    }

    void init(String name, TimeZone timeZone, Year start) {
        this.underConstruction.setName(name);
        this.underConstruction.setTimeZone(timeZone);
        this.underConstruction.setStartYear(start);
    }

    @Override
    public CalendarService.CalendarBuilder endYear(Year setStartYear) {
        this.underConstruction.setEndYear(setStartYear);
        return this;
    }

    @Override
    public CalendarService.CalendarBuilder mRID(String mRID) {
        this.underConstruction.setmRID(mRID);
        return this;
    }

    @Override
    public CalendarService.CalendarBuilder description(String description) {
        this.underConstruction.setDescription(description);
        return this;
    }

    @Override
    public CalendarService.CalendarBuilder addEvent(String name, int code) {
        return null;
    }

    @Override
    public CalendarService.DayTypeBuilder newDayType(String name) {
        return null;
    }

    @Override
    public CalendarService.CalendarBuilder addPeriod(String name, String mondayDayTypeName, String tuesdayDayTypeName, String wednesdayDayTypeName, String thursdayDayTypeName, String fridayDayTypeName, String saturdayDayTypeName, String sundayDayTypeName) {
        return null;
    }

    @Override
    public CalendarService.TransitionBuilder on(MonthDay occurrence) {
        return null;
    }

    @Override
    public CalendarService.TransitionBuilder on(LocalDate occurrence) {
        return null;
    }

    @Override
    public CalendarService.ExceptionBuilder except(String dayTypeName) {
        return null;
    }

    @Override
    public Calendar add() {
        this.underConstruction.save();
        return this.underConstruction;
    }

}
