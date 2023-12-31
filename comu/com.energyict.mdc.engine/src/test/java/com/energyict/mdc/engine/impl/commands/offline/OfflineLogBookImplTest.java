/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.LogBookSpec;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.LogBook;
import com.energyict.mdc.common.masterdata.LogBookType;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.offline.OfflineLogBook;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link OfflineLogBookImpl} component
 *
 * @author sva
 * @since 10/12/12 - 15:23
 */
@RunWith(MockitoJUnitRunner.class)
public class OfflineLogBookImplTest {

    private static final long LOGBOOK_ID = 1;
    private static final long DEVICE_ID = 1;
    private static final long LOGBOOK_TYPE_ID = 123;
    private static final String DEVICE_SERIAL = "SerialNumber";
    private static final Instant LAST_LOGBOOK = Instant.ofEpochMilli(1355150108000L);  // Mon, 10 Dec 2012 14:35:08 GMT
    @Mock
    private IdentificationService identificationService;

    @Test
    public void goOfflineTest() {
        LogBookType logBookType = getMockedLogBookType();
        LogBookSpec logBookSpec = getMockedLogBookSpec(logBookType);
        Device device = getMockedDevice();
        LogBook logBook = getMockedLogBook(logBookType, logBookSpec, device);

        //Business Methods
        OfflineLogBook offlineLogBook = new OfflineLogBookImpl(logBook, this.identificationService);

        // asserts
        assertThat(offlineLogBook).isNotNull();
        assertThat(LOGBOOK_ID).isEqualTo(offlineLogBook.getLogBookId());
        assertThat(device.getId()).isEqualTo(offlineLogBook.getDeviceId());
        assertThat(device.getSerialNumber()).isEqualTo(offlineLogBook.getMasterSerialNumber());
        assertNotNull(offlineLogBook.getLastReading());
        assertThat(Date.from(LAST_LOGBOOK)).isEqualTo(offlineLogBook.getLastReading());
        assertThat(LOGBOOK_TYPE_ID).isEqualTo(offlineLogBook.getOfflineLogBookSpec().getLogBookTypeId());
    }

    private LogBook getMockedLogBook(LogBookType logBookType, LogBookSpec logBookSpec, Device device) {
        LogBook logBook = mock(LogBook.class);
        when(logBook.getId()).thenReturn(LOGBOOK_ID);
        when(logBook.getLogBookSpec()).thenReturn(logBookSpec);
        when(logBook.getLogBookType()).thenReturn(logBookType);
        when(logBook.getDevice()).thenReturn(device);
        when(logBook.getLastLogBook()).thenReturn(Optional.of(LAST_LOGBOOK));
        when(logBook.getLatestEventAdditionDate()).thenReturn(Optional.of(LAST_LOGBOOK));
        return logBook;
    }

    private Device getMockedDevice() {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        when(device.getSerialNumber()).thenReturn(DEVICE_SERIAL);
        return device;
    }

    private LogBookSpec getMockedLogBookSpec(LogBookType logBookType) {
        LogBookSpec logBookSpec = mock(LogBookSpec.class);
        when(logBookSpec.getLogBookType()).thenReturn(logBookType);
        when(logBookSpec.getDeviceConfiguration()).thenReturn(mock(DeviceConfiguration.class));
        return logBookSpec;
    }

    private LogBookType getMockedLogBookType() {
        LogBookType logBookType = mock(LogBookType.class);
        when(logBookType.getId()).thenReturn(LOGBOOK_TYPE_ID);
        return logBookType;
    }
}