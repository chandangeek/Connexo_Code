package com.energyict.mdc.engine.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.calendar.FixedExceptionalOccurrence;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransition;
import com.elster.jupiter.calendar.RecurrentExceptionalOccurrence;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.TariffCalendar;

import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link TariffCalendarExtractor} interface
 * that assumes that all UPL objects are in fact {@link Calendar}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-19 (11:07)
 */
@Component(name = "com.energyict.mdc.upl.messages.legacy.tariff.calendar.extractor", service = {TariffCalendarExtractor.class})
@SuppressWarnings("unused")
public class TariffCalendarExtractorImpl implements TariffCalendarExtractor {

    private ThreadLocal<ThreadContextImpl> threadContextThreadLocal = ThreadLocal.withInitial(ThreadContextImpl::new);

    @Activate
    public void activate() {
        Services.tariffCalendarExtractor(this);
    }

    @Deactivate
    public void deactivate() {
        Services.tariffCalendarExtractor(null);
    }

    @Override
    public ThreadContextImpl threadContext() {
        return this.threadContextThreadLocal.get();
    }

    @Override
    public String id(TariffCalendar calender) {
        return this.id((Calendar) calender);
    }

    private String id(Calendar calender) {
        return Long.toString(calender.getId());
    }

    @Override
    public String name(TariffCalendar calender) {
        return ((Calendar) calender).getName();
    }

    @Override
    public Optional<String> seasonSetId(TariffCalendar calender) {
        return this.season(calender).map(CalendarSeasonSet::id);
    }

    @Override
    public Optional<CalendarSeasonSet> season(TariffCalendar calender) {
        return this.season((Calendar) calender);
    }

    private Optional<CalendarSeasonSet> season(Calendar calender) {
        /* Connexo Calendars don't have season sets
         * but if the Calendar has periods, we will create
         * a virtual season set and use the same id as the Calendar. */
        if (calender.getPeriods().isEmpty()) {
            // No periods, means no seasons so no season set
            return Optional.empty();
        } else {
            return Optional.of(new VirtualCalendarSeasonSet(calender));
        }
    }

    @Override
    public TimeZone definitionTimeZone(TariffCalendar calender) {
        return TimeZone.getDefault();
    }

    @Override
    public TimeZone destinationTimeZone(TariffCalendar calender) {
        return this.threadContext().getTimeZone();
    }

    @Override
    public int intervalInSeconds(TariffCalendar calender) {
        return this.threadContext().intervalInSeconds();
    }

    @Override
    public Range<Year> range(TariffCalendar calender) {
        return this.range((Calendar) calender);
    }

    private Range<Year> range(Calendar calender) {
        return Range.closed(calender.getStartYear(), calender.getEndYear());
    }

    @Override
    public List<CalendarDayType> dayTypes(TariffCalendar calender) {
        return this.dayTypes((Calendar) calender);
    }

    private List<CalendarDayType> dayTypes(Calendar calender) {
        return calender
                .getDayTypes()
                .stream()
                .map(DayTypeAdapter::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<CalendarRule> rules(TariffCalendar calender) {
        return this.rules((Calendar) calender);
    }

    private List<CalendarRule> rules(Calendar calender) {
        List<CalendarRule> rules = new ArrayList<>();
        calender.getExceptionalOccurrences()
                .stream()
                .map(ExceptionalOccurrenceAdapter::new)
                .forEach(rules::add);
        calender.getTransitions()
                .stream()
                .flatMap(this::toRules)
                .forEach(rules::add);
        return rules;
    }

    private Stream<CalendarRule> toRules(PeriodTransition transition) {
        return Stream
                .of(DayOfWeek.values())
                .map(dayOfWeek -> new DayInPeriod(dayOfWeek, transition));
    }

    private static class ThreadContextImpl implements ThreadContext {
        private OfflineDevice device;
        private OfflineDeviceMessage message;

        TimeZone getTimeZone() {
            if (this.device == null) {
                return TimeZone.getDefault();
            } else {
                return this.device.getTimeZone();
            }
        }

        public int intervalInSeconds() {
            if (this.device == null) {
                return 900;
            } else {
            /* Todo: Use the available reading types from the OfflineDevice,
                     sort them as:
                     favour Electricity, favour Wh over W, favour smaller intervals over larger intervals
                     Use the interval of the best match. */
                return 900;
            }
        }

        @Override
        public OfflineDevice getDevice() {
            return device;
        }

        @Override
        public void setDevice(OfflineDevice device) {
            this.device = device;
        }

        @Override
        public OfflineDeviceMessage getMessage() {
            return message;
        }

        @Override
        public void setMessage(OfflineDeviceMessage message) {
            this.message = message;
        }
    }

    private static class DayTypeAdapter implements CalendarDayType {
        private final DayType actual;

        private DayTypeAdapter(DayType actual) {
            this.actual = actual;
        }

        @Override
        public String id() {
            return Long.toString(this.actual.getId());
        }

        @Override
        public String name() {
            return this.actual.getName();
        }

        @Override
        public List<CalendarDayTypeSlice> slices() {
            return this.actual
                    .getEventOccurrences()
                    .stream()
                    .map(EventOccurrenceAdapter::new)
                    .collect(Collectors.toList());
        }
    }

    private static class EventOccurrenceAdapter implements CalendarDayTypeSlice {
        private final EventOccurrence actual;

        private EventOccurrenceAdapter(EventOccurrence actual) {
            this.actual = actual;
        }

        @Override
        public String dayTypeId() {
            return Long.toString(this.actual.getDayType().getId());
        }

        @Override
        public String dayTypeName() {
            return this.actual.getDayType().getName();
        }

        @Override
        public String tariffCode() {
            return Long.toString(this.actual.getEvent().getCode());
        }

        @Override
        public LocalTime start() {
            return this.actual.getFrom();
        }
    }

    private static class ExceptionalOccurrenceAdapter implements CalendarRule {
        private final ExceptionalOccurrence actual;

        private ExceptionalOccurrenceAdapter(ExceptionalOccurrence actual) {
            this.actual = actual;
        }

        @Override
        public String calendarId() {
            return Long.toString(this.actual.getCalendar().getId());
        }

        @Override
        public String dayTypeId() {
            return Long.toString(this.actual.getDayType().getId());
        }

        @Override
        public String dayTypeName() {
            return this.actual.getDayType().getName();
        }

        @Override
        public Optional<String> seasonId() {
            // Exceptional occurrences are NEVER part of a season
            return Optional.empty();
        }

        @Override
        public int year() {
            if (this.actual instanceof FixedExceptionalOccurrence) {
                FixedExceptionalOccurrence fixed = (FixedExceptionalOccurrence) this.actual;
                return fixed.getOccurrence().getYear();
            } else {
                /* Recurrent means it returns every year on the same Date
                 * so the rule cannot match on the actual year.
                 * Returning 0 to indicate that this field should not be used for matching.
                 */
                return 0;
            }
        }

        @Override
        public int month() {
            if (this.actual instanceof FixedExceptionalOccurrence) {
                FixedExceptionalOccurrence fixed = (FixedExceptionalOccurrence) this.actual;
                return fixed.getOccurrence().getMonthValue();
            } else {
                RecurrentExceptionalOccurrence recurrent = (RecurrentExceptionalOccurrence) this.actual;
                return recurrent.getOccurrence().getMonthValue();
            }
        }

        @Override
        public int day() {
            if (this.actual instanceof FixedExceptionalOccurrence) {
                FixedExceptionalOccurrence fixed = (FixedExceptionalOccurrence) this.actual;
                return fixed.getOccurrence().getDayOfMonth();
            } else {
                RecurrentExceptionalOccurrence recurrent = (RecurrentExceptionalOccurrence) this.actual;
                return recurrent.getOccurrence().getDayOfMonth();
            }
        }

        @Override
        public int dayOfWeek() {
            if (this.actual instanceof FixedExceptionalOccurrence) {
                FixedExceptionalOccurrence fixed = (FixedExceptionalOccurrence) this.actual;
                return fixed.getOccurrence().getDayOfWeek().getValue();
            } else {
                /* Recurrent means it returns every year on the same Date but the actual
                 * day of week changes every year so a rule cannot match on day of week.
                 * Returning 0 to indicate that this field should not be used for matching.
                 */
                return 0;
            }
        }
    }

    private static class DayInPeriod implements CalendarRule {
        private final DayOfWeek dayOfWeek;
        private final PeriodTransition transition;

        DayInPeriod(DayOfWeek dayOfWeek, PeriodTransition transition) {
            this.dayOfWeek = dayOfWeek;
            this.transition = transition;
        }

        @Override
        public String calendarId() {
            return Long.toString(this.transition.getPeriod().getCalendar().getId());
        }

        @Override
        public String dayTypeId() {
            return Long.toString(this.transition.getPeriod().getDayType(this.dayOfWeek).getId());
        }

        @Override
        public String dayTypeName() {
            return this.transition.getPeriod().getDayType(this.dayOfWeek).getName();
        }

        @Override
        public Optional<String> seasonId() {
            // Period maps to Season
            return Optional.of(Long.toString(this.transition.getPeriod().getId()));
        }

        @Override
        public int year() {
            return this.transition.getOccurrence().getYear();
        }

        @Override
        public int month() {
            return this.transition.getOccurrence().getMonthValue();
        }

        @Override
        public int day() {
            return this.transition.getOccurrence().getDayOfMonth();
        }

        @Override
        public int dayOfWeek() {
            return this.transition.getOccurrence().getDayOfWeek().getValue();
        }
    }

    private static class VirtualCalendarSeasonSet implements CalendarSeasonSet {
        private final Calendar calender;

        private VirtualCalendarSeasonSet(Calendar calender) {
            this.calender = calender;
        }

        @Override
        public String id() {
            return Long.toString(this.calender.getId());
        }

        @Override
        public String name() {
            return this.calender.getName();
        }

        @Override
        public List<CalendarSeason> seasons() {
            return this.calender.getPeriods()
                    .stream()
                    .map(period -> new CalendarSeasonAdapter(period, this.calender))
                    .collect(Collectors.toList());
        }
    }

    private static final class CalendarSeasonAdapter implements CalendarSeason {
        private final Period period;
        private final Calendar calendar;

        private CalendarSeasonAdapter(Period period, Calendar calendar) {
            this.period = period;
            this.calendar = calendar;
        }

        @Override
        public String id() {
            return Long.toString(this.period.getId());
        }

        @Override
        public String name() {
            return this.period.getName();
        }

        @Override
        public List<CalendarSeasonTransition> transistions() {
            return this.calendar.getTransitions()
                    .stream()
                    .filter(each -> each.getPeriod().equals(this.period))
                    .map(PeriodTransitionAdapter::new)
                    .collect(Collectors.toList());
        }
    }

    private static class PeriodTransitionAdapter implements CalendarSeasonTransition {
        private final PeriodTransition actual;

        private PeriodTransitionAdapter(PeriodTransition actual) {
            this.actual = actual;
        }

        @Override
        public Optional<LocalDate> start() {
            return Optional.of(this.actual.getOccurrence());
        }
    }

}