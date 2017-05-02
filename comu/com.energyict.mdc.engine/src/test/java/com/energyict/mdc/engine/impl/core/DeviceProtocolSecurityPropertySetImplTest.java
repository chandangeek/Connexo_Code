/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.KeyAccessor;
import com.energyict.mdc.engine.FakeTransactionService;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import javax.crypto.SecretKey;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author stijn
 * @since 28.04.17 - 14:47
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceProtocolSecurityPropertySetImplTest {

    @Mock
    private Device device;
    @Mock
    private DeviceIdentifier deviceIdentifier;
    @Mock
    private IdentificationService identificationService;
    @Mock
    private ComServerDAOImpl.ServiceProvider serviceProvider;


    @Before
    public void initializeMocksAndFactories() throws SQLException {
        TransactionService transactionService = new FakeTransactionService();
        when(this.serviceProvider.transactionService()).thenReturn(transactionService);
        when(this.deviceIdentifier.findDevice()).thenReturn(this.device);
    }

    @Test
    public void testDeviceProtocolSecurityPropertySetConstructor() {
        String password = "Password";
        KeyAccessor passPhraseKeyAccessor = getPassPhraseKeyAccessor("PassPhraseKeyAccessorType", "MyPassword");
        KeyAccessor otherPassPhraseKeyAccessor = getPassPhraseKeyAccessor("OtherPassPhraseKeyAccessorType", "/");
        ConfigurationSecurityProperty passPhraseSecurityProperty = getConfigurationSecurityProperty(passPhraseKeyAccessor, password);
        byte[] symmetricKey = {0x01, 0x02};
        KeyAccessor symmetricKeyKeyAccessor = getSymmetricKeyKeyAccessor("SymmetricKeyKeyAccessorType", symmetricKey);
        KeyAccessor otherSymmetricKeyKeyAccessor = getSymmetricKeyKeyAccessor("SymmetricKeyKeyAccessorType", symmetricKey);
        ConfigurationSecurityProperty symmetricKeySecurityProperty = getConfigurationSecurityProperty(symmetricKeyKeyAccessor, "SymmetricKey");

        List<ConfigurationSecurityProperty> expectedSecurityProperties = Arrays.asList(passPhraseSecurityProperty, symmetricKeySecurityProperty);
        List<KeyAccessor> expectedKeyAccessors = Arrays.asList(otherPassPhraseKeyAccessor, passPhraseKeyAccessor, otherPassPhraseKeyAccessor, symmetricKeyKeyAccessor); // Added 2 additional key accessors

        // Business method
        DeviceProtocolSecurityPropertySetImpl deviceProtocolSecurityPropertySet = new DeviceProtocolSecurityPropertySetImpl(
                "Client",
                1,
                2,
                3,
                4,
                5,
                expectedSecurityProperties,
                expectedKeyAccessors,
                identificationService
        );

        // Asserts
        assertThat(deviceProtocolSecurityPropertySet.getClient()).isEqualTo("Client");
        assertThat(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(1);
        assertThat(deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(2);
        assertThat(deviceProtocolSecurityPropertySet.getSecuritySuite()).isEqualTo(3);
        assertThat(deviceProtocolSecurityPropertySet.getRequestSecurityLevel()).isEqualTo(4);
        assertThat(deviceProtocolSecurityPropertySet.getResponseSecurityLevel()).isEqualTo(5);
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().size()).isEqualTo(2);
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getLocalValue(password)).isEqualTo("MyPassword");
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getLocalValue("SymmetricKey")).isEqualTo(symmetricKey);
    }

    private ConfigurationSecurityProperty getConfigurationSecurityProperty(KeyAccessor keyAccessor, String name) {
        ConfigurationSecurityProperty configurationSecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(configurationSecurityProperty.getName()).thenReturn(name);
        KeyAccessorType keyAccessorType = keyAccessor.getKeyAccessorType();
        when(configurationSecurityProperty.getKeyAccessorType()).thenReturn(keyAccessorType);
        return configurationSecurityProperty;
    }

    private KeyAccessor getSymmetricKeyKeyAccessor(String keyAccessorTypeName, byte[] symmetricKey) {
        PlaintextSymmetricKey plaintextSymmetricKey = mock(PlaintextSymmetricKey.class);
        SecretKey secretKey = mock(SecretKey.class);
        when(secretKey.getEncoded()).thenReturn(symmetricKey);
        when(plaintextSymmetricKey.getKey()).thenReturn(Optional.of(secretKey));

        KeyAccessor keyAccessor = mock(KeyAccessor.class);
        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getName()).thenReturn(keyAccessorTypeName);
        when(keyAccessor.getKeyAccessorType()).thenReturn(keyAccessorType);
        when(keyAccessor.getDevice()).thenReturn(device);
        when(keyAccessor.getActualValue()).thenReturn(plaintextSymmetricKey);
        return keyAccessor;
    }

    private KeyAccessor getPassPhraseKeyAccessor(String keyAccessorTypeName, String passPhrase) {
        PlaintextPassphrase plaintextPassphrase = mock(PlaintextPassphrase.class);
        when(plaintextPassphrase.getPassphrase()).thenReturn(Optional.of(passPhrase));

        KeyAccessor keyAccessor = mock(KeyAccessor.class);
        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getName()).thenReturn(keyAccessorTypeName);
        when(keyAccessor.getKeyAccessorType()).thenReturn(keyAccessorType);
        when(keyAccessor.getDevice()).thenReturn(device);
        when(keyAccessor.getActualValue()).thenReturn(plaintextPassphrase);
        return keyAccessor;
    }
}