package com.energyict.mdc.device.data.impl.pki.tasks.command;

import com.elster.jupiter.pki.SecurityAccessorType;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;

import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CheckSecuritySetsTest {

    @Mock
    private Logger logger;
    @Mock
    private SecurityAccessor secAcc;
    @Mock
    private SecurityAccessorType keyAccType;
    @Mock
    private Device device;
    @Mock
    private DeviceConfiguration deviceConfiguration;

    @Test(expected = CommandErrorException.class)
    public void notAttachedToDevice() throws CommandErrorException, CommandAbortException {
        Mockito.when(secAcc.getSecurityAccessorType()).thenReturn(keyAccType);
        long keyAccTypeId = 1L;
        Mockito.when(keyAccType.getId()).thenReturn(keyAccTypeId);
        new CheckSecuritySets(logger).run(secAcc);
        Mockito.verify(secAcc, Mockito.times(1)).getDevice();
        Mockito.verifyNoMoreInteractions(secAcc, device, keyAccType);
    }


    @Test(expected = CommandErrorException.class)
    public void noDeviceConfiguration() throws CommandErrorException, CommandAbortException {
        Mockito.when(secAcc.getDevice()).thenReturn(device);
        Mockito.when(secAcc.getSecurityAccessorType()).thenReturn(keyAccType);
        long keyAccTypeId = 1L;
        Mockito.when(keyAccType.getId()).thenReturn(keyAccTypeId);
        new CheckSecuritySets(logger).run(secAcc);
        Mockito.verify(secAcc, Mockito.times(1)).getDevice();
        Mockito.verify(secAcc, Mockito.times(1)).getSecurityAccessorType();
        Mockito.verify(keyAccType, Mockito.times(1)).getId();
        Mockito.verify(keyAccType, Mockito.times(1)).getName();
        Mockito.verify(device, Mockito.times(1)).getName();
        Mockito.verify(device, Mockito.times(1)).getDeviceConfiguration();
        Mockito.verifyNoMoreInteractions(secAcc, device, keyAccType);
    }

    @Test(expected = CommandAbortException.class)
    public void noMatchingPropSet() throws CommandErrorException, CommandAbortException {
        Mockito.when(secAcc.getDevice()).thenReturn(device);
        Mockito.when(secAcc.getSecurityAccessorType()).thenReturn(keyAccType);
        Mockito.when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        Mockito.when(deviceConfiguration.isConfigured(keyAccType)).thenReturn(false);
        long keyAccTypeId = 1L;
        Mockito.when(keyAccType.getId()).thenReturn(keyAccTypeId);
        new CheckSecuritySets(logger).run(secAcc);
        Mockito.verify(secAcc, Mockito.times(1)).getDevice();
        Mockito.verify(secAcc, Mockito.times(1)).getSecurityAccessorType();
        Mockito.verify(keyAccType, Mockito.times(1)).getId();
        Mockito.verify(keyAccType, Mockito.times(1)).getName();
        Mockito.verify(device, Mockito.times(1)).getName();
        Mockito.verify(device, Mockito.times(1)).getDeviceConfiguration();
        Mockito.verifyNoMoreInteractions(secAcc, device, keyAccType);
    }

    @Test
    public void allOk() throws CommandErrorException, CommandAbortException {
        Mockito.when(secAcc.getDevice()).thenReturn(device);
        Mockito.when(secAcc.getSecurityAccessorType()).thenReturn(keyAccType);
        Mockito.when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        Mockito.when(deviceConfiguration.isConfigured(keyAccType)).thenReturn(true);
        new CheckSecuritySets(logger).run(secAcc);
        Mockito.verify(secAcc, Mockito.times(1)).getDevice();
        Mockito.verify(secAcc, Mockito.times(1)).getSecurityAccessorType();
        Mockito.verify(keyAccType, Mockito.times(1)).getName();
        Mockito.verify(device, Mockito.times(1)).getName();
        Mockito.verify(device, Mockito.times(1)).getDeviceConfiguration();
        Mockito.verifyNoMoreInteractions(secAcc, device, keyAccType);
    }
}
