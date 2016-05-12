package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.PassiveEffectiveCalendar;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.Year;
import java.util.Collections;
import java.util.TimeZone;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PassiveEffectiveCalendarImplTest extends PersistenceIntegrationTest {

    private Device createSimpleDeviceWithOneCalendar() {
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "DeviceName", "MyUniqueID");
        Calendar calendar = createCalendar();
        DeviceType deviceType = device.getDeviceConfiguration().getDeviceType();
        deviceType.addCalendar(calendar);
        PassiveEffectiveCalendarImpl passiveEffectiveCalendar = new PassiveEffectiveCalendarImpl();
        passiveEffectiveCalendar.setAllowedCalendar(deviceType.getAllowedCalendars().get(0));
        passiveEffectiveCalendar.setDevice(device);
        passiveEffectiveCalendar.setActivationDate(Instant.now());
        device.setPassiveCalendars(Collections.singletonList(passiveEffectiveCalendar));
        return device;
    }

    @Test
    @Transactional
    public void testDeviceEffectiveCalendar() {
        Device device = createSimpleDeviceWithOneCalendar();
        assertThat(device.getPassiveCalenders()).hasSize(1);
        PassiveEffectiveCalendar passiveEffectiveCalendar = device.getPassiveCalenders().get(0);
        assertThat(passiveEffectiveCalendar.getAllowedCalendar().getCalendar().get().getName()).isEqualTo("Calendar");
    }

    private Calendar createCalendar() {
        return inMemoryPersistence.
                getCalendarService().newCalendar("Calendar", TimeZone.getTimeZone("Europe/Brussels"), Year.of(2010))
                .endYear(Year.of(2020))
                .description("Description remains to be completed :-)")
                .mRID("Calendar")
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
