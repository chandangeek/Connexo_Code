package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.PassiveEffectiveCalendar;
import com.energyict.mdc.protocol.api.device.data.CollectedCalendarInformation;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.util.Optional;
import java.util.TimeZone;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.device.data.Device.CalendarSupport#updateCalendars(CollectedCalendarInformation)} method.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-06-27 (09:44)
 */
public class UpdateCalendarsIT extends PersistenceIntegrationTest {

    private static final String CALENDAR_NAME = "Calendar";

    @Test
    @Transactional
    public void setActiveGhostCalendarOnVirginDevice() {
        Instant june1st2016 = Instant.ofEpochMilli(1464732000000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(june1st2016);
        Device device = inMemoryPersistence.getDeviceService().newDevice(this.deviceConfiguration, "DeviceName", "setActiveGhostCalendarOnVirginDevice");
        CollectedCalendarInformation collectedCalendarInformation = mock(CollectedCalendarInformation.class);
        when(collectedCalendarInformation.isEmpty()).thenReturn(false);
        String expectedCalendarName = "Casper";
        when(collectedCalendarInformation.getActiveCalendar()).thenReturn(Optional.of(expectedCalendarName));
        when(collectedCalendarInformation.getPassiveCalendar()).thenReturn(Optional.empty());

        // Business method
        device.calendars().updateCalendars(collectedCalendarInformation);

        // Asserts
        assertThat(device.calendars().getActive()).isPresent();
        assertThat(device.calendars().getActive().get().getRange()).isEqualTo(Range.atLeast(june1st2016));
        assertThat(device.calendars().getActive().get().getLastVerifiedDate()).isEqualTo(june1st2016);
        assertThat(device.calendars().getActive().get().getAllowedCalendar().isGhost()).isTrue();
        assertThat(device.calendars().getActive().get().getAllowedCalendar().getName()).isEqualTo(expectedCalendarName);
        assertThat(device.calendars().getPassive()).isEmpty();
    }

    @Test
    @Transactional
    public void setPassiveGhostCalendarOnVirginDevice() {
        Instant june1st2016 = Instant.ofEpochMilli(1464732000000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(june1st2016);
        Device device = inMemoryPersistence.getDeviceService().newDevice(this.deviceConfiguration, "DeviceName", "setPassiveGhostCalendarOnVirginDevice");
        CollectedCalendarInformation collectedCalendarInformation = mock(CollectedCalendarInformation.class);
        when(collectedCalendarInformation.isEmpty()).thenReturn(false);
        String expectedCalendarName = "Casper";
        when(collectedCalendarInformation.getActiveCalendar()).thenReturn(Optional.empty());
        when(collectedCalendarInformation.getPassiveCalendar()).thenReturn(Optional.of(expectedCalendarName));

        // Business method
        device.calendars().updateCalendars(collectedCalendarInformation);

        // Asserts
        assertThat(device.calendars().getActive()).isEmpty();
        assertThat(device.calendars().getPassive()).hasSize(1);
        PassiveEffectiveCalendar passiveEffectiveCalendar = device.calendars().getPassive().get(0);
        assertThat(passiveEffectiveCalendar.getActivationDate()).isEqualTo(june1st2016);
        assertThat(passiveEffectiveCalendar.getAllowedCalendar().isGhost()).isTrue();
        assertThat(passiveEffectiveCalendar.getAllowedCalendar().getName()).isEqualTo(expectedCalendarName);
    }

    @Test
    @Transactional
    public void setActiveAndPassiveGhostCalendarsOnVirginDevice() {
        Instant june1st2016 = Instant.ofEpochMilli(1464732000000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(june1st2016);
        Device device = inMemoryPersistence.getDeviceService().newDevice(this.deviceConfiguration, "DeviceName", "setActiveAndPassiveGhostCalendarsOnVirginDevice");
        CollectedCalendarInformation collectedCalendarInformation = mock(CollectedCalendarInformation.class);
        when(collectedCalendarInformation.isEmpty()).thenReturn(false);
        String expectedActiveCalendarName = "Casper 1";
        when(collectedCalendarInformation.getActiveCalendar()).thenReturn(Optional.of(expectedActiveCalendarName));
        String expectedPassiveCalendarName = "Casper 2";
        when(collectedCalendarInformation.getPassiveCalendar()).thenReturn(Optional.of(expectedPassiveCalendarName));

        // Business method
        device.calendars().updateCalendars(collectedCalendarInformation);

        // Asserts
        assertThat(device.calendars().getActive()).isPresent();
        assertThat(device.calendars().getActive().get().getRange()).isEqualTo(Range.atLeast(june1st2016));
        assertThat(device.calendars().getActive().get().getLastVerifiedDate()).isEqualTo(june1st2016);
        assertThat(device.calendars().getActive().get().getAllowedCalendar().isGhost()).isTrue();
        assertThat(device.calendars().getActive().get().getAllowedCalendar().getName()).isEqualTo(expectedActiveCalendarName);
        assertThat(device.calendars().getPassive()).hasSize(1);
        PassiveEffectiveCalendar passiveEffectiveCalendar = device.calendars().getPassive().get(0);
        assertThat(passiveEffectiveCalendar.getActivationDate()).isEqualTo(june1st2016);
        assertThat(passiveEffectiveCalendar.getAllowedCalendar().isGhost()).isTrue();
        assertThat(passiveEffectiveCalendar.getAllowedCalendar().getName()).isEqualTo(expectedPassiveCalendarName);
    }

    @Test
    @Transactional
    public void setActiveCalendarOnVirginDevice() {
        Instant june1st2016 = Instant.ofEpochMilli(1464732000000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(june1st2016);
        Device device = inMemoryPersistence.getDeviceService().newDevice(this.deviceConfiguration, "DeviceName", "setActiveCalendarOnVirginDevice");
        Calendar calendar = this.addCalendarToDeviceType(device);

        CollectedCalendarInformation collectedCalendarInformation = mock(CollectedCalendarInformation.class);
        when(collectedCalendarInformation.isEmpty()).thenReturn(false);
        when(collectedCalendarInformation.getActiveCalendar()).thenReturn(Optional.of(calendar.getName()));
        when(collectedCalendarInformation.getPassiveCalendar()).thenReturn(Optional.empty());

        // Business method
        device.calendars().updateCalendars(collectedCalendarInformation);

        // Asserts
        assertThat(device.calendars().getActive()).isPresent();
        assertThat(device.calendars().getActive().get().getRange()).isEqualTo(Range.atLeast(june1st2016));
        assertThat(device.calendars().getActive().get().getLastVerifiedDate()).isEqualTo(june1st2016);
        assertThat(device.calendars().getActive().get().getAllowedCalendar().isGhost()).isFalse();
        assertThat(device.calendars().getActive().get().getAllowedCalendar().getName()).isEqualTo(calendar.getName());
        assertThat(device.calendars().getPassive()).isEmpty();
    }

    @Test
    @Transactional
    public void setPassiveCalendarOnVirginDevice() {
        Instant june1st2016 = Instant.ofEpochMilli(1464732000000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(june1st2016);
        Device device = inMemoryPersistence.getDeviceService().newDevice(this.deviceConfiguration, "DeviceName", "setPassiveCalendarOnVirginDevice");
        Calendar calendar = this.addCalendarToDeviceType(device);

        CollectedCalendarInformation collectedCalendarInformation = mock(CollectedCalendarInformation.class);
        when(collectedCalendarInformation.isEmpty()).thenReturn(false);
        when(collectedCalendarInformation.getActiveCalendar()).thenReturn(Optional.empty());
        when(collectedCalendarInformation.getPassiveCalendar()).thenReturn(Optional.of(calendar.getName()));

        // Business method
        device.calendars().updateCalendars(collectedCalendarInformation);

        // Asserts
        assertThat(device.calendars().getActive()).isEmpty();
        assertThat(device.calendars().getPassive()).hasSize(1);
        PassiveEffectiveCalendar passiveEffectiveCalendar = device.calendars().getPassive().get(0);
        assertThat(passiveEffectiveCalendar.getActivationDate()).isEqualTo(june1st2016);
        assertThat(passiveEffectiveCalendar.getAllowedCalendar().isGhost()).isFalse();
        assertThat(passiveEffectiveCalendar.getAllowedCalendar().getName()).isEqualTo(calendar.getName());
    }

    @Test
    @Transactional
    public void setActiveCalendarThatWasPassiveBefore() {
        Instant may1st2016 = Instant.ofEpochMilli(1462053600000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(may1st2016);
        Device device = this.createSimpleDeviceWithOnePassiveCalendar("DeviceName", "setActiveCalendarThatWasPassiveBefore");
        CollectedCalendarInformation collectedCalendarInformation = mock(CollectedCalendarInformation.class);
        when(collectedCalendarInformation.isEmpty()).thenReturn(false);
        when(collectedCalendarInformation.getActiveCalendar()).thenReturn(Optional.of(CALENDAR_NAME));
        when(collectedCalendarInformation.getPassiveCalendar()).thenReturn(Optional.of(CALENDAR_NAME));
        Instant june1st2016 = Instant.ofEpochMilli(1464732000000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(june1st2016);

        // Business method
        device.calendars().updateCalendars(collectedCalendarInformation);

        // Asserts
        assertThat(device.calendars().getActive()).isPresent();
        assertThat(device.calendars().getActive().get().getRange()).isEqualTo(Range.atLeast(june1st2016));
        assertThat(device.calendars().getActive().get().getLastVerifiedDate()).isEqualTo(june1st2016);
        assertThat(device.calendars().getActive().get().getAllowedCalendar().isGhost()).isFalse();
        assertThat(device.calendars().getActive().get().getAllowedCalendar().getName()).isEqualTo(CALENDAR_NAME);
        assertThat(device.calendars().getPassive()).isEmpty();
    }

    @Test
    @Transactional
    public void setPassiveCalendarThatWasAlreadyPassive() {
        Instant may1st2016 = Instant.ofEpochMilli(1462053600000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(may1st2016);
        Device device = this.createSimpleDeviceWithOnePassiveCalendar("DeviceName", "setPassiveCalendarThatWasAlreadyPassive");
        CollectedCalendarInformation collectedCalendarInformation = mock(CollectedCalendarInformation.class);
        when(collectedCalendarInformation.isEmpty()).thenReturn(false);
        when(collectedCalendarInformation.getActiveCalendar()).thenReturn(Optional.empty());
        when(collectedCalendarInformation.getPassiveCalendar()).thenReturn(Optional.of(CALENDAR_NAME));
        Instant june1st2016 = Instant.ofEpochMilli(1464732000000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(june1st2016);

        // Business method
        device.calendars().updateCalendars(collectedCalendarInformation);

        // Asserts
        assertThat(device.calendars().getActive()).isEmpty();
        assertThat(device.calendars().getPassive()).hasSize(1);
        PassiveEffectiveCalendar passiveEffectiveCalendar = device.calendars().getPassive().get(0);
        assertThat(passiveEffectiveCalendar.getActivationDate()).isEqualTo(may1st2016);
        assertThat(passiveEffectiveCalendar.getAllowedCalendar().isGhost()).isFalse();
        assertThat(passiveEffectiveCalendar.getAllowedCalendar().getName()).isEqualTo(CALENDAR_NAME);
    }

    @Test
    @Transactional
    public void setOtherActiveGhostCalendar() {
        Instant may1st2016 = Instant.ofEpochMilli(1462053600000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(may1st2016);
        Device device = this.createSimpleDeviceWithOneActiveCalendar("DeviceName", "setOtherActiveGhostCalendar");
        String expectedCalendarName = "Second";
        this.createCalendar(expectedCalendarName);  // Do not add the calendar to the device type so that it will recognized as Ghost
        CollectedCalendarInformation collectedCalendarInformation = mock(CollectedCalendarInformation.class);
        when(collectedCalendarInformation.isEmpty()).thenReturn(false);
        when(collectedCalendarInformation.getActiveCalendar()).thenReturn(Optional.of(expectedCalendarName));
        when(collectedCalendarInformation.getPassiveCalendar()).thenReturn(Optional.empty());
        Instant june1st2016 = Instant.ofEpochMilli(1464732000000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(june1st2016);

        // Business method
        device.calendars().updateCalendars(collectedCalendarInformation);

        // Asserts
        assertThat(device.calendars().getActive()).isPresent();
        assertThat(device.calendars().getActive().get().getRange()).isEqualTo(Range.atLeast(june1st2016));
        assertThat(device.calendars().getActive().get().getLastVerifiedDate()).isEqualTo(june1st2016);
        assertThat(device.calendars().getActive().get().getAllowedCalendar().isGhost()).isTrue();
        assertThat(device.calendars().getActive().get().getAllowedCalendar().getName()).isEqualTo(expectedCalendarName);
        assertThat(device.calendars().getPassive()).isEmpty();
    }

    @Test
    @Transactional
    public void setOtherActiveCalendar() {
        Instant may1st2016 = Instant.ofEpochMilli(1462053600000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(may1st2016);
        Device device = this.createSimpleDeviceWithOneActiveCalendar("DeviceName", "setOtherActiveCalendar");
        String expectedCalendarName = "Second";
        Calendar calendar = this.createCalendar(expectedCalendarName);
        this.addCalendarToDeviceType(device, calendar);
        CollectedCalendarInformation collectedCalendarInformation = mock(CollectedCalendarInformation.class);
        when(collectedCalendarInformation.isEmpty()).thenReturn(false);
        when(collectedCalendarInformation.getActiveCalendar()).thenReturn(Optional.of(expectedCalendarName));
        when(collectedCalendarInformation.getPassiveCalendar()).thenReturn(Optional.empty());
        Instant june1st2016 = Instant.ofEpochMilli(1464732000000L);
        when(inMemoryPersistence.getClock().instant()).thenReturn(june1st2016);

        // Business method
        device.calendars().updateCalendars(collectedCalendarInformation);

        // Asserts
        assertThat(device.calendars().getActive()).isPresent();
        assertThat(device.calendars().getActive().get().getRange()).isEqualTo(Range.atLeast(june1st2016));
        assertThat(device.calendars().getActive().get().getLastVerifiedDate()).isEqualTo(june1st2016);
        assertThat(device.calendars().getActive().get().getAllowedCalendar().isGhost()).isFalse();
        assertThat(device.calendars().getActive().get().getAllowedCalendar().getName()).isEqualTo(expectedCalendarName);
        assertThat(device.calendars().getPassive()).isEmpty();
    }

    private Calendar addCalendarToDeviceType(Device device) {
        return this.addCalendarToDeviceType(device, this.createCalendar());
    }

    private Calendar addCalendarToDeviceType(Device device, Calendar calendar) {
        DeviceType deviceType = device.getDeviceType();
        deviceType.addCalendar(calendar);
        return calendar;
    }

    private Device createSimpleDeviceWithOneActiveCalendar(String name, String mRID) {
        Device device = inMemoryPersistence.getDeviceService().newDevice(this.deviceConfiguration, name, mRID);
        Calendar calendar = this.addCalendarToDeviceType(device);
        CollectedCalendarInformation collected = mock(CollectedCalendarInformation.class);
        when(collected.isEmpty()).thenReturn(false);
        when(collected.getActiveCalendar()).thenReturn(Optional.of(calendar.getName()));
        when(collected.getPassiveCalendar()).thenReturn(Optional.empty());
        device.calendars().updateCalendars(collected);
        return device;
    }

    private Device createSimpleDeviceWithOnePassiveCalendar(String name, String mRID) {
        Device device = inMemoryPersistence.getDeviceService().newDevice(this.deviceConfiguration, name, mRID);
        Calendar calendar = this.addCalendarToDeviceType(device);
        CollectedCalendarInformation collected = mock(CollectedCalendarInformation.class);
        when(collected.isEmpty()).thenReturn(false);
        when(collected.getActiveCalendar()).thenReturn(Optional.empty());
        when(collected.getPassiveCalendar()).thenReturn(Optional.of(calendar.getName()));
        device.calendars().updateCalendars(collected);
        return device;
    }

    private Calendar createCalendar() {
        return this.createCalendar(CALENDAR_NAME);
    }

    private Calendar createCalendar(String name) {
        return inMemoryPersistence.
                getCalendarService().newCalendar(name, TimeZone.getTimeZone("Europe/Brussels"), Year.of(2010))
                .endYear(Year.of(2020))
                .description("Description remains to be completed :-)")
                .mRID(name + "-mRID")
                .addEvent("On peak", 3)
                .addEvent("Off peak", 5)
                .addEvent("Demand response", 97)
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