/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.cache;

import com.elster.jupiter.events.LocalEvent;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.EngineService;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceCacheDeleteWhenDeviceIsRemovedHandlerTest {

    @Mock
    private Device device;
    @Mock
    private LocalEvent localEvent;
    @Mock
    private EngineService engineService;
    @Mock
    private DeviceCache deviceCache;

    @Before
    public void setup() {
        when(localEvent.getSource()).thenReturn(device);
    }

    @Test
    public void cacheIsPresentTest() {
        when(engineService.findDeviceCacheByDevice(device)).thenReturn(Optional.of(deviceCache));
        DeviceCacheDeleteWhenDeviceIsRemovedHandler deviceCacheDeleteWhenDeviceIsRemovedHandler = new DeviceCacheDeleteWhenDeviceIsRemovedHandler(engineService);
        deviceCacheDeleteWhenDeviceIsRemovedHandler.handle(localEvent);

        verify(deviceCache).delete();
    }
}