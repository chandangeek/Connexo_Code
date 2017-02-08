/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class DeviceCalendarTest {

    @Mock
    private DeviceIdentifier deviceIdentifier;

    @Test
    public void isNotEmptyWhenActiveCalendarIsPresentTest() {
        DeviceCalendar deviceCalendar = getTestInstance();
        deviceCalendar.setActiveCalendar("MyActiveCalendarName");

        assertThat(deviceCalendar.getActiveCalendar().isPresent()).isTrue();
        assertThat(deviceCalendar.getPassiveCalendar().isPresent()).isFalse();
        assertThat(deviceCalendar.isEmpty()).isFalse();
    }

    @Test
    public void isNotEmptyWhenPassiveCalendarIsPresentTest() {
        DeviceCalendar deviceCalendar = getTestInstance();
        deviceCalendar.setPassiveCalendar("MyPassiveCalendarName");

        assertThat(deviceCalendar.getActiveCalendar().isPresent()).isFalse();
        assertThat(deviceCalendar.getPassiveCalendar().isPresent()).isTrue();
        assertThat(deviceCalendar.isEmpty()).isFalse();
    }

    @Test
    public void isNotEmptyWhenBothArePresentTest() {
        DeviceCalendar deviceCalendar = getTestInstance();
        deviceCalendar.setActiveCalendar("MyActiveCalendarName");
        deviceCalendar.setPassiveCalendar("MyPassiveCalendarName");

        assertThat(deviceCalendar.getActiveCalendar().isPresent()).isTrue();
        assertThat(deviceCalendar.getPassiveCalendar().isPresent()).isTrue();
        assertThat(deviceCalendar.isEmpty()).isFalse();
    }

    @Test
    public void isEmptyTest() {
        DeviceCalendar deviceCalendar = getTestInstance();

        assertThat(deviceCalendar.getActiveCalendar().isPresent()).isFalse();
        assertThat(deviceCalendar.getPassiveCalendar().isPresent()).isFalse();
        assertThat(deviceCalendar.isEmpty()).isTrue();
    }

    private DeviceCalendar getTestInstance() {
        return new DeviceCalendar(deviceIdentifier);
    }

}