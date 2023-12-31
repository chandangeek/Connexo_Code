/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.energyict.mdc.common.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.common.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.common.protocol.security.RequestSecurityLevel;
import com.energyict.mdc.common.protocol.security.ResponseSecurityLevel;
import com.energyict.mdc.common.protocol.security.SecuritySuite;
import com.energyict.mdc.device.configuration.rest.KeyFunctionTypePrivilegeTranslationKeys;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 9/30/14.
 */
public class SecurityPropertySetResourceTest extends DeviceDataRestApplicationJerseyTest {
    @Mock
    private ValueFactory<SecurityAccessorType> securityAccessorTypeValueFactory;
    @Mock
    private SecurityAccessorType securityAccessorType;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        when(securityAccessorTypeValueFactory.getValueType()).thenReturn(SecurityAccessorType.class);
        when(securityAccessorType.getId()).thenReturn(6378L);
        when(securityAccessorType.getName()).thenReturn("KeyAccessorType for password");
    }

    @Override
    protected void setupTranslations() {
        super.setupTranslations();
        Stream.of(KeyFunctionTypePrivilegeTranslationKeys.values()).forEach(this::mockTranslation);
        Stream.of(DefaultTranslationKey.values()).forEach(this::mockTranslation);
    }

    private void mockTranslation(KeyFunctionTypePrivilegeTranslationKeys translationKey) {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(translationKey.getDefaultFormat());
        doReturn(messageFormat).when(thesaurus).getFormat(translationKey);
    }

    private void mockTranslation(DefaultTranslationKey translationKey) {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(translationKey.getDefaultFormat());
        doReturn(messageFormat).when(thesaurus).getFormat(translationKey);
    }

    @Test
    public void testGetSecurityPropertySets() throws Exception {
        Device device = mock(Device.class);
        when(deviceService.findDeviceByName("AX1")).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mockAuthenticationDeviceAccessLevel(1, "Low level Authentication");
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mockEncryptionDeviceAccessLevel(2, "Message Authentication and Encryption");
        SecuritySuite securitySuite = mockSecuritySuite(3, "Suite1");
        RequestSecurityLevel requestSecurityLevel = mockRequestSecurityLevel(4, "RequestSec1");
        ResponseSecurityLevel responseSecurityLevel = mockResponseSecurityLevel(5, "ResponseSec1");
        SecurityPropertySet sps1 = mockSecurityPropertySet(1001L, "Set 1", "Client 1", authenticationDeviceAccessLevel, encryptionDeviceAccessLevel, securitySuite, requestSecurityLevel, responseSecurityLevel);
        ConfigurationSecurityProperty securityProperty1 = mockSecurityPropertyWithSpec(sps1, "password", securityAccessorType, securityAccessorTypeValueFactory);
        when(sps1.getConfigurationSecurityProperties()).thenReturn(Collections.singletonList(securityProperty1));
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Collections.singletonList(sps1));
        String response = target("/devices/AX1/securityproperties").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].name")).isEqualTo("Set 1");
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].client.propertyValueInfo.value")).isEqualTo("Client 1");
        assertThat(jsonModel.<Integer>get("$.securityPropertySets[0].authenticationLevel.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].authenticationLevel.name")).isEqualTo("Low level Authentication");
        assertThat(jsonModel.<Integer>get("$.securityPropertySets[0].encryptionLevel.id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].encryptionLevel.name")).isEqualTo("Message Authentication and Encryption");
        assertThat(jsonModel.<Integer>get("$.securityPropertySets[0].securitySuite.id")).isEqualTo(3);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].securitySuite.name")).isEqualTo("Suite1");
        assertThat(jsonModel.<Integer>get("$.securityPropertySets[0].requestSecurityLevel.id")).isEqualTo(4);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].requestSecurityLevel.name")).isEqualTo("RequestSec1");
        assertThat(jsonModel.<Integer>get("$.securityPropertySets[0].responseSecurityLevel.id")).isEqualTo(5);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].responseSecurityLevel.name")).isEqualTo("ResponseSec1");
        assertThat(jsonModel.<List>get("$.securityPropertySets[0].properties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].key")).isEqualTo("password");
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyValueInfo.inheritedValue")).isNull();
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyValueInfo.defaultValue")).isNull();
        assertThat(jsonModel.<Number>get("$.securityPropertySets[0].properties[0].propertyValueInfo.value.id")).isEqualTo(6378);
        assertThat(jsonModel.<String>get("$.securityPropertySets[0].properties[0].propertyValueInfo.value.name")).isEqualTo("KeyAccessorType for password");
        assertThat(jsonModel.<Boolean>get("$.securityPropertySets[0].properties[0].required")).isEqualTo(true);
    }

    @Test
    public void testGetSecurityPropertySetById() throws Exception {
        long deviceConfigId = 6161L;
        long sps1Id = 1234L;
        String deviceName = "AX1";

        Device device = mock(Device.class);
        when(deviceService.findDeviceByName(deviceName)).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getId()).thenReturn(deviceConfigId);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mockAuthenticationDeviceAccessLevel(1, "Low level Authentication");
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mockEncryptionDeviceAccessLevel(2, "Message Authentication and Encryption");
        SecuritySuite securitySuite = mockSecuritySuite(3, "Suite1");
        RequestSecurityLevel requestSecurityLevel = mockRequestSecurityLevel(4, "RequestSec1");
        ResponseSecurityLevel responseSecurityLevel = mockResponseSecurityLevel(5, "ResponseSec1");
        SecurityPropertySet sps1 = mockSecurityPropertySet(sps1Id, "Set 1", "Client 1", authenticationDeviceAccessLevel, encryptionDeviceAccessLevel, securitySuite, requestSecurityLevel, responseSecurityLevel);
        when(deviceConfigurationService.findSecurityPropertySet(sps1Id)).thenReturn(Optional.of(sps1));
        ConfigurationSecurityProperty securityProperty1 = mockSecurityPropertyWithSpec(sps1, "password", securityAccessorType, securityAccessorTypeValueFactory);
        when(sps1.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(sps1.getConfigurationSecurityProperties()).thenReturn(Collections.singletonList(securityProperty1));
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Collections.singletonList(sps1));
        String response = target("/devices/" + deviceName + "/securityproperties/" + sps1Id).request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);

        assertThat(jsonModel.<String>get("$.name")).isEqualTo("Set 1");
        assertThat(jsonModel.<String>get("$.client.propertyValueInfo.value")).isEqualTo("Client 1");
        assertThat(jsonModel.<Integer>get("$.authenticationLevel.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.authenticationLevel.name")).isEqualTo("Low level Authentication");
        assertThat(jsonModel.<Integer>get("$.encryptionLevel.id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.encryptionLevel.name")).isEqualTo("Message Authentication and Encryption");
        assertThat(jsonModel.<Integer>get("$.securitySuite.id")).isEqualTo(3);
        assertThat(jsonModel.<String>get("$.securitySuite.name")).isEqualTo("Suite1");
        assertThat(jsonModel.<Integer>get("$.requestSecurityLevel.id")).isEqualTo(4);
        assertThat(jsonModel.<String>get("$.requestSecurityLevel.name")).isEqualTo("RequestSec1");
        assertThat(jsonModel.<Integer>get("$.responseSecurityLevel.id")).isEqualTo(5);
        assertThat(jsonModel.<String>get("$.responseSecurityLevel.name")).isEqualTo("ResponseSec1");
        assertThat(jsonModel.<List>get("$.properties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.properties[0].key")).isEqualTo("password");
        assertThat(jsonModel.<String>get("$.properties[0].propertyValueInfo.inheritedValue")).isNull();
        assertThat(jsonModel.<String>get("$.properties[0].propertyValueInfo.defaultValue")).isNull();
        assertThat(jsonModel.<Number>get("$.properties[0].propertyValueInfo.value.id")).isEqualTo(6378);
        assertThat(jsonModel.<String>get("$.properties[0].propertyValueInfo.value.name")).isEqualTo("KeyAccessorType for password");
        assertThat(jsonModel.<Boolean>get("$.properties[0].required")).isEqualTo(true);
    }

    @Test
    public void testGetSecurityPropertySetByIdDeviceConfigurationMismatch() throws Exception {
        long deviceConfigId = 6161L;
        long sps1Id = 1234L;
        String deviceName = "AX1";

        Device device = mock(Device.class);
        when(deviceService.findDeviceByName(deviceName)).thenReturn(Optional.of(device));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getId()).thenReturn(deviceConfigId);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mockAuthenticationDeviceAccessLevel(1, "DlmsSecuritySupportPerClient.authenticationlevel.1");
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mockEncryptionDeviceAccessLevel(2, "Mtu155SecuritySupport.encryptionlevel.2");
        SecuritySuite securitySuite = mockSecuritySuite(3, "Suite1");
        RequestSecurityLevel requestSecurityLevel = mockRequestSecurityLevel(4, "RequestSec1");
        ResponseSecurityLevel responseSecurityLevel = mockResponseSecurityLevel(5, "ResponseSec1");
        SecurityPropertySet sps1 = mockSecurityPropertySet(sps1Id, "Set 1", "Client 1", authenticationDeviceAccessLevel, encryptionDeviceAccessLevel, securitySuite, requestSecurityLevel, responseSecurityLevel);
        when(deviceConfigurationService.findSecurityPropertySet(sps1Id)).thenReturn(Optional.of(sps1));
        DeviceConfiguration otherDeviceConfiguration = mock(DeviceConfiguration.class);
        when(otherDeviceConfiguration.getId()).thenReturn(deviceConfigId + 1);
        when(sps1.getDeviceConfiguration()).thenReturn(otherDeviceConfiguration);

        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Collections.singletonList(sps1));
        Response response = target("/devices/" + deviceName + "/securityproperties/" + sps1Id).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetUnknownSecurityPropertySetById() throws Exception {
        long sps1Id = 1234L;
        String deviceName = "AX1";

        Device device = mock(Device.class);
        when(deviceService.findDeviceByName(deviceName)).thenReturn(Optional.of(device));
        when(deviceConfigurationService.findSecurityPropertySet(sps1Id)).thenReturn(Optional.empty());

        Response response = target("/devices/" + deviceName + "/securityproperties/" + sps1Id).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    private <T> ConfigurationSecurityProperty mockSecurityPropertyWithSpec(SecurityPropertySet securityPropertySet, String name, SecurityAccessorType securityAccessorType, ValueFactory<T> valueFactory) {
        ConfigurationSecurityProperty securityProperty = mock(ConfigurationSecurityProperty.class);
        when(securityProperty.getName()).thenReturn(name);
        when(securityProperty.getSecurityAccessorType()).thenReturn(securityAccessorType);
        PropertySpec propertySpec1 = mock(PropertySpec.class);
        when(propertySpec1.getName()).thenReturn(name);
        when(propertySpec1.isRequired()).thenReturn(true);
        when(propertySpec1.getValueFactory()).thenReturn(valueFactory);
        Set<PropertySpec> set = securityPropertySet.getPropertySpecs();
        set.add(propertySpec1);
        when(securityPropertySet.getPropertySpecs()).thenReturn(set);
        return securityProperty;
    }

    private SecurityPropertySet mockSecurityPropertySet(long id, String name,
                                                        String client,
                                                        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel, EncryptionDeviceAccessLevel encryptionDeviceAccessLevel,
                                                        SecuritySuite securitySuite, RequestSecurityLevel requestSecurityLevel, ResponseSecurityLevel responseSecurityLevel) {
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(id);
        when(securityPropertySet.getName()).thenReturn(name);
        when(securityPropertySet.getClient()).thenReturn(client);
        PropertySpec clientPropertySpec = mockPropertySpec("Client", new StringFactory());
        Optional<PropertySpec> clientPropertySpecOptional = Optional.of(clientPropertySpec);
        when(securityPropertySet.getClientSecurityPropertySpec()).thenReturn(clientPropertySpecOptional);
        when(securityPropertySet.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel);
        when(securityPropertySet.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel);
        when(securityPropertySet.getSecuritySuite()).thenReturn(securitySuite);
        when(securityPropertySet.getRequestSecurityLevel()).thenReturn(requestSecurityLevel);
        when(securityPropertySet.getResponseSecurityLevel()).thenReturn(responseSecurityLevel);

        return securityPropertySet;
    }

    private EncryptionDeviceAccessLevel mockEncryptionDeviceAccessLevel(int encryptionLevelId, String encryptionName) {
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionDeviceAccessLevel.getId()).thenReturn(encryptionLevelId);
        when(encryptionDeviceAccessLevel.getTranslation()).thenReturn(encryptionName);
        return encryptionDeviceAccessLevel;
    }

    private AuthenticationDeviceAccessLevel mockAuthenticationDeviceAccessLevel(int authLevelId, String authName) {
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        when(authenticationDeviceAccessLevel.getId()).thenReturn(authLevelId);
        when(authenticationDeviceAccessLevel.getTranslation()).thenReturn(authName);
        return authenticationDeviceAccessLevel;
    }

    private SecuritySuite mockSecuritySuite(int id, String name) {
        SecuritySuite securitySuite = mock(SecuritySuite.class);
        when(securitySuite.getId()).thenReturn(id);
        when(securitySuite.getTranslation()).thenReturn(name);
        return securitySuite;
    }

    private RequestSecurityLevel mockRequestSecurityLevel(int id, String name) {
        RequestSecurityLevel requestSecurityLevel = mock(RequestSecurityLevel.class);
        when(requestSecurityLevel.getId()).thenReturn(id);
        when(requestSecurityLevel.getTranslation()).thenReturn(name);
        return requestSecurityLevel;
    }

    private ResponseSecurityLevel mockResponseSecurityLevel(int id, String name) {
        ResponseSecurityLevel responseSecurityLevel = mock(ResponseSecurityLevel.class);
        when(responseSecurityLevel.getId()).thenReturn(id);
        when(responseSecurityLevel.getTranslation()).thenReturn(name);
        return responseSecurityLevel;
    }
}
