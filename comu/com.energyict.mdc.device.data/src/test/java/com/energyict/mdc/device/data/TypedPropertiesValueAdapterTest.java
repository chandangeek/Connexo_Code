/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineKeyAccessor;
import com.energyict.mdc.upl.TypedProperties;

import java.util.Collections;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link TypedPropertiesValueAdapter} component
 *
 * @author stijn
 * @since 19.05.17 - 14:54
 */
@RunWith(MockitoJUnitRunner.class)
public class TypedPropertiesValueAdapterTest {

    private static final String REGULAR = "regular";
    private static final String KEY_ACCESSOR = "keyAccessor";

    @Test
    public void testAdaptToUPLValuesUsingDevice() throws Exception {
        SecurityAccessorType securityAccessorType = mock(SecurityAccessorType.class);
        when(securityAccessorType.getId()).thenReturn(1L);
        Device device = mock(Device.class);
        SecurityAccessor securityAccessor = mock(SecurityAccessor.class);
        PlaintextPassphrase plaintextPassphrase = mock(PlaintextPassphrase.class);
        when(plaintextPassphrase.getEncryptedPassphrase()).thenReturn(Optional.of("My key"));
        when(securityAccessor.getActualValue()).thenReturn(Optional.of(plaintextPassphrase));
        when(securityAccessor.getSecurityAccessorType()).thenReturn(securityAccessorType);
        when(device.getSecurityAccessors()).thenReturn(Collections.singletonList(securityAccessor));

        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(REGULAR, "Do not alter");
        properties.setProperty(KEY_ACCESSOR, securityAccessorType);

        com.energyict.mdc.upl.properties.TypedProperties adaptedProperties = TypedPropertiesValueAdapter.adaptToUPLValues(device, properties);

        assertThat(adaptedProperties.propertyNames().size()).isEqualTo(2);
        assertThat(adaptedProperties.getProperty(REGULAR)).isEqualTo("Do not alter");
        assertThat(adaptedProperties.getProperty(KEY_ACCESSOR)).isEqualTo("My key");
    }

    @Test
    public void testAdaptToUPLValueUsingDevice() throws Exception {
        SecurityAccessorType securityAccessorType = mock(SecurityAccessorType.class);
        when(securityAccessorType.getId()).thenReturn(1L);
        Device device = mock(Device.class);
        SecurityAccessor securityAccessor = mock(SecurityAccessor.class);
        PlaintextPassphrase plaintextPassphrase = mock(PlaintextPassphrase.class);
        when(plaintextPassphrase.getEncryptedPassphrase()).thenReturn(Optional.of("My key"));
        when(securityAccessor.getActualValue()).thenReturn(Optional.of(plaintextPassphrase));
        when(securityAccessor.getSecurityAccessorType()).thenReturn(securityAccessorType);
        when(device.getSecurityAccessors()).thenReturn(Collections.singletonList(securityAccessor));

        Object adaptedValue = TypedPropertiesValueAdapter.adaptToUPLValue(device, securityAccessorType);

        assertThat(adaptedValue).isEqualTo("My key");
    }

    @Test
    public void testAdaptToUPLValuesUsingOfflineDevice() throws Exception {
        SecurityAccessorType securityAccessorType = mock(SecurityAccessorType.class);
        when(securityAccessorType.getId()).thenReturn(1L);
        OfflineDevice device = mock(OfflineDevice.class);
        OfflineKeyAccessor keyAccessor = mock(OfflineKeyAccessor.class);
        PlaintextPassphrase plaintextPassphrase = mock(PlaintextPassphrase.class);
        when(plaintextPassphrase.getEncryptedPassphrase()).thenReturn(Optional.of("My key"));
        when(keyAccessor.getActualValue()).thenReturn(Optional.of(plaintextPassphrase));
        when(keyAccessor.getSecurityAccessorType()).thenReturn(securityAccessorType);
        when(device.getAllOfflineKeyAccessors()).thenReturn(Collections.singletonList(keyAccessor));

        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(REGULAR, "Do not alter");
        properties.setProperty(KEY_ACCESSOR, securityAccessorType);

        com.energyict.mdc.upl.properties.TypedProperties adaptedProperties = TypedPropertiesValueAdapter.adaptToUPLValues(device, properties);

        assertThat(adaptedProperties.propertyNames().size()).isEqualTo(2);
        assertThat(adaptedProperties.getProperty(REGULAR)).isEqualTo("Do not alter");
        assertThat(adaptedProperties.getProperty(KEY_ACCESSOR)).isEqualTo("My key");

    }

    @Test
    public void testAdaptToUPLValueUsingOfflineDevice() throws Exception {
        SecurityAccessorType securityAccessorType = mock(SecurityAccessorType.class);
        when(securityAccessorType.getId()).thenReturn(1L);
        OfflineDevice device = mock(OfflineDevice.class);
        OfflineKeyAccessor keyAccessor = mock(OfflineKeyAccessor.class);
        PlaintextPassphrase plaintextPassphrase = mock(PlaintextPassphrase.class);
        when(plaintextPassphrase.getEncryptedPassphrase()).thenReturn(Optional.of("My key"));
        when(keyAccessor.getActualValue()).thenReturn(Optional.of(plaintextPassphrase));
        when(keyAccessor.getSecurityAccessorType()).thenReturn(securityAccessorType);
        when(device.getAllOfflineKeyAccessors()).thenReturn(Collections.singletonList(keyAccessor));

        Object adaptedValue = TypedPropertiesValueAdapter.adaptToUPLValue(device, securityAccessorType);

        assertThat(adaptedValue).isEqualTo("My key");
    }
}
