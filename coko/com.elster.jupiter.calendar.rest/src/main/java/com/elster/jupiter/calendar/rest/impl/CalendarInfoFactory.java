package com.elster.jupiter.calendar.rest.impl;


import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransition;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CalendarInfoFactory {
    private int NUMBER_OF_EVENTS = 5;
    private int NUMBER_OF_DAYTYPES = 3;

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

    CalendarInfo forViewing(long id) {
        CalendarInfo info = fromCalendar(id);

        info.weekTemplate = new ArrayList<>();
        String[] weekdays = new String[7];
        weekdays[0] = "Monday";
        weekdays[1] = "Tuesday";
        weekdays[2] = "Wednesday";
        weekdays[3] = "Thursday";
        weekdays[4] = "Friday";
        weekdays[5] = "Saturday";
        weekdays[6] = "Sunday";

        for (int i = 0; i < weekdays.length; i++) {
            DayInfo dayInfo = new DayInfo();
            dayInfo.name = weekdays[i];
            dayInfo.type = getRandomBetween(0, NUMBER_OF_DAYTYPES - 1);
            info.weekTemplate.add(dayInfo);
        }

        return info;
    }

    private int getRandomBetween(int min, int max) {
        int range = (max - min) + 1;
        return (int) (Math.random() * range) + min;
    }

    public CalendarInfo fromCalendar(Calendar calendar) {
        CalendarInfo calendarInfo = new CalendarInfo();

        addBasicInformation(calendar, calendarInfo);
//        calendarInfo.startYear = calendar.getStartYear();
        addPeriods(calendarInfo, calendar.getTransitions());
        addEvents(calendarInfo, calendar.getEvents());
        addDayTypes(calendarInfo, calendar.getDayTypes());

        return calendarInfo;
    }

    public CalendarInfo fromCalendar(Calendar calendar, LocalDate localDate) {
        CalendarInfo calendarInfo = new CalendarInfo();
        addBasicInformation(calendar, calendarInfo);
        Map<DayOfWeek, PeriodTransition> transitionsPerDay = calculateWeekInfo(calendar, localDate);
        calendarInfo.weekTemplate = new ArrayList<>();
        Set<DayType> dayTypes = new HashSet<>();
        Set<Event> events = new HashSet<>();
        Set<PeriodTransition> periodTransistions = new HashSet<>();
        for (DayOfWeek day: transitionsPerDay.keySet()) {
            DayType dayType = transitionsPerDay.get(day).getPeriod().getDayType(day);
            //Add to week template
            DayInfo dayInfo = new DayInfo();
            dayInfo.name = day.toString();
            dayInfo.type = dayType.getId();
            calendarInfo.weekTemplate.add(dayInfo);
            //if dagtype nog niet in dagtypes: voeg toe
            dayTypes.add(dayType);
            //ook event toevoegen waar het dagtype aan gelinked is
            dayType.getEventOccurrences()
                    .stream()
                    .forEach(eventOccurrence -> events.add(eventOccurrence.getEvent()));
            //add periods to set
            periodTransistions.add(transitionsPerDay.get(day));
        }
        ArrayList<DayType> dayTypesList = new ArrayList<>();
        dayTypesList.addAll(dayTypes);
        addDayTypes(calendarInfo, dayTypesList);

        ArrayList<Event> eventList = new ArrayList<>();
        eventList.addAll(events);
        addEvents(calendarInfo, eventList);

        ArrayList<PeriodTransition> periodList = new ArrayList<>();
        periodList.addAll(periodTransistions);
        addPeriods(calendarInfo, periodList);

        return calendarInfo;
    }

    private Map<DayOfWeek, PeriodTransition> calculateWeekInfo(Calendar calendar, LocalDate localDate) {
        Map<DayOfWeek, PeriodTransition> transitionPerDay = new LinkedHashMap<>(DayOfWeek.values().length);
        for(int i = 0; i < DayOfWeek.values().length; i++) {
            PeriodTransition transition = getTransitionForDate(calendar.getTransitions(), localDate.plusDays(i));
            transitionPerDay.put(localDate.plusDays(i).getDayOfWeek(), transition);
        }

        return transitionPerDay;
    }


    private PeriodTransition getTransitionForDate( List<PeriodTransition> transitions, LocalDate localDate) {
        for(int i = 0; i < transitions.size(); i++) {
            if(i == transitions.size() - 1) {
                return transitions.get(i);
            } else if (isBetween(localDate, transitions.get(i), transitions.get(i + 1))) {
                return transitions.get(i);
            }
        }
        return null;
    }

    private boolean isBetween(LocalDate localDate, PeriodTransition firstTransition, PeriodTransition nextTransition) {
        return (firstTransition.getOccurrence().isBefore(localDate) || firstTransition.getOccurrence().isEqual(localDate))
                && nextTransition.getOccurrence().isAfter(localDate);
    }

    private void addBasicInformation(Calendar calendar, CalendarInfo calendarInfo) {
        calendarInfo.name = calendar.getName();
        calendarInfo.category = calendar.getCategory().getName();
//        calendarInfo.mRID = calendar.getMrid();
        calendarInfo.id = calendar.getId();
        calendarInfo.description = calendar.getDescription();
        calendarInfo.timeZone = calendar.getTimeZone().getDisplayName();
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
