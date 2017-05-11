/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.KeyAccessor;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import javax.crypto.SecretKey;
import java.sql.SQLException;
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
    private ComServerDAOImpl.ServiceProvider serviceProvider;


    @Before
    public void initializeMocksAndFactories() throws SQLException {
        TransactionService transactionService = TransactionModule.FakeTransactionService.INSTANCE;
        when(this.serviceProvider.transactionService()).thenReturn(transactionService);
//        when(this.deviceIdentifier.findDevice()).thenReturn(this.device); TODO: mock deviceService so it can resolve device identifiers
    }

    @Test
    public void testDeviceProtocolSecurityPropertySetConstructor() {
        TypedProperties securityProperties = TypedProperties.empty();
        securityProperties.setProperty("Password", "MyPassword");
        securityProperties.setProperty("SymmetricKey", "MySymmetricKey");

        // Business method
        DeviceProtocolSecurityPropertySetImpl deviceProtocolSecurityPropertySet = new DeviceProtocolSecurityPropertySetImpl(
                "Client",
                1,
                2,
                3,
                4,
                5,
                securityProperties
        );

        // Asserts
        assertThat(deviceProtocolSecurityPropertySet.getClient()).isEqualTo("Client");
        assertThat(deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel()).isEqualTo(1);
        assertThat(deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()).isEqualTo(2);
        assertThat(deviceProtocolSecurityPropertySet.getSecuritySuite()).isEqualTo(3);
        assertThat(deviceProtocolSecurityPropertySet.getRequestSecurityLevel()).isEqualTo(4);
        assertThat(deviceProtocolSecurityPropertySet.getResponseSecurityLevel()).isEqualTo(5);
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().size()).isEqualTo(2);
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getLocalValue("Password")).isEqualTo("MyPassword");
        assertThat(deviceProtocolSecurityPropertySet.getSecurityProperties().getLocalValue("SymmetricKey")).isEqualTo("MySymmetricKey");
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
        when(keyAccessor.getActualValue()).thenReturn(Optional.of(plaintextSymmetricKey));
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
        when(keyAccessor.getActualValue()).thenReturn(Optional.of(plaintextPassphrase));
        return keyAccessor;
    }
}