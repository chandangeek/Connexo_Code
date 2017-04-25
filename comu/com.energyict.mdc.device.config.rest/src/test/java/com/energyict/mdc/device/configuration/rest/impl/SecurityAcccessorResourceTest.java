/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.KeyAccessorTypeUpdater;
import com.energyict.mdc.device.configuration.rest.ExecutionLevelInfo;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import static java.lang.Math.toIntExact;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SecurityAcccessorResourceTest extends DeviceConfigurationApplicationJerseyTest {


    private static final String DESCRIPTION = "NEW DESCRIPTION";
    private static final String NAME = "NEW NAME";

    @Test
    public void testGetAllKeyFunctionTypesOfDeviceType() throws Exception {
        DeviceType deviceType = mockDeviceType("device type 1", 66);
        KeyAccessorType keyFunctionType = mockKeyFunctionType(1, "Name", "Epic description");
        KeyAccessorType keyFunctionType2 = mockKeyFunctionType(2, "Name2", "Epic description2");
        when(deviceConfigurationService.findDeviceType(66)).thenReturn(Optional.of(deviceType));
        when(deviceType.getKeyAccessorTypes()).thenReturn(Arrays.asList(keyFunctionType, keyFunctionType2));

        Map<String, Object> map = target("/devicetypes/66/securityaccessors").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(2);
        assertThat((List) map.get("securityaccessors")).hasSize(2);
    }

    @Test(expected = WebApplicationException.class)
    public void getUnexistingDeviceType() throws Exception {
        when(deviceConfigurationService.findDeviceType(66)).thenReturn(Optional.empty());
        target("/devicetypes/66/securityaccessors").request().get(Map.class);
    }

    @Test
    public void getKeyFunctionType() throws Exception {
        DeviceType deviceType = mockDeviceType("device type 1", 66);
        KeyAccessorType keyFunctionType = mockKeyFunctionType(1, NAME, DESCRIPTION);
        when(deviceConfigurationService.findDeviceType(66)).thenReturn(Optional.of(deviceType));
        when(deviceType.getKeyAccessorTypes()).thenReturn(Collections.singletonList(keyFunctionType));

        String response = target("/devicetypes/66/securityaccessors/1").request().get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.id")).isEqualTo(1);
        assertThat(model.<String>get("$.name")).isEqualTo(NAME);
        assertThat(model.<String>get("$.description")).isEqualTo(DESCRIPTION);
        assertThat(model.<Number>get("$.keyType.id")).isEqualTo(1);
        assertThat(model.<String>get("$.keyType.name")).isEqualTo("Name of the keytype");
        assertThat(model.<Boolean>get("$.keyType.requiresDuration")).isEqualTo(true);
        assertThat(model.<Number>get("$.validityPeriod.count")).isEqualTo(2);
        assertThat(model.<Number>get("$.validityPeriod.asSeconds")).isEqualTo(5356800);
        assertThat(model.<String>get("$.validityPeriod.localizedTimeUnit")).isEqualTo("months");
        assertThat(model.<String>get("$.validityPeriod.timeUnit")).isEqualTo("months");
        assertThat(model.<String>get("$.parent.id")).isEqualTo("device type 1");
        assertThat(model.<Number>get("$.parent.version")).isEqualTo(toIntExact(OK_VERSION));
    }

    @Test
    public void addKeyFunctionTypeOnDeviceType() throws Exception {
        DeviceType deviceType = mockDeviceType("device type 1", 66);
        SecurityAccessorInfo info = new SecurityAccessorInfo();
        when(deviceConfigurationService.findAndLockDeviceType(66, 1)).thenReturn(Optional.of(deviceType));
        info.id = 1;
        info.description = DESCRIPTION;
        info.name = NAME;
        info.validityPeriod = new TimeDurationInfo(new TimeDuration(1, TimeDuration.TimeUnit.YEARS));
        info.parent = new VersionInfo<>("device type 1", 1L);

        KeyType keyType = mock(KeyType.class);
        when(keyType.getId()).thenReturn(1L);
        when(keyType.getName()).thenReturn("AES 128");
        when(keyType.getCryptographicType()).thenReturn(CryptographicType.SymmetricKey);

        when(pkiService.getKeyType("AES 128")).thenReturn(Optional.of(keyType));

        KeyAccessorType.Builder builder = mock(KeyAccessorType.Builder.class);
        when(deviceType.addKeyAccessorType(NAME, keyType)).thenReturn(builder);
        when(builder.keyEncryptionMethod(anyString())).thenReturn(builder);
        when(builder.description(anyString())).thenReturn(builder);
        KeyAccessorType addedKeyFunctionTypeDoesntMatter = mockKeyFunctionType(1, NAME, DESCRIPTION);
        when(builder.add()).thenReturn(addedKeyFunctionTypeDoesntMatter);

        info.keyType = new KeyTypeInfo();
        info.keyType.id = 1;
        info.keyType.name = "AES 128";
        info.keyType.requiresDuration = true;

        target("/devicetypes/66/securityaccessors").request().post(Entity.json(info));
        verify(deviceType).addKeyAccessorType(NAME, keyType);
        verify(builder).description(DESCRIPTION);
        verify(builder).duration(info.validityPeriod.asTimeDuration());
        verify(builder).add();
    }

    @Test
    public void editKeyFunctionTypeOfDeviceType() throws Exception {
        DeviceType deviceType = mockDeviceType("device type 1", 66);
        KeyAccessorType keyFunctionType = mockKeyFunctionType(1, "Name", "Epic description");
        when(deviceType.getKeyAccessorTypes()).thenReturn(Collections.singletonList(keyFunctionType));
        when(deviceConfigurationService.findAndLockDeviceType(66, 1)).thenReturn(Optional.of(deviceType));
        SecurityAccessorInfo info = new SecurityAccessorInfo();
        info.id = 1;
        info.description = "New Description";
        info.name = "New name";
        info.validityPeriod = new TimeDurationInfo(new TimeDuration(1, TimeDuration.TimeUnit.YEARS));
        info.parent = new VersionInfo<>("device type 1", 1L);
        ExecutionLevelInfo executionLevelInfo = new ExecutionLevelInfo();
        executionLevelInfo.id = "edit.device.security.properties.level1";
        info.editLevels = Collections.singletonList(executionLevelInfo);
        KeyAccessorTypeUpdater updater = mock(KeyAccessorTypeUpdater.class);
        when(deviceType.getKeyAccessorTypeUpdater(keyFunctionType)).thenReturn(Optional.of(updater));
        when(updater.complete()).thenReturn(keyFunctionType);

        target("/devicetypes/66/securityaccessors/1").request().put(Entity.json(info));
        verify(deviceType).getKeyAccessorTypeUpdater(keyFunctionType);
        verify(updater).description(info.description);
        verify(updater).name(info.name);
        verify(updater).duration(info.validityPeriod.asTimeDuration());
        verify(updater).addUserAction(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1);
    }

    @Test
    public void deleteKeyFunctionTypeOfDeviceType() throws Exception {
        DeviceType deviceType = mockDeviceType("device type 1", 66);
        KeyAccessorType keyFunctionType = mockKeyFunctionType(1, "Name", "Epic description");
        when(deviceType.getKeyAccessorTypes()).thenReturn(Collections.singletonList(keyFunctionType));
        when(deviceConfigurationService.findAndLockDeviceType(66, 1)).thenReturn(Optional.of(deviceType));
        SecurityAccessorInfo info = new SecurityAccessorInfo();
        info.parent = new VersionInfo<>("device type 1", 1L);

        target("/devicetypes/66/securityaccessors/1").request().method("DELETE", Entity.json(info));
        verify(deviceType).removeKeyAccessorType(keyFunctionType);
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
        when(keyType.getCryptographicType()).thenReturn(CryptographicType.SymmetricKey);
        TimeDuration validityPeriod = new TimeDuration(2, TimeDuration.TimeUnit.MONTHS);
        when(keyFunctionType.getDuration()).thenReturn(Optional.of(validityPeriod));
        return keyFunctionType;
    }
}
