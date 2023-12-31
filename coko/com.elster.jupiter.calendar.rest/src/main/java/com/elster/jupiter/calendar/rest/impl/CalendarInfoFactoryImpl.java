/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.rest.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.calendar.FixedExceptionalOccurrence;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransition;
import com.elster.jupiter.calendar.RecurrentExceptionalOccurrence;
import com.elster.jupiter.calendar.rest.CalendarInfo;
import com.elster.jupiter.calendar.rest.CalendarInfoFactory;
import com.elster.jupiter.calendar.rest.CategoryInfo;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.calendar.rest.CalendarInfoFactory",
        immediate = true,
        service = CalendarInfoFactory.class)
public class CalendarInfoFactoryImpl implements CalendarInfoFactory {
    private volatile Thesaurus thesaurus;

    public CalendarInfoFactoryImpl() {
        // for OSGi
    }

    @Inject
    public CalendarInfoFactoryImpl(Thesaurus thesaurus) {
        this();
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(CalendarApplication.COMPONENT_NAME, Layer.REST);
    }

    @Override
    public CalendarInfo nameOnly(String name) {
        CalendarInfo calendarInfo = new CalendarInfo();
        calendarInfo.name = name;
        return calendarInfo;
    }

    @Override
    public CalendarInfo detailedFromCalendar(Calendar calendar) {
        CalendarInfo calendarInfo = new CalendarInfo();

        addBasicInformation(calendar, calendarInfo);
        addPeriods(calendarInfo, calendar.getTransitions());
        addEvents(calendarInfo, calendar.getEvents()); // TODO
        addDayTypes(calendarInfo, calendar.getDayTypes());
        addDaysPerType(calendarInfo, calendar.getPeriods(), calendar.getDayTypes());
        addSpecialDays(calendarInfo, calendar.getExceptionalOccurrences());
        return calendarInfo;
    }


    @Override
    public CalendarInfo summaryFromCalendar(Calendar calendar) {
        CalendarInfo calendarInfo = new CalendarInfo();
        addBasicInformation(calendar, calendarInfo);
        return calendarInfo;
    }

    @Override
    public CalendarInfo summaryForOverview(Calendar calendar, boolean calendarInUse) {
        CalendarInfo calendarInfo = summaryFromCalendar(calendar);
        calendarInfo.inUse = calendarInUse;
        return calendarInfo;
    }

    @Override
    public CalendarInfo detailedWeekFromCalendar(Calendar calendar, LocalDate localDate) {
        CalendarInfo calendarInfo = new CalendarInfo();
        addBasicInformation(calendar, calendarInfo);
        Map<Long, PeriodTransition> periodTransitions = new HashMap<>();
        Map<LocalDate, ExtendedDayType> dayTypesPerDay = calculateWeekInfo(calendar, localDate, periodTransitions);
        calendarInfo.weekTemplate = new ArrayList<>();
        Map<Long, DayType> dayTypes = new HashMap<>();
        Map<Long, Event> events = new HashMap<>();
        List<ExceptionalOccurrence> exceptionalOccurrences = new ArrayList<>();
        int counter = -1;
        for (LocalDate date : dayTypesPerDay.keySet()) {
            ExtendedDayType extendedDayType = dayTypesPerDay.get(date);
            DayType dayType = extendedDayType.getDayType();
            DayInfo dayInfo = new DayInfo();
            dayInfo.name = thesaurus.getFormat(TranslationKeys.from(date.getDayOfWeek().name())).format();
            LocalDate calculatedDate = localDate.plusDays(counter);
            dayInfo.date = calculatedDate.toEpochDay();
            dayInfo.inCalendar = isDayInCalendar(calendar, calculatedDate);
            dayInfo.type = dayType.getId();
            calendarInfo.weekTemplate.add(dayInfo);
            dayTypes.put(dayType.getId(), dayType);
            dayType.getEventOccurrences()
                    .forEach(eventOccurrence -> events.put(eventOccurrence.getEvent()
                            .getId(), eventOccurrence.getEvent()));
            extendedDayType.getExceptionalOccurrence().ifPresent(exceptionalOccurrences::add);
            counter++;
        }
        List<DayType> dayTypesList = new ArrayList<>();
        dayTypesList.addAll(dayTypes.values());
        addDayTypes(calendarInfo, dayTypesList);

        List<Event> eventList = new ArrayList<>();
        eventList.addAll(events.values());
        addEvents(calendarInfo, eventList);

        List<PeriodTransition> periodList = new ArrayList<>();
        periodList.addAll(periodTransitions.values());
        addPeriods(calendarInfo, periodList);

        addSpecialDays(calendarInfo, exceptionalOccurrences);
        return calendarInfo;
    }

    private boolean isDayInCalendar(Calendar calendar, LocalDate calculatedDate) {
        return calendar.getStartYear().getValue() <= calculatedDate.getYear();
    }

    private Map<LocalDate, ExtendedDayType> calculateWeekInfo(Calendar calendar, LocalDate localDate, Map<Long, PeriodTransition> periodTransistions) {
        Map<LocalDate, ExtendedDayType> dayTypesPerDay = new LinkedHashMap<>(DayOfWeek.values().length + 1);
        for (int i = -1; i < DayOfWeek.values().length; i++) {
            ExtendedDayType extendedDayType = getExtendedDayTypeForDate(calendar, localDate.plusDays(i), periodTransistions);
            dayTypesPerDay.put(localDate.plusDays(i), extendedDayType);
        }

        return dayTypesPerDay;
    }


    private ExtendedDayType getExtendedDayTypeForDate(Calendar calendar, LocalDate localDate, Map<Long, PeriodTransition> periodTransistions) {
        ExtendedDayType extendedDayType = null;
        List<PeriodTransition> transitions = calendar.getTransitions();
        for (int i = 0; i < transitions.size(); i++) {
            if (i == transitions.size() - 1) {
                extendedDayType = new ExtendedDayType(transitions.get(i).getPeriod().getDayType(localDate.getDayOfWeek()));
                periodTransistions.put(transitions.get(i).getPeriod().getId(), transitions.get(i));
                break;
            } else if (isBetween(localDate, transitions.get(i), transitions.get(i + 1))) {
                extendedDayType = new ExtendedDayType(transitions.get(i).getPeriod().getDayType(localDate.getDayOfWeek()));
                periodTransistions.put(transitions.get(i).getPeriod().getId(), transitions.get(i));
                break;
            }
        }
        Optional<ExceptionalOccurrence> exception = checkException(calendar, localDate);
        if (exception.isPresent()) {
            extendedDayType = new ExtendedDayType(exception.get().getDayType(), exception.get());
        }
        return extendedDayType;
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
        calendarInfo.category = CategoryInfo.from(calendar.getCategory());
        calendarInfo.startYear = calendar.getStartYear().getValue();
        calendarInfo.id = calendar.getId();
        calendarInfo.description = calendar.getDescription();
        calendarInfo.startYear = calendar.getStartYear().getValue();
        calendarInfo.status = new IdWithDisplayValueInfo<>(calendar.getStatus(), calendar.getStatusDisplayName());
        calendarInfo.version = calendar.getVersion();
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
        dayTypes.forEach(dayType -> calendarInfo.dayTypes.add(createDayType(dayType)));
    }

    private void addDaysPerType(CalendarInfo calendarInfo, List<Period> periods, List<DayType> dayTypes) {
        calendarInfo.daysPerType = new ArrayList<>();
        Map<Long, LinkedHashSet<String>> daysPerDaytype = new HashMap<>();
        dayTypes.forEach(dayType -> daysPerDaytype.put(dayType.getId(), new LinkedHashSet<>()));
        for (Period period : periods) {
            Stream.of(DayOfWeek.values())
                    .forEach(dow -> daysPerDaytype.get(period.getDayType(dow).getId())
                            .add(thesaurus.getFormat(TranslationKeys.from(dow.name())).format()));
        }

        daysPerDaytype.keySet()
                .forEach(key -> calendarInfo.daysPerType.add(new DaysPerTypeInfo(key, new ArrayList<>(daysPerDaytype.get(key)))));
    }

    private void addSpecialDays(CalendarInfo calendarInfo, List<ExceptionalOccurrence> exceptionalOccurrences) {
        calendarInfo.fixedSpecialDays = new ArrayList<>();
        calendarInfo.recurrentSpecialDays = new ArrayList<>();
        exceptionalOccurrences.forEach(exceptionalOccurrence -> {
            if (exceptionalOccurrence instanceof FixedExceptionalOccurrence) {
                calendarInfo.fixedSpecialDays.add(createExceptionalOccurrence((FixedExceptionalOccurrence) exceptionalOccurrence));
            } else if (exceptionalOccurrence instanceof RecurrentExceptionalOccurrence) {
                calendarInfo.recurrentSpecialDays.add(createExceptionalOccurrence((RecurrentExceptionalOccurrence) exceptionalOccurrence));
            }
        });
    }

    private ExceptionalOccurrenceInfo createExceptionalOccurrence(FixedExceptionalOccurrence exceptionalOccurrence) {
        ExceptionalOccurrenceInfo exceptionalOccurrenceInfo = new ExceptionalOccurrenceInfo();
        exceptionalOccurrenceInfo.id = exceptionalOccurrence.getId();
        exceptionalOccurrenceInfo.dayType = new IdWithNameInfo();
        exceptionalOccurrenceInfo.dayType.id = exceptionalOccurrence.getDayType().getId();
        exceptionalOccurrenceInfo.dayType.name = exceptionalOccurrence.getDayType().getName();
        exceptionalOccurrenceInfo.day = exceptionalOccurrence.getOccurrence().getDayOfMonth();
        exceptionalOccurrenceInfo.month = exceptionalOccurrence.getOccurrence().getMonth().name();
        exceptionalOccurrenceInfo.year = exceptionalOccurrence.getOccurrence().getYear();
        return exceptionalOccurrenceInfo;
    }

    private ExceptionalOccurrenceInfo createExceptionalOccurrence(RecurrentExceptionalOccurrence exceptionalOccurrence) {
        ExceptionalOccurrenceInfo exceptionalOccurrenceInfo = new ExceptionalOccurrenceInfo();
        exceptionalOccurrenceInfo.id = exceptionalOccurrence.getId();
        exceptionalOccurrenceInfo.dayType = new IdWithNameInfo();
        exceptionalOccurrenceInfo.dayType.id = exceptionalOccurrence.getDayType().getId();
        exceptionalOccurrenceInfo.dayType.name = exceptionalOccurrence.getDayType().getName();
        exceptionalOccurrenceInfo.day = exceptionalOccurrence.getOccurrence().getDayOfMonth();
        exceptionalOccurrenceInfo.month = exceptionalOccurrence.getOccurrence().getMonth().name();
        return exceptionalOccurrenceInfo;
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
        dayType.getEventOccurrences()
                .forEach(eventOccurrence -> dayTypeInfo.ranges.add(createRangeInfo(eventOccurrence)));
    }

    private RangeInfo createRangeInfo(EventOccurrence eventOccurrence) {
        return new RangeInfo(eventOccurrence.getFrom().getHour(), eventOccurrence.getFrom().getMinute(), eventOccurrence
                .getFrom()
                .getSecond(), eventOccurrence.getEvent().getId());
    }

    private void addEvents(CalendarInfo calendarInfo, List<Event> events) {
        calendarInfo.events = new ArrayList<>();
        events.forEach(event -> calendarInfo.events.add(new EventInfo(event.getId(), event.getName(), event.getCode())));
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private static class ExtendedDayType {
        private DayType dayType;
        private ExceptionalOccurrence exceptionalOccurrence;

        public ExtendedDayType(DayType dayType, ExceptionalOccurrence exceptionalOccurrence) {
            this(dayType);
            this.exceptionalOccurrence = exceptionalOccurrence;
        }

        public ExtendedDayType(DayType dayType) {
            this.dayType = dayType;
        }

        public DayType getDayType() {
            return dayType;
        }

        public Optional<ExceptionalOccurrence> getExceptionalOccurrence() {
            return Optional.ofNullable(exceptionalOccurrence);
        }
    }
}
