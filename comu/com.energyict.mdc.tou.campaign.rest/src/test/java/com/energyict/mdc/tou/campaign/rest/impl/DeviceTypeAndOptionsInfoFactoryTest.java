/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.calendar.Calendar;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.TimeOfUseOptions;
import com.energyict.mdc.upl.messages.ProtocolSupportedCalendarOptions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceTypeAndOptionsInfoFactoryTest {

    private static DeviceConfigurationService deviceConfigurationService = mock(DeviceConfigurationService.class);
    private static DeviceType deviceType = mock(DeviceType.class);
    private static DeviceTypeAndOptionsInfoFactory deviceTypeAndOptionsInfoFactory;
    private static TimeOfUseOptions timeOfUseOptions = mock(TimeOfUseOptions.class);
    private static AllowedCalendar allowedCalendar = mock(AllowedCalendar.class);
    private static Calendar calendar = mock(Calendar.class);

    @BeforeClass
    public static void setUp() {
        when(deviceConfigurationService.findTimeOfUseOptions(deviceType)).thenReturn(Optional.of(timeOfUseOptions));
        deviceTypeAndOptionsInfoFactory = new DeviceTypeAndOptionsInfoFactory(deviceConfigurationService);
        when(deviceType.getId()).thenReturn(1L);
        when(deviceType.getName()).thenReturn("TestDeviceType");
        when(deviceType.getAllowedCalendars()).thenReturn(Collections.singletonList(allowedCalendar));
        when(allowedCalendar.isGhost()).thenReturn(false);
        when(allowedCalendar.getCalendar()).thenReturn(Optional.of(calendar));
        when(calendar.getId()).thenReturn(1L);
        when(allowedCalendar.getName()).thenReturn("TestCalendar");
    }

    @Test
    public void createTestFullCalendar() {
        Set<ProtocolSupportedCalendarOptions> protocolSupportedCalendarOptionsSet = new HashSet<>();
        protocolSupportedCalendarOptionsSet.add(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR);
        when(timeOfUseOptions.getOptions()).thenReturn(protocolSupportedCalendarOptionsSet);
        DeviceTypeAndOptionsInfo deviceTypeAndOptionsInfo = deviceTypeAndOptionsInfoFactory.create(deviceType);
        assertEquals(deviceTypeAndOptionsInfo.deviceType.id, 1L);
        assertEquals(deviceTypeAndOptionsInfo.deviceType.name, "TestDeviceType");
        assertEquals(deviceTypeAndOptionsInfo.calendars.get(0).id, 1L);
        assertEquals(deviceTypeAndOptionsInfo.calendars.get(0).name, "TestCalendar");
        assertTrue(deviceTypeAndOptionsInfo.fullCalendar);
        assertFalse(deviceTypeAndOptionsInfo.specialDays);
        assertFalse(deviceTypeAndOptionsInfo.withActivationDate);
    }

    @Test
    public void createTestWithActivation() {
        Set<ProtocolSupportedCalendarOptions> protocolSupportedCalendarOptionsSet = new HashSet<>();
        protocolSupportedCalendarOptionsSet.add(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATETIME);
        when(timeOfUseOptions.getOptions()).thenReturn(protocolSupportedCalendarOptionsSet);
        DeviceTypeAndOptionsInfo deviceTypeAndOptionsInfo = deviceTypeAndOptionsInfoFactory.create(deviceType);
        assertEquals(deviceTypeAndOptionsInfo.deviceType.id, 1L);
        assertEquals(deviceTypeAndOptionsInfo.deviceType.name, "TestDeviceType");
        assertEquals(deviceTypeAndOptionsInfo.calendars.get(0).id, 1L);
        assertEquals(deviceTypeAndOptionsInfo.calendars.get(0).name, "TestCalendar");
        assertTrue(deviceTypeAndOptionsInfo.fullCalendar);
        assertFalse(deviceTypeAndOptionsInfo.specialDays);
        assertTrue(deviceTypeAndOptionsInfo.withActivationDate);
    }

    @Test
    public void createTestSpecialDays() {
        Set<ProtocolSupportedCalendarOptions> protocolSupportedCalendarOptionsSet = new HashSet<>();
        protocolSupportedCalendarOptionsSet.add(ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR);
        when(timeOfUseOptions.getOptions()).thenReturn(protocolSupportedCalendarOptionsSet);
        DeviceTypeAndOptionsInfo deviceTypeAndOptionsInfo = deviceTypeAndOptionsInfoFactory.create(deviceType);
        assertEquals(deviceTypeAndOptionsInfo.deviceType.id, 1L);
        assertEquals(deviceTypeAndOptionsInfo.deviceType.name, "TestDeviceType");
        assertEquals(deviceTypeAndOptionsInfo.calendars.get(0).id, 1L);
        assertEquals(deviceTypeAndOptionsInfo.calendars.get(0).name, "TestCalendar");
        assertFalse(deviceTypeAndOptionsInfo.fullCalendar);
        assertTrue(deviceTypeAndOptionsInfo.specialDays);
        assertFalse(deviceTypeAndOptionsInfo.withActivationDate);
    }

    @Test
    public void createTestAllProtocols() {
        Set<ProtocolSupportedCalendarOptions> protocolSupportedCalendarOptionsSet = new HashSet<>();
        protocolSupportedCalendarOptionsSet.add(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR);
        protocolSupportedCalendarOptionsSet.add(ProtocolSupportedCalendarOptions.SEND_ACTIVITY_CALENDAR_WITH_DATETIME);
        protocolSupportedCalendarOptionsSet.add(ProtocolSupportedCalendarOptions.SEND_SPECIAL_DAYS_CALENDAR);
        when(timeOfUseOptions.getOptions()).thenReturn(protocolSupportedCalendarOptionsSet);
        DeviceTypeAndOptionsInfo deviceTypeAndOptionsInfo = deviceTypeAndOptionsInfoFactory.create(deviceType);
        assertEquals(deviceTypeAndOptionsInfo.deviceType.id, 1L);
        assertEquals(deviceTypeAndOptionsInfo.deviceType.name, "TestDeviceType");
        assertEquals(deviceTypeAndOptionsInfo.calendars.get(0).id, 1L);
        assertEquals(deviceTypeAndOptionsInfo.calendars.get(0).name, "TestCalendar");
        assertTrue(deviceTypeAndOptionsInfo.fullCalendar);
        assertTrue(deviceTypeAndOptionsInfo.specialDays);
        assertTrue(deviceTypeAndOptionsInfo.withActivationDate);
    }

}
