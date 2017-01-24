package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.ActiveEffectiveCalendar;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.upl.meterdata.CollectedCalendarInformation;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActiveEffectiveCalendarImplTest extends PersistenceIntegrationTest {

    @Test
    @Transactional
    public void setInitialCalendar() {
        Device device = createSimpleDeviceWithOneCalendar();
        assertThat(device.calendars().getActive().get().getAllowedCalendar().getName()).isEqualTo("Calendar");
    }

    @Test
    @Transactional
    public void setInitialCalendarWithReload() {
        Device device = createSimpleDeviceWithOneCalendar();
        Device reloaded = inMemoryPersistence.getDeviceService().findDeviceById(device.getId()).get();

        assertThat(reloaded.calendars().getActive().get().getAllowedCalendar().getName()).isEqualTo("Calendar");
    }

    @Test
    @Transactional
    public void updateCalendar() {
        Instant may1st2016 = Instant.ofEpochMilli(1462053600000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(may1st2016);
        Device device = createSimpleDeviceWithOneCalendar(may1st2016);
        device.getDeviceType().addGhostCalendar("Casper");
        Instant june1st2016 = Instant.ofEpochMilli(1464732000000L);
        CollectedCalendarInformation collected = mock(CollectedCalendarInformation.class);
        when(collected.isEmpty()).thenReturn(false);
        when(collected.getActiveCalendar()).thenReturn(Optional.of("Casper"));
        when(collected.getPassiveCalendar()).thenReturn(Optional.empty());
        when(inMemoryPersistence.getClock().instant()).thenReturn(june1st2016);

        // Business method
        device.calendars().updateCalendars(collected);

        // Asserts
        assertThat(device.calendars().getActive()).isPresent();
        ActiveEffectiveCalendar activeEffectiveCalendar = device.calendars().getActive().get();
        assertThat(activeEffectiveCalendar.getAllowedCalendar().getName()).isEqualTo("Casper");
        assertThat(activeEffectiveCalendar.getRange()).isEqualTo(Range.atLeast(june1st2016));
    }

    @Test
    @Transactional
    public void updateCalendarWithReload() {
        Instant may1st2016 = Instant.ofEpochMilli(1462053600000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(may1st2016);
        Device device = createSimpleDeviceWithOneCalendar(may1st2016);
        device.getDeviceType().addGhostCalendar("Casper");
        Instant june1st2016 = Instant.ofEpochMilli(1464732000000L);
        CollectedCalendarInformation collected = mock(CollectedCalendarInformation.class);
        when(collected.isEmpty()).thenReturn(false);
        when(collected.getActiveCalendar()).thenReturn(Optional.of("Casper"));
        when(collected.getPassiveCalendar()).thenReturn(Optional.empty());
        when(inMemoryPersistence.getClock().instant()).thenReturn(june1st2016);

        // Business method
        device.calendars().updateCalendars(collected);

        // Asserts
        Device reloaded = inMemoryPersistence.getDeviceService().findDeviceById(device.getId()).get();
        ActiveEffectiveCalendar activeEffectiveCalendar = reloaded.calendars().getActive().get();
        assertThat(activeEffectiveCalendar.getAllowedCalendar().getName()).isEqualTo("Casper");
        assertThat(activeEffectiveCalendar.getRange()).isEqualTo(Range.atLeast(june1st2016));
    }

    @Test
    @Transactional
    public void updateCalendarIncrementsVersion() {
        Instant may1st2016 = Instant.ofEpochMilli(1462053600000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(may1st2016);
        Device device = createSimpleDeviceWithOneCalendar(may1st2016);
        device.getDeviceType().addGhostCalendar("Casper");
        Instant june1st2016 = Instant.ofEpochMilli(1464732000000L);
        CollectedCalendarInformation collected = mock(CollectedCalendarInformation.class);
        when(collected.isEmpty()).thenReturn(false);
        when(collected.getActiveCalendar()).thenReturn(Optional.of("Casper"));
        when(collected.getPassiveCalendar()).thenReturn(Optional.empty());
        when(inMemoryPersistence.getClock().instant()).thenReturn(june1st2016);
        long versionBefore = device.getVersion();

        // Business method
        device.calendars().updateCalendars(collected);

        // Asserts
        assertThat(device.getVersion()).isGreaterThan(versionBefore);
    }

    private Device createSimpleDeviceWithOneCalendar() {
        return this.createSimpleDeviceWithOneCalendar(inMemoryPersistence.getClock().instant());
    }

    private Device createSimpleDeviceWithOneCalendar(Instant effective) {
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(this.deviceConfiguration, "DeviceName", "MyUniqueID", effective);
        Calendar calendar = createCalendar();
        DeviceType deviceType = device.getDeviceConfiguration().getDeviceType();
        deviceType.addCalendar(calendar);
        CollectedCalendarInformation collected = mock(CollectedCalendarInformation.class);
        when(collected.isEmpty()).thenReturn(false);
        when(collected.getActiveCalendar()).thenReturn(Optional.of(calendar.getName()));
        when(collected.getPassiveCalendar()).thenReturn(Optional.empty());
        when(inMemoryPersistence.getClock().instant()).thenReturn(effective);
        device.calendars().updateCalendars(collected);
        return device;
    }

    private Calendar createCalendar() {
        CalendarService calendarService = inMemoryPersistence.
                getCalendarService();
        Category category = calendarService.findCategoryByName(OutOfTheBoxCategory.TOU.getDefaultDisplayName()).get();
        EventSet eventSet = calendarService.newEventSet("eventset")
                .addEvent("On peak").withCode(3)
                .addEvent("Off peak").withCode(5)
                .addEvent("Demand response").withCode(97)
                .add();
        return calendarService
                .newCalendar("Calendar", Year.of(2010), eventSet)
                .category(category)
                .endYear(Year.of(2020))
                .description("Description remains to be completed :-)")
                .mRID("Calendar")
                .newDayType("Summer weekday")
                .event("Off peak").startsFrom(LocalTime.of(0, 0, 0))
                .eventWithCode(3).startsFrom(LocalTime.of(13, 0, 0))
                .event("Off peak").startsFrom(LocalTime.of(20, 0, 0))
                .add()
                .newDayType("Weekend")
                .event("Off peak").startsFrom(LocalTime.MIDNIGHT)
                .add()
                .newDayType("Holiday")
                .event("Off peak").startsFrom(LocalTime.MIDNIGHT)
                .add()
                .newDayType("Winter day")
                .event("Off peak").startsFrom(LocalTime.of(0, 0, 0))
                .event("On peak").startsFrom(LocalTime.of(5, 0, 0))
                .event("Off peak").startsFrom(LocalTime.of(21, 0, 0))
                .add()
                .newDayType("Demand response")
                .eventWithCode(97).startsFrom(LocalTime.MIDNIGHT)
                .add()
                .addPeriod("Summer", "Summer weekday", "Summer weekday", "Summer weekday", "Summer weekday", "Summer weekday", "Weekend", "Weekend")
                .addPeriod("Winter", "Winter day", "Winter day", "Winter day", "Winter day", "Winter day", "Winter day", "Winter day")
                .on(MonthDay.of(5, 1)).transitionTo("Summer")
                .on(MonthDay.of(11, 1)).transitionTo("Winter")
                .except("Holiday")
                .occursOnceOn(LocalDate.of(2016, 1, 18))
                .occursOnceOn(LocalDate.of(2016, 2, 15))
                .occursOnceOn(LocalDate.of(2016, 5, 30))
                .occursAlwaysOn(MonthDay.of(7, 4))
                .occursOnceOn(LocalDate.of(2016, 9, 5))
                .occursOnceOn(LocalDate.of(2016, 10, 10))
                .occursAlwaysOn(MonthDay.of(11, 11))
                .occursOnceOn(LocalDate.of(2016, 11, 24))
                .occursAlwaysOn(MonthDay.of(12, 25))
                .occursAlwaysOn(MonthDay.of(12, 26))
                .add()
                .add();
    }

}