/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.PassiveCalendar;
import com.energyict.mdc.upl.meterdata.CollectedCalendarInformation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PassiveCalendarImplTest extends PersistenceIntegrationTest {

    private static final String CALENDAR_NAME = "Calendar";

    private Device createSimpleDeviceWithOneCalendar() {
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "DeviceName", "MyUniqueID", inMemoryPersistence.getClock().instant());
        Calendar calendar = createCalendar();
        DeviceType deviceType = device.getDeviceConfiguration().getDeviceType();
        deviceType.addCalendar(calendar);
        CollectedCalendarInformation collectedCalendarInformation = mock(CollectedCalendarInformation.class);
        when(collectedCalendarInformation.isEmpty()).thenReturn(false);
        when(collectedCalendarInformation.getActiveCalendar()).thenReturn(Optional.empty());
        when(collectedCalendarInformation.getPassiveCalendar()).thenReturn(Optional.of(CALENDAR_NAME));
        device.calendars().updateCalendars(collectedCalendarInformation);
        return device;
    }

    @Test
    @Transactional
    public void testDeviceEffectiveCalendar() {
        Device device = createSimpleDeviceWithOneCalendar();
        assertThat(device.calendars().getPassive()).isPresent();
        PassiveCalendar passiveCalendar = device.calendars().getPassive().get();
        assertThat(passiveCalendar.getAllowedCalendar().getCalendar().get().getName()).isEqualTo("Calendar");
    }

    private Calendar createCalendar() {
        CalendarService calendarService = inMemoryPersistence.
                getCalendarService();
        Category category = calendarService.findCategoryByName(OutOfTheBoxCategory.TOU.name()).get();
        EventSet eventSet = calendarService.newEventSet("eventset")
                .addEvent("On peak").withCode(3)
                .addEvent("Off peak").withCode(5)
                .addEvent("Demand response").withCode(97)
                .add();

        return calendarService
                .newCalendar(CALENDAR_NAME, category, Year.of(2010), eventSet)
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