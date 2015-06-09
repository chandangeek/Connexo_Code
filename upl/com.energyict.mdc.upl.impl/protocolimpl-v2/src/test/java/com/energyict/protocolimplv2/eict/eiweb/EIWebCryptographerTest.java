package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.cbo.NotFoundException;
import com.energyict.cbo.Password;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.channels.inbound.EIWebConnectionType;
import com.energyict.mdc.exceptions.ComServerExceptionFactoryProvider;
import com.energyict.mdc.exceptions.DefaultComServerExceptionFactoryProvider;
import com.energyict.mdc.ports.InboundComPort;
import com.energyict.mdc.protocol.exceptions.CommunicationException;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.InboundDAO;
import com.energyict.mdc.protocol.inbound.crypto.MD5Seed;
import com.energyict.mdc.protocol.security.SecurityProperty;
import com.energyict.mdc.tasks.InboundConnectionTask;
import com.energyict.mdw.core.Device;
import org.junit.*;

import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link EIWebCryptographer} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-19 (14:37)
 */
public class EIWebCryptographerTest {

    private static final String MAC_ADDRESS_VALUE = "0090C2D49541";

    /**
     * Test that failure to find the related device
     * does not break the EIWebCryptographer component.
     */
    @Test(expected = NotFoundException.class)
    public void testBuildMD5SeedForNonExistingDevice () {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        InboundDAO inboundDAO = mock(InboundDAO.class);
        InboundComPort comPort = mock(InboundComPort.class);
        doThrow(NotFoundException.class).when(inboundDAO).getDeviceConnectionTypeProperties(deviceIdentifier, comPort);

        EIWebCryptographer cryptographer = new EIWebCryptographer(inboundDAO, comPort);

        // Business method
        cryptographer.buildMD5Seed(deviceIdentifier, "1234");

        // Asserts: expected NotFoundException
    }

    /**
     * Test that a {@link CommunicationException} is thrown
     * when the device is not ready for inbound communication
     * because not {@link InboundConnectionTask}s are defined against it.
     */
    @Test(expected = CommunicationException.class)
    public void testBuildMD5SeedWithoutConnectionTypeProperties () {
        ComServerExceptionFactoryProvider.instance.set(new DefaultComServerExceptionFactoryProvider());

        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        InboundDAO inboundDAO = mock(InboundDAO.class);
        InboundComPort comPort = mock(InboundComPort.class);
        when(inboundDAO.getDeviceConnectionTypeProperties(deviceIdentifier, comPort)).thenReturn(null);

        EIWebCryptographer cryptographer = new EIWebCryptographer(inboundDAO, comPort);

        // Business method
        cryptographer.buildMD5Seed(deviceIdentifier, "1234");

        // Asserts: expected CommunicationException
    }

    @Test
    public void testBuildMD5SeedExistingDevice () {
        Device device = mock(Device.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        InboundDAO inboundDAO = mock(InboundDAO.class);
        InboundComPort comPort = mock(InboundComPort.class);
        TypedProperties connectionTypeProperties = TypedProperties.empty();
        connectionTypeProperties.setProperty(EIWebConnectionType.MAC_ADDRESS_PROPERTY_NAME, MAC_ADDRESS_VALUE);
        when(inboundDAO.getDeviceConnectionTypeProperties(deviceIdentifier, comPort)).thenReturn(connectionTypeProperties);
        SecurityProperty encryptionPassword = mock(SecurityProperty.class);
        when(encryptionPassword.getValue()).thenReturn(new Password("EIWebCryptographerTest"));
        when(inboundDAO.getDeviceProtocolSecurityProperties(deviceIdentifier, comPort)).thenReturn(Arrays.asList(encryptionPassword));

        EIWebCryptographer cryptographer = new EIWebCryptographer(inboundDAO, comPort);

        // Business method
        MD5Seed md5Seed = cryptographer.buildMD5Seed(deviceIdentifier, "1234");

        // Asserts
        assertThat(md5Seed).isNotNull();
    }

    @Test
    public void testWasUsed () {
        Device device = mock(Device.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceIdentifier.findDevice()).thenReturn(device);
        InboundDAO inboundDAO = mock(InboundDAO.class);
        InboundComPort comPort = mock(InboundComPort.class);
        TypedProperties connectionTypeProperties = TypedProperties.empty();
        connectionTypeProperties.setProperty(EIWebConnectionType.MAC_ADDRESS_PROPERTY_NAME, MAC_ADDRESS_VALUE);
        when(inboundDAO.getDeviceConnectionTypeProperties(deviceIdentifier, comPort)).thenReturn(connectionTypeProperties);
        SecurityProperty encryptionPassword = mock(SecurityProperty.class);
        when(encryptionPassword.getValue()).thenReturn(new Password("EIWebCryptographerTest"));
        when(inboundDAO.getDeviceProtocolSecurityProperties(deviceIdentifier, comPort)).thenReturn(Arrays.asList(encryptionPassword));

        EIWebCryptographer cryptographer = new EIWebCryptographer(inboundDAO, comPort);

        // Business method
        cryptographer.buildMD5Seed(deviceIdentifier, "1234");

        // Asserts
        assertThat(cryptographer.wasUsed()).isTrue();
    }

    @Test
    public void testWasNotUsed () {
        EIWebCryptographer cryptographer = new EIWebCryptographer(mock(InboundDAO.class), mock(InboundComPort.class));

        // Business method
        // Do not use any method at all since that is what we are actually testing

        // Asserts
        assertThat(cryptographer.wasUsed()).isFalse();
    }

}