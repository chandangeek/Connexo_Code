package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.mdc.channels.inbound.EIWebConnectionType;
import com.energyict.mdc.protocol.inbound.crypto.MD5Seed;
import com.energyict.mdc.protocol.security.SecurityProperty;
import com.energyict.mdc.tasks.InboundConnectionTask;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.DeviceFactory;
import com.energyict.mdw.core.DeviceFactoryProvider;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.identifier.NotFoundException;
import com.energyict.protocolimpl.properties.TypedProperties;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

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
        InboundDiscoveryContext inboundDiscoveryContext = mock(InboundDiscoveryContext.class);
        doThrow(NotFoundException.class).when(inboundDiscoveryContext).getConnectionTypeProperties(deviceIdentifier);

        EIWebCryptographer cryptographer = new EIWebCryptographer(inboundDiscoveryContext);

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
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        InboundDiscoveryContext inboundDiscoveryContext = mock(InboundDiscoveryContext.class);
        when(inboundDiscoveryContext.getConnectionTypeProperties(deviceIdentifier)).thenReturn(Optional.empty());

        EIWebCryptographer cryptographer = new EIWebCryptographer(inboundDiscoveryContext);

        // Business method
        cryptographer.buildMD5Seed(deviceIdentifier, "1234");

        // Asserts: expected CommunicationException
    }

    @Test
    public void testBuildMD5SeedExistingDevice () {
        DeviceFactory deviceFactory = mock(DeviceFactory.class);
        DeviceFactoryProvider.instance.set(() -> deviceFactory);
        Device device = mock(Device.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceFactory.find(deviceIdentifier)).thenReturn(device);
        TypedProperties connectionTypeProperties = TypedProperties.empty();
        connectionTypeProperties.setProperty(EIWebConnectionType.MAC_ADDRESS_PROPERTY_NAME, MAC_ADDRESS_VALUE);
        SecurityProperty encryptionPassword = mock(SecurityProperty.class);
        when(encryptionPassword.getValue()).thenReturn(new SimplePassword("EIWebCryptographerTest"));
        InboundDiscoveryContext inboundDiscoveryContext = mock(InboundDiscoveryContext.class);
        when(inboundDiscoveryContext.getConnectionTypeProperties(deviceIdentifier)).thenReturn(connectionTypeProperties);
        when(inboundDiscoveryContext.getProtocolSecurityProperties(deviceIdentifier)).thenReturn(Collections.singletonList(encryptionPassword));

        EIWebCryptographer cryptographer = new EIWebCryptographer(inboundDiscoveryContext);

        // Business method
        MD5Seed md5Seed = cryptographer.buildMD5Seed(deviceIdentifier, "1234");

        // Asserts
        assertThat(md5Seed).isNotNull();
    }

    @Test
    public void testWasUsed () {
        DeviceFactory deviceFactory = mock(DeviceFactory.class);
        Device device = mock(Device.class);
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        when(deviceFactory.find(deviceIdentifier)).thenReturn(device);
        InboundDiscoveryContext inboundDiscoveryContext = mock(InboundDiscoveryContext.class);
        TypedProperties connectionTypeProperties = TypedProperties.empty();
        connectionTypeProperties.setProperty(EIWebConnectionType.MAC_ADDRESS_PROPERTY_NAME, MAC_ADDRESS_VALUE);
        when(inboundDiscoveryContext.getConnectionTypeProperties(deviceIdentifier)).thenReturn(connectionTypeProperties);
        SecurityProperty encryptionPassword = mock(SecurityProperty.class);
        when(encryptionPassword.getValue()).thenReturn(new SimplePassword("EIWebCryptographerTest"));
        when(inboundDiscoveryContext.getProtocolSecurityProperties(deviceIdentifier)).thenReturn(Collections.singletonList(encryptionPassword));

        EIWebCryptographer cryptographer = new EIWebCryptographer(inboundDiscoveryContext);

        // Business method
        cryptographer.buildMD5Seed(deviceIdentifier, "1234");

        // Asserts
        assertThat(cryptographer.wasUsed()).isTrue();
    }

    @Test
    public void testWasNotUsed () {
        EIWebCryptographer cryptographer = new EIWebCryptographer(mock(InboundDiscoveryContext.class));

        // Business method
        // Do not use any method at all since that is what we are actually testing

        // Asserts
        assertThat(cryptographer.wasUsed()).isFalse();
    }

}