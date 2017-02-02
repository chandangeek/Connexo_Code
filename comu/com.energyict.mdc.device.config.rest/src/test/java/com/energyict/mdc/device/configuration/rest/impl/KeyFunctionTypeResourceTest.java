/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.DeviceType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KeyFunctionTypeResourceTest extends DeviceConfigurationApplicationJerseyTest {


    @Test
    public void testGetAllKeyFunctionTypesOfDeviceType() throws Exception {
        DeviceType deviceType = mockDeviceType("device type 1", 66);
        KeyAccessorType keyFunctionType = mockKeyFunctionType(1, "Name", "Epic description");
        when(deviceConfigurationService.findDeviceType(66)).thenReturn(Optional.of(deviceType));
        when(deviceType.getKeyAccessorTypes()).thenReturn(Collections.singletonList(keyFunctionType));

        Map<String, Object> map = target("/devicetypes/66/keyfunctiontypes").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(1);
        assertThat((List) map.get("keyfunctiontypes")).hasSize(1);
    }

    private KeyAccessorType mockKeyFunctionType(long id, String name, String description) {
        KeyAccessorType keyFunctionType = mock(KeyAccessorType.class);
        when(keyFunctionType.getName()).thenReturn(name);
        when(keyFunctionType.getDescription()).thenReturn(description);
        when(keyFunctionType.getId()).thenReturn(id);
        KeyType keyType = mock(KeyType.class);
        when(keyType.getId()).thenReturn(1L);
        when(keyType.getName()).thenReturn("Name of the keytype");
        when(keyFunctionType.getKeyType()).thenReturn(keyType);
        TimeDuration validityPeriod = new TimeDuration(31556926);
        when(keyFunctionType.getDuration()).thenReturn(Optional.of(validityPeriod));
        return keyFunctionType;
    }
}
