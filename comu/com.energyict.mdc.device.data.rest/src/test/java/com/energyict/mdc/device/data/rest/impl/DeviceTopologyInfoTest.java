package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 2/05/2016
 * Time: 13:33
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceTopologyInfoTest {

    private final static long DEVICE_ID = 222L;
    private final static String DEVICE_MRID  = "mRID";
    private final static String DEVICE_SERIAL_NUMBER = "serialNumber";
    private final static String DEVICE_TYPE_NAME = "deviceTypeName";
    private final static String DEVICE_CONFIGURATION_NAME = "deviceConfigurarionName";

    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private DeviceType deviceType;
    @Mock
    private Device device;

    @Test
    public void testFromDevice(){
        when(device.getId()).thenReturn(DEVICE_ID);
        when(device.getmRID()).thenReturn(DEVICE_MRID);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getSerialNumber()).thenReturn(DEVICE_SERIAL_NUMBER);
        when(deviceType.getName()).thenReturn(DEVICE_TYPE_NAME);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(deviceConfiguration.getName()).thenReturn(DEVICE_CONFIGURATION_NAME);

        Instant initialTimestamp = LocalDateTime.of(2014, 12, 1, 12, 0).toInstant(ZoneOffset.UTC);

        DeviceTopologyInfo info = DeviceTopologyInfo.from(device, Optional.of(initialTimestamp));

        assertThat(info.id).isEqualTo(DEVICE_ID);
        assertThat(info.mRID).isEqualTo(DEVICE_MRID);
        assertThat(info.serialNumber).isEqualTo(DEVICE_SERIAL_NUMBER);
        assertThat(info.deviceTypeName).isEqualTo(DEVICE_TYPE_NAME);
        assertThat(info.deviceConfigurationName).isEqualTo(DEVICE_CONFIGURATION_NAME);
        assertThat(info.creationTime).isEqualTo(initialTimestamp.toEpochMilli());
    }
}
