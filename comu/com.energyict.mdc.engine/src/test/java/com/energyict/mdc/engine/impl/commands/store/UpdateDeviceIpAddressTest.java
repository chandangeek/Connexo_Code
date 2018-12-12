/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.device.data.impl.identifiers.DeviceIdentifierById;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.meterdata.DeviceConnectionProperty;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author sva
 * @since 30/09/13 - 9:04
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateDeviceIpAddressTest {
    private static final long DEVICE_ID = 1;
    private static final String IP_ADDRESS = "10.0.1.50:4059";
    private static final String CONNECTION_TASK_PROPERTY_NAME = "connectionTaskPropertyName";
    @Mock
    private DeviceCommand.ServiceProvider serviceProvider;

    @Test
    public void testToJournalMessageDescription() throws Exception {
        final DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID);
        final DeviceConnectionProperty deviceIpAddress = new DeviceConnectionProperty(deviceIdentifier, IP_ADDRESS, CONNECTION_TASK_PROPERTY_NAME);
        UpdateDeviceConnectionProperty command = new UpdateDeviceConnectionProperty(deviceIpAddress, null, serviceProvider);

        // Business method
        final String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessage).contains("Update device connection property {deviceIdentifier: device having id 1; connection property name: connectionTaskPropertyName; connection property value: 10.0.1.50:4059}");
    }
}
