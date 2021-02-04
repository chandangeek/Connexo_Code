/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.NoDeviceCommandServices;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link DeviceConnectionProperty} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (16:11)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceIpAddressTest {
    private static final int DEVICE_ID = 97;
    private static final String IP_ADDRESS = "192.168.2.100";
    private static final String IP_ADDRESS_PROPERTY_NAME = "propertyValue";

    @Test
    public void testConstructorDoesNotThrowExceptions() {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID);

        // Business method
        new DeviceConnectionProperty(deviceIdentifier, IP_ADDRESS, IP_ADDRESS_PROPERTY_NAME);

        // Simply asserting that no exceptions are thrown
    }

    @Test
    public void testIsAlwaysConfiguredOnComTasks() {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID);
        DeviceConnectionProperty deviceIpAddress = new DeviceConnectionProperty(deviceIdentifier, IP_ADDRESS, IP_ADDRESS_PROPERTY_NAME);
        DataCollectionConfiguration comTask = mock(DataCollectionConfiguration.class);

        // Business method
        boolean isConfiguredIn = deviceIpAddress.isConfiguredIn(comTask);

        // Asserts
        assertThat(isConfiguredIn).isTrue();
    }

    @Test
    public void testToDeviceCommand() {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID);
        DeviceConnectionProperty deviceIpAddress = new DeviceConnectionProperty(deviceIdentifier, IP_ADDRESS, IP_ADDRESS_PROPERTY_NAME);

        // Business method
        NoDeviceCommandServices serviceProvider = new NoDeviceCommandServices();
        DeviceCommand command = deviceIpAddress.toDeviceCommand(new MeterDataStoreCommandImpl(null, serviceProvider), serviceProvider);

        // Asserts
        assertThat(command).isNotNull();
    }

    @Test
    public void testGetDeviceIdentifier() {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID);
        DeviceConnectionProperty deviceIpAddress = new DeviceConnectionProperty(deviceIdentifier, IP_ADDRESS, IP_ADDRESS_PROPERTY_NAME);

        // Business method
        DeviceIdentifier needsChecking = deviceIpAddress.getDeviceIdentifier();

        // Asserts
        assertThat(needsChecking).isSameAs(deviceIdentifier);
    }

    @Test
    public void testGetIpAddress() {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID);
        DeviceConnectionProperty deviceIpAddress = new DeviceConnectionProperty(deviceIdentifier, IP_ADDRESS, IP_ADDRESS_PROPERTY_NAME);

        // Business method
        String propertyValue = deviceIpAddress.getConnectionPropertyNameAndValue().get(IP_ADDRESS_PROPERTY_NAME).toString();

        // Asserts
        assertThat(propertyValue).isEqualTo(IP_ADDRESS);
    }

    @Test
    public void testGetConnectionTypePropertyName() {
        DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(DEVICE_ID);
        DeviceConnectionProperty deviceIpAddress = new DeviceConnectionProperty(deviceIdentifier, IP_ADDRESS, IP_ADDRESS_PROPERTY_NAME);

        // Business method
        String connectionTaskPropertyName = deviceIpAddress.getConnectionPropertyNameAndValue().keySet().stream().findFirst().get();

        // Asserts
        assertThat(connectionTaskPropertyName).isEqualTo(IP_ADDRESS_PROPERTY_NAME);
    }

}
