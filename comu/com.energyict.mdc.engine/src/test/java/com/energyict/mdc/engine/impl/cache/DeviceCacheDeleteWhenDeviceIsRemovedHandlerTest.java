package com.energyict.mdc.engine.impl.cache;

import com.elster.jupiter.events.LocalEvent;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.EngineService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 30/06/15
 * Time: 15:55
 */
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