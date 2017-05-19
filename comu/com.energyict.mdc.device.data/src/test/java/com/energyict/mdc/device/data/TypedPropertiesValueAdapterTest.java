/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PlaintextPassphrase;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineKeyAccessor;

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
        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getId()).thenReturn(1L);
        Device device = mock(Device.class);
        KeyAccessor keyAccessor = mock(KeyAccessor.class);
        PlaintextPassphrase plaintextPassphrase = mock(PlaintextPassphrase.class);
        when(plaintextPassphrase.getPassphrase()).thenReturn(Optional.of("My key"));
        when(keyAccessor.getActualValue()).thenReturn(Optional.of(plaintextPassphrase));
        when(keyAccessor.getKeyAccessorType()).thenReturn(keyAccessorType);
        when(device.getKeyAccessors()).thenReturn(Collections.singletonList(keyAccessor));

        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(REGULAR, "Do not alter");
        properties.setProperty(KEY_ACCESSOR, keyAccessorType);

        com.energyict.mdc.upl.properties.TypedProperties adaptedProperties = TypedPropertiesValueAdapter.adaptToUPLValues(device, properties);

        assertThat(adaptedProperties.propertyNames().size()).isEqualTo(2);
        assertThat(adaptedProperties.getProperty(REGULAR)).isEqualTo("Do not alter");
        assertThat(adaptedProperties.getProperty(KEY_ACCESSOR)).isEqualTo("My key");
    }

    @Test
    public void testAdaptToUPLValueUsingDevice() throws Exception {
        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getId()).thenReturn(1L);
        Device device = mock(Device.class);
        KeyAccessor keyAccessor = mock(KeyAccessor.class);
        PlaintextPassphrase plaintextPassphrase = mock(PlaintextPassphrase.class);
        when(plaintextPassphrase.getPassphrase()).thenReturn(Optional.of("My key"));
        when(keyAccessor.getActualValue()).thenReturn(Optional.of(plaintextPassphrase));
        when(keyAccessor.getKeyAccessorType()).thenReturn(keyAccessorType);
        when(device.getKeyAccessors()).thenReturn(Collections.singletonList(keyAccessor));

        Object adaptedValue = TypedPropertiesValueAdapter.adaptToUPLValue(device, keyAccessorType);

        assertThat(adaptedValue).isEqualTo("My key");
    }

    @Test
    public void testAdaptToUPLValuesUsingOfflineDevice() throws Exception {
        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getId()).thenReturn(1L);
        OfflineDevice device = mock(OfflineDevice.class);
        OfflineKeyAccessor keyAccessor = mock(OfflineKeyAccessor.class);
        PlaintextPassphrase plaintextPassphrase = mock(PlaintextPassphrase.class);
        when(plaintextPassphrase.getPassphrase()).thenReturn(Optional.of("My key"));
        when(keyAccessor.getActualValue()).thenReturn(Optional.of(plaintextPassphrase));
        when(keyAccessor.getKeyAccessorType()).thenReturn(keyAccessorType);
        when(device.getAllOfflineKeyAccessors()).thenReturn(Collections.singletonList(keyAccessor));

        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(REGULAR, "Do not alter");
        properties.setProperty(KEY_ACCESSOR, keyAccessorType);

        com.energyict.mdc.upl.properties.TypedProperties adaptedProperties = TypedPropertiesValueAdapter.adaptToUPLValues(device, properties);

        assertThat(adaptedProperties.propertyNames().size()).isEqualTo(2);
        assertThat(adaptedProperties.getProperty(REGULAR)).isEqualTo("Do not alter");
        assertThat(adaptedProperties.getProperty(KEY_ACCESSOR)).isEqualTo("My key");

    }

    @Test
    public void testAdaptToUPLValueUsingOfflineDevice() throws Exception {
        KeyAccessorType keyAccessorType = mock(KeyAccessorType.class);
        when(keyAccessorType.getId()).thenReturn(1L);
        OfflineDevice device = mock(OfflineDevice.class);
        OfflineKeyAccessor keyAccessor = mock(OfflineKeyAccessor.class);
        PlaintextPassphrase plaintextPassphrase = mock(PlaintextPassphrase.class);
        when(plaintextPassphrase.getPassphrase()).thenReturn(Optional.of("My key"));
        when(keyAccessor.getActualValue()).thenReturn(Optional.of(plaintextPassphrase));
        when(keyAccessor.getKeyAccessorType()).thenReturn(keyAccessorType);
        when(device.getAllOfflineKeyAccessors()).thenReturn(Collections.singletonList(keyAccessor));

        Object adaptedValue = TypedPropertiesValueAdapter.adaptToUPLValue(device, keyAccessorType);

        assertThat(adaptedValue).isEqualTo("My key");
    }
}