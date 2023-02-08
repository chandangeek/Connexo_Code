/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.energyict.mdc.common.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.upl.TypedProperties;
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
                "security",
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

    private ConfigurationSecurityProperty getConfigurationSecurityProperty(SecurityAccessor securityAccessor, String name) {
        ConfigurationSecurityProperty configurationSecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(configurationSecurityProperty.getName()).thenReturn(name);
        SecurityAccessorType securityAccessorType = securityAccessor.getSecurityAccessorType();
        when(configurationSecurityProperty.getSecurityAccessorType()).thenReturn(securityAccessorType);
        return configurationSecurityProperty;
    }

    private SecurityAccessor getSymmetricKeyKeyAccessor(String keyAccessorTypeName, byte[] symmetricKey) {
        PlaintextSymmetricKey plaintextSymmetricKey = mock(PlaintextSymmetricKey.class);
        SecretKey secretKey = mock(SecretKey.class);
        when(secretKey.getEncoded()).thenReturn(symmetricKey);
        when(plaintextSymmetricKey.getKey()).thenReturn(Optional.of(secretKey));

        SecurityAccessor securityAccessor = mock(SecurityAccessor.class);
        SecurityAccessorType securityAccessorType = mock(SecurityAccessorType.class);
        when(securityAccessorType.getName()).thenReturn(keyAccessorTypeName);
        when(securityAccessor.getSecurityAccessorType()).thenReturn(securityAccessorType);
        when(securityAccessor.getDevice()).thenReturn(device);
        when(securityAccessor.getActualValue()).thenReturn(Optional.of(plaintextSymmetricKey));
        return securityAccessor;
    }

    private SecurityAccessor getPassPhraseKeyAccessor(String keyAccessorTypeName, String passPhrase) {
        PlaintextPassphrase plaintextPassphrase = mock(PlaintextPassphrase.class);
        when(plaintextPassphrase.getEncryptedPassphrase()).thenReturn(Optional.of(passPhrase));

        SecurityAccessor securityAccessor = mock(SecurityAccessor.class);
        SecurityAccessorType securityAccessorType = mock(SecurityAccessorType.class);
        when(securityAccessorType.getName()).thenReturn(keyAccessorTypeName);
        when(securityAccessor.getSecurityAccessorType()).thenReturn(securityAccessorType);
        when(securityAccessor.getDevice()).thenReturn(device);
        when(securityAccessor.getActualValue()).thenReturn(Optional.of(plaintextPassphrase));
        return securityAccessor;
    }
}
