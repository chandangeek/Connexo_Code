/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.fsm.State;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.topology.DeviceTopology;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.TopologyTimeline;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceTopologyInfoTest {

    private static final long DEVICE_ID = 222L;
    private static final String DEVICE_NAME = "name";
    private static final String DEVICE_SERIAL_NUMBER = "serialNumber";
    private static final String DEVICE_TYPE_NAME = "deviceTypeName";
    private static final String DEVICE_CONFIGURATION_NAME = "deviceConfigurarionName";

    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private DeviceType deviceType;
    @Mock
    private Device device;
    @Mock
    private TopologyService topologyService;
    @Mock
    private Clock clock;
    @Mock
    private DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    @Before
    public void setup() {
        when(topologyService.findDataloggerReference(any(Device.class), any(Instant.class))).thenReturn(Optional.empty());
    }

    @Test
    public void testFromDevice(){
        Instant initialTimestamp = LocalDateTime.of(2014, 12, 1, 12, 0).toInstant(ZoneOffset.UTC);
        when(clock.instant()).thenReturn(initialTimestamp);
        DeviceTopology deviceTopology = mock(DeviceTopology.class);
        Device gateway = mock(Device.class);
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.of(gateway));
        when(topologyService.getPhysicalTopology(gateway, Range.atMost(initialTimestamp))).thenReturn(deviceTopology);

        TopologyTimeline topologyTimeline = mock(TopologyTimeline.class);
        Set<Device> allDevices = new HashSet<>();
        allDevices.add(device);
        when(topologyTimeline.getAllDevices()).thenReturn(allDevices);
        when(topologyTimeline.mostRecentlyAddedOn(device)).thenReturn(Optional.of(Instant.ofEpochMilli(10L)));
        when(deviceTopology.timelined()).thenReturn(topologyTimeline);
        when(topologyService.getPysicalTopologyTimeline(gateway)).thenReturn(topologyTimeline);

        State state = mock(State.class);
        when(state.getName()).thenReturn("dlc.default.inStock");
        when(device.getState()).thenReturn(state);

        when(device.getId()).thenReturn(DEVICE_ID);
        when(device.getName()).thenReturn(DEVICE_NAME);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getSerialNumber()).thenReturn(DEVICE_SERIAL_NUMBER);
        when(deviceType.getName()).thenReturn(DEVICE_TYPE_NAME);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(deviceConfiguration.getName()).thenReturn(DEVICE_CONFIGURATION_NAME);
        when(device.getCreateTime()).thenReturn(initialTimestamp);

        DeviceTopologyInfo info = DeviceTopologyInfo.from(device, Optional.of(initialTimestamp), deviceLifeCycleConfigurationService);

        assertThat(info.id).isEqualTo(DEVICE_ID);
        assertThat(info.name).isEqualTo(DEVICE_NAME);
        assertThat(info.serialNumber).isEqualTo(DEVICE_SERIAL_NUMBER);
        assertThat(info.deviceTypeName).isEqualTo(DEVICE_TYPE_NAME);
        assertThat(info.deviceConfigurationName).isEqualTo(DEVICE_CONFIGURATION_NAME);
        assertThat(info.creationTime).isEqualTo(initialTimestamp.toEpochMilli());
    }
}
