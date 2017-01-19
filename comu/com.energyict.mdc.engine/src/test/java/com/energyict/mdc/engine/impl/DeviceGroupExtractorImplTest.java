package com.energyict.mdc.engine.impl;

import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.upl.DeviceGroupExtractor;

import java.time.Clock;
import java.time.Instant;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceGroupExtractorImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-19 (08:40)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceGroupExtractorImplTest {

    private static final long QUERY_GROUP_ID = 97;
    private static final long ENUMERATED_GROUP_ID = 101;

    @Mock
    private Clock clock;
    @Mock
    private DeviceService deviceService;
    @Mock
    private QueryEndDeviceGroup queryEndDeviceGroup;
    @Mock
    private EnumeratedEndDeviceGroup enumeratedEndDeviceGroup;

    @Before
    public void initializeMocks() {
        when(this.queryEndDeviceGroup.getId()).thenReturn(QUERY_GROUP_ID);
        when(this.enumeratedEndDeviceGroup.getId()).thenReturn(ENUMERATED_GROUP_ID);

        when(this.clock.instant()).thenReturn(Instant.now());
    }

    @Test
    public void getQueryEndDeviceGroupId() {
        DeviceGroupExtractor deviceGroupExtractor = this.getInstance();

        // Business method
        deviceGroupExtractor.id(this.queryEndDeviceGroup);

        // Asserts
        verify(this.queryEndDeviceGroup).getId();
    }

    @Test
    public void getEnumeratedEndDeviceGroupId() {
        DeviceGroupExtractor deviceGroupExtractor = this.getInstance();

        // Business method
        deviceGroupExtractor.id(this.enumeratedEndDeviceGroup);

        // Asserts
        verify(this.enumeratedEndDeviceGroup).getId();
    }

    @Test
    public void getQueryEndDeviceGroupMembers() {
        DeviceGroupExtractor deviceGroupExtractor = this.getInstance();

        // Business method
        deviceGroupExtractor.members(this.queryEndDeviceGroup);

        // Asserts
        verify(this.deviceService).findAllDevices(any(Condition.class));
    }

    @Test
    public void getEnumeratedEndDeviceGroupMembers() {
        DeviceGroupExtractor deviceGroupExtractor = this.getInstance();

        // Business method
        deviceGroupExtractor.members(this.enumeratedEndDeviceGroup);

        // Asserts
        verify(this.clock).instant();
        verify(this.enumeratedEndDeviceGroup).getMembers(this.clock.instant());
        verify(this.deviceService).findAllDevices(any(Condition.class));
    }

    private DeviceGroupExtractor getInstance() {
        return new DeviceGroupExtractorImpl(this.clock, this.deviceService);
    }
}