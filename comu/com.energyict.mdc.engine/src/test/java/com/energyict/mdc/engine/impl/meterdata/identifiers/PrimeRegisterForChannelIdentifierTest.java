package com.energyict.mdc.engine.impl.meterdata.identifiers;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

import com.energyict.protocolimplv2.identifiers.PrimeRegisterForChannelIdentifier;

import java.util.ArrayList;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link PrimeRegisterForChannelIdentifier} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (17:33)
 */
public class PrimeRegisterForChannelIdentifierTest {

    private static final int CHANNEL_INDEX = 2;

    private static final ObisCode testObisCode = ObisCode.fromString("1.0.1.8.0.255");
    private static final ObisCode testDeviceRegisterObisCode = ObisCode.fromString("1.0.1.9.0.255");

    @Test
    public void testGetObisCode () {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        PrimeRegisterForChannelIdentifier identifier = new PrimeRegisterForChannelIdentifier(deviceIdentifier, testObisCode, testObisCode, CHANNEL_INDEX);

        // Business method
        ObisCode obisCode = identifier.getObisCode();

        // Asserts
        assertThat(testObisCode).isEqualTo(obisCode);
    }

    @Test
    public void testGetDeviceRegisterObisCode () {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        PrimeRegisterForChannelIdentifier identifier = new PrimeRegisterForChannelIdentifier(deviceIdentifier, testObisCode, testDeviceRegisterObisCode, CHANNEL_INDEX);

        // Business method
        ObisCode obisCode = identifier.getDeviceRegisterObisCode();

        // Asserts
        assertThat(testDeviceRegisterObisCode).isEqualTo(obisCode);
    }

    @Test
    public void testToStringPrintsDeviceIdentifier() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        PrimeRegisterForChannelIdentifier identifier = new PrimeRegisterForChannelIdentifier(deviceIdentifier, testObisCode, testDeviceRegisterObisCode, CHANNEL_INDEX);

        // Business method
        identifier.toString();

        // Asserts
        verify(deviceIdentifier).toString();
    }

    @Test(expected = NotFoundException.class)
    public void testDeviceDoesNotExist () {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        doThrow(NotFoundException.class).when(deviceIdentifier).findDevice();
        PrimeRegisterForChannelIdentifier identifier = new PrimeRegisterForChannelIdentifier(deviceIdentifier, testObisCode, testObisCode, CHANNEL_INDEX);

        // Business method
        identifier.findRegister();
    }

    @Test(expected = NotFoundException.class)
    public void testDeviceDoesNotHaveEnoughChannels () {
        BaseDevice device = mock(BaseDevice.class);
        when(device.getChannels()).thenReturn(new ArrayList<BaseChannel>(0));
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        PrimeRegisterForChannelIdentifier identifier = new PrimeRegisterForChannelIdentifier(deviceIdentifier, testObisCode, testObisCode, CHANNEL_INDEX);

        // Business method
        identifier.findRegister();
    }

    @Test(expected = NotFoundException.class)
    public void testChannelDoesNotHaveAPrimeRegister () {
        BaseDevice device = mock(BaseDevice.class);
        when(device.getRegisterWithDeviceObisCode(testObisCode)).thenReturn(null);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        PrimeRegisterForChannelIdentifier identifier = new PrimeRegisterForChannelIdentifier(deviceIdentifier, testObisCode, testObisCode, CHANNEL_INDEX);

        // Business method
        identifier.findRegister();
    }

    @Test
    public void testFindRegister () {
        BaseRegister register = mock(BaseRegister.class);
        BaseDevice device = mock(BaseDevice.class);
        when(device.getRegisterWithDeviceObisCode(testObisCode)).thenReturn(register);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        PrimeRegisterForChannelIdentifier identifier = new PrimeRegisterForChannelIdentifier(deviceIdentifier, testObisCode, testObisCode, 1);

        // Business method
        BaseRegister needsChecking = identifier.findRegister();

        // Asserts
        assertThat(needsChecking).isEqualTo(register);
    }

}