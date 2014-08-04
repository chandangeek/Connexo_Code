package com.energyict.mdc.engine.impl.protocol.inbound;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.impl.DeviceIdentifierById;
import com.energyict.mdc.protocol.api.device.BaseDevice;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.DeviceIdentifierById} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-12 (11:19)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceIdentifierByIdTest {

    private static final long DEVICE_ID = 97;

    @Mock
    private DeviceDataService deviceDataService;

    @Test(expected = NotFoundException.class)
    public void testDeviceDoesNotExist () {
        when(this.deviceDataService.findDeviceById(DEVICE_ID)).thenReturn(null);

        // Business method
        new DeviceIdentifierById(DEVICE_ID, this.deviceDataService).findDevice();

        // Expected a NotFoundException
    }

    @Test
    public void testOnlyOneDevice () {
        Device device = mock(Device.class);
        when(this.deviceDataService.findDeviceById(DEVICE_ID)).thenReturn(device);

        // Business method
        BaseDevice foundDevice = new DeviceIdentifierById(DEVICE_ID, this.deviceDataService).findDevice();

        // Asserts
        assertThat(foundDevice).isEqualTo(device);
    }

}