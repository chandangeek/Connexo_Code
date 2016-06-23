package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;

import com.jayway.jsonpath.JsonModel;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class UsagePointResourceTest extends UsagePointApplicationJerseyTest {

    @Mock
    private UsagePoint usagePoint;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private Meter meter;
    @Mock
    private Device device;
    @Mock
    private DeviceType deviceType;
    @Mock
    private State deviceState;
    @Mock
    private MeterInfoFactory meterInfoFactory;

    @Before
    public void before() {
        when(meteringService.findUsagePoint("testUP")).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getMRID()).thenReturn("testUP");
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(meterActivation.getStart()).thenReturn(Instant.ofEpochMilli(1410774620100L));
        when(meter.getMRID()).thenReturn("testD");
        when(deviceState.getName()).thenReturn("Decommissioned");
        when(device.getSerialNumber()).thenReturn("123");
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getState()).thenReturn(deviceState);
        when(deviceType.getId()).thenReturn(1L);
        when(deviceType.getName()).thenReturn("testDT");
        when(deviceService.findByUniqueMrid("testD")).thenReturn(Optional.of(device));
        doReturn(Collections.singletonList(meterActivation)).when(usagePoint).getMeterActivations();
    }

    @Test
    public void testDevicesHistory() {
        String json = target("/usagepoints/testUP/history/devices").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<String>get("$.devices[0].mRID")).isEqualTo("testD");
        assertThat(jsonModel.<String>get("$.devices[0].serialNumber")).isEqualTo("123");
        assertThat(jsonModel.<String>get("$.devices[0].state")).isEqualTo("Decommissioned");
        assertThat(jsonModel.<Number>get("$.devices[0].start")).isEqualTo(1410774620100L);
        assertThat(jsonModel.<Boolean>get("$.devices[0].active")).isEqualTo(true);
        assertThat(jsonModel.<Number>get("$.devices[0].deviceType.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.devices[0].deviceType.name")).isEqualTo("testDT");
    }
}
