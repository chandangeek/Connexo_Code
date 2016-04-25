package com.elster.jupiter.calendar.rest.impl;


import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransition;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;

import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class CalendarInfoFactory {

    private Thesaurus thesaurus;

    @Inject
    public CalendarInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(CalendarApplication.COMPONENT_NAME, Layer.REST);
    }

    public CalendarInfo fromCalendar(Calendar calendar) {
        CalendarInfo calendarInfo = new CalendarInfo();

        addBasicInformation(calendar, calendarInfo);
        //calendarInfo.startYear = calendar.getStartYear();
        addPeriods(calendarInfo, calendar.getTransitions());
        addEvents(calendarInfo, calendar.getEvents());
        addDayTypes(calendarInfo, calendar.getDayTypes());
        addDaysPerType(calendarInfo, calendar.getPeriods(), calendar.getDayTypes());
        return calendarInfo;
    }


    public CalendarInfo fromCalendar(Calendar calendar, LocalDate localDate) {
        CalendarInfo calendarInfo = new CalendarInfo();
        addBasicInformation(calendar, calendarInfo);
        Map<Long, PeriodTransition> periodTransistions = new HashMap<>();
        Map<DayOfWeek, DayType> dayTypesPerDay = calculateWeekInfo(calendar, localDate, periodTransistions);
        calendarInfo.weekTemplate = new ArrayList<>();
        Map<Long, DayType> dayTypes = new HashMap<>();
        Map<Long, Event> events = new HashMap<>();
        for (DayOfWeek day : dayTypesPerDay.keySet()) {
            DayType dayType = dayTypesPerDay.get(day);
            //Add to week template
            DayInfo dayInfo = new DayInfo();
            dayInfo.name = thesaurus.getTranslations().get(day.name());
            dayInfo.type = dayType.getId();
            calendarInfo.weekTemplate.add(dayInfo);
            //if dagtype nog niet in dagtypes: voeg toe
            dayTypes.put(dayType.getId(), dayType);
            //ook event toevoegen waar het dagtype aan gelinked is
            dayType.getEventOccurrences()
                    .stream()
                    .forEach(eventOccurrence -> events.put(eventOccurrence.getEvent()
                            .getId(), eventOccurrence.getEvent()));
        }
        ArrayList<DayType> dayTypesList = new ArrayList<>();
        dayTypesList.addAll(dayTypes.values());
        addDayTypes(calendarInfo, dayTypesList);

        ArrayList<Event> eventList = new ArrayList<>();
        eventList.addAll(events.values());
        addEvents(calendarInfo, eventList);

        ArrayList<PeriodTransition> periodList = new ArrayList<>();
        periodList.addAll(periodTransistions.values());
        addPeriods(calendarInfo, periodList);

        return calendarInfo;
    }

    private Map<DayOfWeek, DayType> calculateWeekInfo(Calendar calendar, LocalDate localDate, Map<Long, PeriodTransition> periodTransistions) {
        Map<DayOfWeek, DayType> dayTypesPerDay = new LinkedHashMap<>(DayOfWeek.values().length);
        for (int i = 0; i < DayOfWeek.values().length; i++) {
            DayType dayType = getDaytypeForDate(calendar, localDate.plusDays(i), periodTransistions);
            dayTypesPerDay.put(localDate.plusDays(i).getDayOfWeek(), dayType);
        }

        return dayTypesPerDay;
    }


    private DayType getDaytypeForDate(Calendar calendar, LocalDate localDate, Map<Long, PeriodTransition> periodTransistions) {
        DayType dayType = null;
        List<PeriodTransition> transitions = calendar.getTransitions();
        for (int i = 0; i < transitions.size(); i++) {
            if (i == transitions.size() - 1) {
                dayType = transitions.get(i).getPeriod().getDayType(localDate.getDayOfWeek());
                periodTransistions.put(transitions.get(i).getPeriod().getId(), transitions.get(i));
                break;
            } else if (isBetween(localDate, transitions.get(i), transitions.get(i + 1))) {
                dayType = transitions.get(i).getPeriod().getDayType(localDate.getDayOfWeek());
                periodTransistions.put(transitions.get(i).getPeriod().getId(), transitions.get(i));
                break;
            }
        }
        Optional<ExceptionalOccurrence> exception = checkException(calendar, localDate);
        if (exception.isPresent()) {
            dayType = exception.get().getDayType();
        }
        return dayType;
    }

    private Optional<ExceptionalOccurrence> checkException(Calendar calendar, LocalDate localDate) {
        List<ExceptionalOccurrence> exceptionalOccurrences = calendar.getExceptionalOccurrences();
        Optional<ExceptionalOccurrence> exception = exceptionalOccurrences.stream()
                .filter(exceptionalOccurrence -> exceptionalOccurrence.occursAt(localDate))
                .findFirst();
        return exception;
    }

    private boolean isBetween(LocalDate localDate, PeriodTransition firstTransition, PeriodTransition nextTransition) {
        return (firstTransition.getOccurrence().isBefore(localDate) || firstTransition.getOccurrence()
                .isEqual(localDate))
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
                .filter(distinctByKey(o -> o.getPeriod().getId()))
                .forEach(transition -> calendarInfo.periods.add(new PeriodInfo(transition.getPeriod()
                        .getName(), transition.getOccurrence().getMonthValue(), transition.getOccurrence()
                        .getDayOfMonth())));
    }

    private void addDayTypes(CalendarInfo calendarInfo, List<DayType> dayTypes) {
        calendarInfo.dayTypes = new ArrayList<>();
        dayTypes.stream()
                .forEach(dayType -> calendarInfo.dayTypes.add(createDayType(dayType)));

    }

    private void addDaysPerType(CalendarInfo calendarInfo, List<Period> periods, List<DayType> dayTypes) {
        calendarInfo.daysPerType = new ArrayList<>();
        Map<Long, Set<String>> daysPerDaytype = new HashMap<>();
        dayTypes.stream()
                .forEach(dayType -> daysPerDaytype.put(dayType.getId(), new LinkedHashSet<>()));
        for(Period period: periods) {
            Stream.of(DayOfWeek.values())
                    .forEach(dow -> daysPerDaytype.get(period.getDayType(dow).getId()).add(thesaurus.getTranslations().get(dow.name())));
        }

        daysPerDaytype.keySet()
                .stream()
                .forEach(key -> calendarInfo.daysPerType.add(new DaysPerTypeInfo(key, new ArrayList<>(daysPerDaytype.get(key)))));
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
        return new RangeInfo(eventOccurrence.getFrom().getHour(), eventOccurrence.getFrom().getMinute(), eventOccurrence
                .getFrom()
                .getSecond(), eventOccurrence.getEvent().getId());
    }

    private void addEvents(CalendarInfo calendarInfo, List<Event> events) {
        calendarInfo.events = new ArrayList<>();
        events.stream()
                .forEach(event -> calendarInfo.events.add(new EventInfo(event.getId(), event.getName(), event.getCode())));
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

}
