package com.elster.jupiter.calendar.rest.impl;


import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransition;

import java.util.ArrayList;
import java.util.List;

public class CalendarInfoFactory {
    private int NUMBER_OF_EVENTS = 3;
    private int NUMBER_OF_DAYTYPES = 4;

    CalendarInfo fromCalendar(long id) {
        CalendarInfo calendarInfo = new CalendarInfo();

        calendarInfo.name = "Residential TOU Example";
        calendarInfo.category = "TOU";
        calendarInfo.mRID = "optional";
        calendarInfo.id = id;
        calendarInfo.description = "From example provided by Robert Ritchy";
        calendarInfo.timeZone = "EDT";
        calendarInfo.startYear = 210;

        calendarInfo.events = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_EVENTS; i++) {
            calendarInfo.events.add(new EventInfo(i, "Event " + i, i));
        }


        calendarInfo.dayTypes = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_DAYTYPES; i++) {
            DayTypeInfo dayTypeInfo = new DayTypeInfo();
            dayTypeInfo.id = i;
            dayTypeInfo.name = "Day type" + i;
            dayTypeInfo.ranges = new ArrayList<>();
            int prevFromHour = 0;
            for (int j = 0; j < getRandomBetween(1, 4); j++) {
                RangeInfo range = new RangeInfo();
                if (j == 0) {
                    range.fromHour = 0L;
                    range.fromMinute = 0L;
                    range.fromSecond = 0L;
                } else {
                    range.fromHour = (long) getRandomBetween(prevFromHour + 1, 19 + j);
                    range.fromMinute = (long) getRandomBetween(0, 59);
                    range.fromSecond = (long) getRandomBetween(0, 59);
                    prevFromHour = (int) range.fromHour;
                }
                range.event = getRandomBetween(0, NUMBER_OF_EVENTS - 1);
                dayTypeInfo.ranges.add(range);
            }

            calendarInfo.dayTypes.add(dayTypeInfo);
        }

        calendarInfo.periods = new ArrayList<>();
        calendarInfo.periods.add(new PeriodInfo("Summer", 5, 1));
        calendarInfo.periods.add(new PeriodInfo("Winter", 11, 1));


        return calendarInfo;
    }

    private int getRandomBetween(int min, int max) {
        int range = (max - min) + 1;
        return (int) (Math.random() * range) + min;
    }

    public CalendarInfo fromCalendar(Calendar calendar) {
        CalendarInfo calendarInfo = new CalendarInfo();

        calendarInfo.name = calendar.getName();
        calendarInfo.category = calendar.getCategory().getName();
//        calendarInfo.mRID = calendar.getMrid();
        calendarInfo.id = calendar.getId();
        calendarInfo.description = calendar.getDescription();
        calendarInfo.timeZone = calendar.getTimeZone().getDisplayName();
//        calendarInfo.startYear = calendar.getStartYear();
        addEvents(calendarInfo, calendar.getEvents());
        addDayTypes(calendarInfo, calendar.getDayTypes());
        addPeriods(calendarInfo, calendar.getTransitions());

        return calendarInfo;
    }

    private void addPeriods(CalendarInfo calendarInfo, List<PeriodTransition> transitions) {
        calendarInfo.periods = new ArrayList<>();
        transitions.stream()
                .forEach(transition -> calendarInfo.periods.add(new PeriodInfo(transition.getPeriod().getName(), transition.getOccurrence().getMonthValue(), transition.getOccurrence().getDayOfMonth())));
    }

    private void addDayTypes(CalendarInfo calendarInfo, List<DayType> dayTypes) {
        calendarInfo.dayTypes = new ArrayList<>();
        dayTypes.stream()
                .forEach(dayType -> calendarInfo.dayTypes.add(createDayType(dayType)));

    }

    private DayTypeInfo createDayType(DayType dayType) {
        DayTypeInfo dayTypeInfo = new DayTypeInfo();
        dayTypeInfo.id = dayType.getId();
        dayTypeInfo.name = dayType.getName();
        createRanges(dayTypeInfo, dayType);

        return dayTypeInfo;
    }

    private void createRanges(DayTypeInfo dayTypeInfo, DayType dayType) {
        dayTypeInfo.ranges = new ArrayList<>();
        dayType.getEventOccurrences().stream()
                .forEach(eventOccurrence -> dayTypeInfo.ranges.add(createRangeInfo(eventOccurrence)));
    }

    private RangeInfo createRangeInfo(EventOccurrence eventOccurrence) {
        return new RangeInfo(eventOccurrence.getFrom().getHour(), eventOccurrence.getFrom().getMinute(), eventOccurrence.getFrom().getSecond(), eventOccurrence.getEvent().getId());
    }

    private void addEvents(CalendarInfo calendarInfo, List<Event> events) {
        calendarInfo.events = new ArrayList<>();
        events.stream()
                .forEach(event -> calendarInfo.events.add(new EventInfo(event.getId(),event.getName(),event.getCode())));
    }
}
