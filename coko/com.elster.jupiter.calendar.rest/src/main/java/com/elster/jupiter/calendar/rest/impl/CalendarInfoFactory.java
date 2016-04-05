package com.elster.jupiter.calendar.rest.impl;


import java.util.ArrayList;

public class CalendarInfoFactory {
    private int NUMBER_OF_EVENTS = 3;
    private int NUMBER_OF_DAYTYPES = 4;

    CalendarInfo fromCalendar() {
        CalendarInfo calendarInfo = new CalendarInfo();

        calendarInfo.name = "Residential TOU Example";
        calendarInfo.category = "TOU";
        calendarInfo.mRID = "optional";
        calendarInfo.id = 1;
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
                range.event = getRandomBetween(0, NUMBER_OF_EVENTS);
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

}
