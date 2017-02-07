/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.cache;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceCacheImplTest extends EqualsContractTest {

    private final long deviceId = 4456;
    private final long otherDeviceId = 778654;
    private final String content = "ThisIsMyCachedContent";
    private final String otherContent = "ThisIsMyOtherContent";
    private DeviceCacheImpl deviceCache;

    @Mock
    private DataModel dataModel;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private Device device;
    @Mock
    private Device otherDevice;
    @Mock
    private DeviceProtocolCache deviceProtocolCache;
    @Mock
    private DeviceProtocolCache otherDeviceProtocolCache;

    @Before
    public void equalsContractSetUp() {
        when(device.getId()).thenReturn(deviceId);
        when(otherDevice.getId()).thenReturn(otherDeviceId);
        when(protocolPluggableService.marshallDeviceProtocolCache(deviceProtocolCache)).thenReturn(content);
        when(protocolPluggableService.marshallDeviceProtocolCache(otherDeviceProtocolCache)).thenReturn(otherContent);
        super.equalsContractSetUp();
    }

    @Override
    protected Object getInstanceA() {
        if (this.deviceCache == null) {
            deviceCache = new DeviceCacheImpl(dataModel, protocolPluggableService).initialize(device, deviceProtocolCache);
        }
        return deviceCache;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new DeviceCacheImpl(dataModel, protocolPluggableService).initialize(device, deviceProtocolCache);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        final DeviceCacheImpl deviceCache1 = new DeviceCacheImpl(dataModel, protocolPluggableService).initialize(otherDevice, deviceProtocolCache);
        final DeviceCacheImpl deviceCache2 = new DeviceCacheImpl(dataModel, protocolPluggableService).initialize(device, otherDeviceProtocolCache);
        return ImmutableList.of(deviceCache1, deviceCache2);
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}