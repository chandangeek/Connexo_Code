package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.mdc.channels.inbound.EIWebConnectionType;
import com.energyict.mdc.protocol.inbound.general.InboundConnection;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import com.energyict.protocol.exception.CommunicationException;
import com.energyict.mdc.upl.security.SecurityPropertySpecTranslationKeys;

import java.util.Optional;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
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
    @Test(expected = RuntimeException.class)
    public void testBuildMD5SeedForNonExistingDevice() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        InboundDiscoveryContext inboundDiscoveryContext = mock(InboundDiscoveryContext.class);
        doThrow(RuntimeException.class).when(inboundDiscoveryContext).getConnectionTypeProperties(deviceIdentifier);

        EIWebCryptographer cryptographer = new EIWebCryptographer(inboundDiscoveryContext);

        // Business method
        cryptographer.buildMD5Seed(deviceIdentifier, "1234");

        // Asserts: expected RuntimeException (actually a CanNotFindForIdentifier)
    }

    /**
     * Test that a {@link CommunicationException} is thrown
     * when the device is not ready for inbound communication
     * because no {@link InboundConnection}s are defined against it.
     */
    @Test(expected = CommunicationException.class)
    public void testBuildMD5SeedWithoutConnectionTypeProperties() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        InboundDiscoveryContext inboundDiscoveryContext = mock(InboundDiscoveryContext.class);
        when(inboundDiscoveryContext.getConnectionTypeProperties(deviceIdentifier)).thenReturn(Optional.empty());

        EIWebCryptographer cryptographer = new EIWebCryptographer(inboundDiscoveryContext);

        // Business method
        cryptographer.buildMD5Seed(deviceIdentifier, "1234");

        // Asserts: expected CommunicationException
    }

    @Test
    public void testBuildMD5SeedExistingDevice() {
        TypedProperties connectionTypeProperties = TypedProperties.empty();
        connectionTypeProperties.setProperty(EIWebConnectionType.MAC_ADDRESS_PROPERTY_NAME, MAC_ADDRESS_VALUE);
        InboundDiscoveryContext inboundDiscoveryContext = mock(InboundDiscoveryContext.class);
        Optional<DeviceProtocolSecurityPropertySet> deviceProtocolSecurityPropertySet = createDeviceProtocolSecurityPropertySet("EIWebCryptographerTest");
        when(inboundDiscoveryContext.getConnectionTypeProperties(any(DeviceIdentifier.class))).thenReturn(Optional.of(connectionTypeProperties));
        when(inboundDiscoveryContext.getDeviceProtocolSecurityPropertySet(any(DeviceIdentifier.class))).thenReturn(deviceProtocolSecurityPropertySet);

        EIWebCryptographer cryptographer = new EIWebCryptographer(inboundDiscoveryContext);

        // Business method
        StringBasedMD5Seed md5Seed = cryptographer.buildMD5Seed(mock(DeviceIdentifier.class), "1234");

        // Asserts
        assertThat(md5Seed).isNotNull();
    }

    @Test
    public void testWasUsed() {
        InboundDiscoveryContext inboundDiscoveryContext = mock(InboundDiscoveryContext.class);
        TypedProperties connectionTypeProperties = TypedProperties.empty();
        connectionTypeProperties.setProperty(EIWebConnectionType.MAC_ADDRESS_PROPERTY_NAME, MAC_ADDRESS_VALUE);
        when(inboundDiscoveryContext.getConnectionTypeProperties(any(DeviceIdentifier.class))).thenReturn(Optional.of(connectionTypeProperties));
        Optional<DeviceProtocolSecurityPropertySet> deviceProtocolSecurityPropertySet = createDeviceProtocolSecurityPropertySet("EIWebCryptographerTest");
        when(inboundDiscoveryContext.getDeviceProtocolSecurityPropertySet(any(DeviceIdentifier.class))).thenReturn(deviceProtocolSecurityPropertySet);

        EIWebCryptographer cryptographer = new EIWebCryptographer(inboundDiscoveryContext);

        // Business method
        cryptographer.buildMD5Seed(mock(DeviceIdentifier.class), "1234");

        // Asserts
        assertThat(cryptographer.wasUsed()).isTrue();
    }

    @Test
    public void testWasNotUsed() {
        EIWebCryptographer cryptographer = new EIWebCryptographer(mock(InboundDiscoveryContext.class));

        // Business method
        // Do not use any method at all since that is what we are actually testing

        // Asserts
        assertThat(cryptographer.wasUsed()).isFalse();
    }

    private Optional<DeviceProtocolSecurityPropertySet> createDeviceProtocolSecurityPropertySet(String password) {
        DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = mock(DeviceProtocolSecurityPropertySet.class);
        com.energyict.mdc.upl.properties.TypedProperties securityProperties = com.energyict.mdc.upl.TypedProperties.empty();
        securityProperties.setProperty(SecurityPropertySpecTranslationKeys.PASSWORD.toString(), password);
        when(deviceProtocolSecurityPropertySet.getSecurityProperties()).thenReturn(securityProperties);
        return Optional.of(deviceProtocolSecurityPropertySet);
    }
}
