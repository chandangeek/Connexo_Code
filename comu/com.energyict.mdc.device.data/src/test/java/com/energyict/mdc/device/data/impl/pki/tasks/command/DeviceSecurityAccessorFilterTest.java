package com.energyict.mdc.device.data.impl.pki.tasks.command;

import com.elster.jupiter.pki.SecurityAccessorType;

import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.SecurityAccessorTypeOnDeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeviceSecurityAccessorFilterTest {

    @Mock
    private SecurityAccessor securityAccessor;
    @Mock
    private Device device;
    @Mock
    private DeviceType deviceTpe;
    @Mock
    private SecurityAccessorType securityAccessorType;
    @Mock
    private SecurityAccessorTypeOnDeviceType securityAccessorTypeOnDevice;

    @Test(expected = CommandAbortException.class)
    public void noSuchSecurityAccessorOnDeviceType() throws CommandAbortException {
        Mockito.when(securityAccessor.getSecurityAccessorType()).thenReturn(securityAccessorType);
        Mockito.when(securityAccessor.getDevice()).thenReturn(device);
        Mockito.when(device.getDeviceType()).thenReturn(deviceTpe);
        Mockito.when(deviceTpe.getSecurityAccessor(securityAccessor.getSecurityAccessorType())).thenReturn(Optional.empty());
        DeviceSecurityAccessorFilter deviceSecurityAccessorFilter = new DeviceSecurityAccessorFilter();
        deviceSecurityAccessorFilter.run(securityAccessor);
    }

    @Test(expected = CommandAbortException.class)
    public void notConfiguredSecurityAccessorOnDeviceType() throws CommandAbortException {
        Mockito.when(securityAccessor.getSecurityAccessorType()).thenReturn(securityAccessorType);
        Mockito.when(securityAccessor.getDevice()).thenReturn(device);
        Mockito.when(device.getDeviceType()).thenReturn(deviceTpe);
        Mockito.when(deviceTpe.getSecurityAccessor(securityAccessor.getSecurityAccessorType())).thenReturn(Optional.of(securityAccessorTypeOnDevice));
        DeviceSecurityAccessorFilter deviceSecurityAccessorFilter = new DeviceSecurityAccessorFilter();
        Mockito.when(securityAccessorTypeOnDevice.isRenewalConfigured()).thenReturn(false);
        deviceSecurityAccessorFilter.run(securityAccessor);
    }

    @Test
    public void configuredSecurityAccessorOnDeviceType() throws CommandAbortException {
        Mockito.when(securityAccessor.getSecurityAccessorType()).thenReturn(securityAccessorType);
        Mockito.when(securityAccessor.getDevice()).thenReturn(device);
        Mockito.when(device.getDeviceType()).thenReturn(deviceTpe);
        Mockito.when(deviceTpe.getSecurityAccessor(securityAccessor.getSecurityAccessorType())).thenReturn(Optional.of(securityAccessorTypeOnDevice));
        DeviceSecurityAccessorFilter deviceSecurityAccessorFilter = new DeviceSecurityAccessorFilter();
        Mockito.when(securityAccessorTypeOnDevice.isRenewalConfigured()).thenReturn(true);
        deviceSecurityAccessorFilter.run(securityAccessor);
    }

}
