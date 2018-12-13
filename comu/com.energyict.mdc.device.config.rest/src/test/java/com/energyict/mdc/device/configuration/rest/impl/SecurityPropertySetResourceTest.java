/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.security.Privileges;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.users.Group;
import com.energyict.mdc.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.configuration.rest.KeyFunctionTypePrivilegeTranslationKeys;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.RequestSecurityLevel;
import com.energyict.mdc.protocol.api.security.ResponseSecurityLevel;
import com.energyict.mdc.protocol.api.security.SecuritySuite;
import com.energyict.mdc.upl.security.AdvancedDeviceProtocolSecurityCapabilities;

import com.google.common.collect.ImmutableSet;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Created by bvn on 9/12/14.
 */
public class SecurityPropertySetResourceTest extends DeviceConfigurationApplicationJerseyTest {
    private static final long OK_VERSION = 24L;
    private static final long BAD_VERSION = 17L;
    private static final String SECURITY_ACCESSOR_NAME = "SecurityAccessorType";

    @Mock
    private SecurityAccessorType securityAccessorType;
    @Mock
    private ValueFactory<SecurityAccessorType> securityAccessorTypeValueFactory;
    @Mock
    private com.energyict.mdc.upl.properties.ValueFactory uplStringValueFactory;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        when(securityAccessorTypeValueFactory.getValueType()).thenReturn(SecurityAccessorType.class);
        when(securityAccessorType.getId()).thenReturn(11L);
        when(securityAccessorType.getName()).thenReturn(SECURITY_ACCESSOR_NAME);
        when(securityAccessorTypeValueFactory.fromStringValue(anyString()))
                .thenAnswer(invocation -> SECURITY_ACCESSOR_NAME.equals(invocation.getArgumentAt(0, String.class)) ? securityAccessorType : null);
        when(securityAccessorTypeValueFactory.toStringValue(any()))
                .thenAnswer(invocation -> invocation.getArgumentAt(0, SecurityAccessorType.class).getName());

        when(uplStringValueFactory.getValueTypeName()).thenReturn(String.class.getName());
        when(uplStringValueFactory.fromStringValue(anyString())).thenAnswer(invocation -> invocation.getArgumentAt(0, String.class));
        when(uplStringValueFactory.toStringValue(any())).thenAnswer(invocation -> String.valueOf(invocation.getArgumentAt(0, Object.class)));

    }

    @Override
    protected void setupThesaurus() {
        super.setupThesaurus();
        Stream.of(KeyFunctionTypePrivilegeTranslationKeys.values()).forEach(this::mockTranslation);
    }

    private void mockTranslation(KeyFunctionTypePrivilegeTranslationKeys translationKey) {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(translationKey.getDefaultFormat());
        doReturn(messageFormat).when(thesaurus).getFormat(translationKey);
    }

    @Test
    public void testGetSecurityPropertySet() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(Optional.of(deviceType));
        Group group1 = mockUserGroup(66L, "Z - user group 1");
        Group group2 = mockUserGroup(67L, "A - user group 2");
        Group group3 = mockUserGroup(68L, "O - user group 1");
        when(userService.getGroups()).thenReturn(Arrays.asList(group2, group1, group3));
        SecurityPropertySet sps1 = mockSecurityPropertySet(101L, "Primary", "Primary client", 1001, "Auth1", 2001, "Encrypt1", 3001, "Suite1", 4001, "RequestSec1", 5001, "ResponseSec1"
        );
        when(sps1.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        SecurityPropertySet sps2 = mockSecurityPropertySet(102L, "Secondary", "Secondary client", 1002, "Auth2", 2002, "Encrypt2", 3002, "Suite2", 4002, "RequestSec2", 5002, "ResponseSec2"
        );
        when(sps2.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1, sps2));
        when(deviceConfigurationService.findDeviceConfiguration(456L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(456L, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));
        Map response = target("/devicetypes/123/deviceconfigurations/456/securityproperties").request().get(Map.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.data")).hasSize(2);
        assertThat(jsonModel.<Integer>get("$.data[0].id")).isEqualTo(101);
        assertThat(jsonModel.<String>get("$.data[0].name")).isEqualTo("Primary");
        assertThat(jsonModel.<String>get("$.data[0].client.propertyValueInfo.value")).isEqualTo("Primary client");
        assertThat(jsonModel.<Integer>get("$.data[0].authenticationLevelId")).isEqualTo(1001);
        assertThat(jsonModel.<Integer>get("$.data[0].authenticationLevel.id")).isEqualTo(1001);
        assertThat(jsonModel.<String>get("$.data[0].authenticationLevel.name")).isEqualTo("Auth1");
        assertThat(jsonModel.<Integer>get("$.data[0].encryptionLevelId")).isEqualTo(2001);
        assertThat(jsonModel.<Integer>get("$.data[0].encryptionLevel.id")).isEqualTo(2001);
        assertThat(jsonModel.<String>get("$.data[0].encryptionLevel.name")).isEqualTo("Encrypt1");
        assertThat(jsonModel.<Integer>get("$.data[0].securitySuiteId")).isEqualTo(3001);
        assertThat(jsonModel.<Integer>get("$.data[0].securitySuite.id")).isEqualTo(3001);
        assertThat(jsonModel.<String>get("$.data[0].securitySuite.name")).isEqualTo("Suite1");
        assertThat(jsonModel.<Integer>get("$.data[0].requestSecurityLevelId")).isEqualTo(4001);
        assertThat(jsonModel.<Integer>get("$.data[0].requestSecurityLevel.id")).isEqualTo(4001);
        assertThat(jsonModel.<String>get("$.data[0].requestSecurityLevel.name")).isEqualTo("RequestSec1");
        assertThat(jsonModel.<Integer>get("$.data[0].responseSecurityLevelId")).isEqualTo(5001);
        assertThat(jsonModel.<Integer>get("$.data[0].responseSecurityLevel.id")).isEqualTo(5001);
        assertThat(jsonModel.<String>get("$.data[0].responseSecurityLevel.name")).isEqualTo("ResponseSec1");
    }

    @Test
    public void testGetSecurityPropertySets() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(Optional.of(deviceType));
        Group group1 = mockUserGroup(66L, "Z - user group 1", Collections.singletonList(Privileges.Constants.EDIT_SECURITY_PROPERTIES_1));
        Group group2 = mockUserGroup(67L, "A - user group 2", Collections.singletonList(Privileges.Constants.VIEW_SECURITY_PROPERTIES_4));
        Group group3 = mockUserGroup(68L, "O - user group 3", Collections.emptyList());
        when(userService.getGroups()).thenReturn(Arrays.asList(group2, group1, group3));
        SecurityPropertySet sps1 = mockSecurityPropertySet(101L, "Primary", "Primary client", 1001, "Auth1", 2001, "Encrypt1", 3001, "Suite1", 4001, "RequestSec1", 5001, "ResponseSec1"
        );
        when(sps1.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        SecurityPropertySet sps2 = mockSecurityPropertySet(102L, "Secondary", "Secondary client", 1002, "Auth2", 2002, "Encrypt2", 3002, "Suite2", 4002, "RequestSec2", 5002, "ResponseSec2"
        );
        when(sps2.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1, sps2));
        when(deviceConfigurationService.findDeviceConfiguration(456L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(456L, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));
        String response = target("/devicetypes/123/deviceconfigurations/456/securityproperties").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);

        assertThat(jsonModel.<List>get("$.data")).hasSize(2);
    }

    @Test
    public void testGetAuthenticationLevels() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(Optional.of(deviceType));
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Arrays.asList(DeviceProtocolAuthenticationAccessLevels.values()));

        SecurityPropertySet sps1 = mockSecurityPropertySet(101L, "Primary", "Primary client", 1001, "Auth1", 2001, "Encrypt1", 3001, "Suite1", 4001, "RequestSec1", 5001, "ResponseSec1");
        when(sps1.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        SecurityPropertySet sps2 = mockSecurityPropertySet(102L, "Secondary", "Secondary client", 1002, "Auth2", 2002, "Encrypt2", 3002, "Suite2", 4002, "RequestSec2", 5002, "ResponseSec2");
        when(sps2.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1, sps2));
        when(deviceConfigurationService.findDeviceConfiguration(456L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(456L, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));

        String response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/authlevels").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.data")).hasSize(2);
    }

    @Test
    public void testGetAuthenticationLevelsWhenHavingSecuritySuiteUriParameter() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(Optional.of(deviceType));
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class, withSettings().extraInterfaces(AdvancedDeviceProtocolSecurityCapabilities.class));
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        com.energyict.mdc.upl.security.SecuritySuite securitySuite = mock(com.energyict.mdc.upl.security.SecuritySuite.class);
        when(securitySuite.getId()).thenReturn(3001);
        when(securitySuite.getAuthenticationAccessLevels()).thenReturn(Collections.singletonList(DeviceProtocolAuthenticationAccessLevels.ONE));
        when(((AdvancedDeviceProtocolSecurityCapabilities) deviceProtocol).getSecuritySuites()).thenReturn(Collections.singletonList(securitySuite));
        when(deviceProtocol.getAuthenticationAccessLevels()).thenReturn(Arrays.asList(DeviceProtocolAuthenticationAccessLevels.values()));

        SecurityPropertySet sps1 = mockSecurityPropertySet(101L, "Primary", "Primary client", 1001, "Auth1", 2001, "Encrypt1", 3001, "Suite1", 4001, "RequestSec1", 5001, "ResponseSec1");
        when(sps1.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        SecurityPropertySet sps2 = mockSecurityPropertySet(102L, "Secondary", "Secondary client", 1002, "Auth2", 2002, "Encrypt2", 3002, "Suite2", 4002, "RequestSec2", 5002, "ResponseSec2");
        when(sps2.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1, sps2));
        when(deviceConfigurationService.findDeviceConfiguration(456L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(456L, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));

        String response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/authlevels").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.data")).hasSize(2);

        response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/authlevels").queryParam("securitySuiteId", "3001").request().get(String.class);
        jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.data")).hasSize(1);
    }

    @Test
    public void testGetEncryptionLevels() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(Optional.of(deviceType));
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceProtocol.getEncryptionAccessLevels()).thenReturn(Arrays.asList(DeviceProtocolEncryptionAccessLevels.values()));

        SecurityPropertySet sps1 = mockSecurityPropertySet(101L, "Primary", "Primary client", 1001, "Auth1", 2001, "Encrypt1", 3001, "Suite1", 4001, "RequestSec1", 5001, "ResponseSec1");
        when(sps1.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        SecurityPropertySet sps2 = mockSecurityPropertySet(102L, "Secondary", "Secondary client", 1002, "Auth2", 2002, "Encrypt2", 3002, "Suite2", 4002, "RequestSec2", 5002, "ResponseSec2");
        when(sps2.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1, sps2));
        when(deviceConfigurationService.findDeviceConfiguration(456L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(456L, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));
        String response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/enclevels").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);

        assertThat(jsonModel.<List>get("$.data")).hasSize(2);
    }

    @Test
    public void testGetEncryptionLevelsWhenHavingSecuritySuiteUriParameter() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(Optional.of(deviceType));
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class, withSettings().extraInterfaces(AdvancedDeviceProtocolSecurityCapabilities.class));
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        com.energyict.mdc.upl.security.SecuritySuite securitySuite = mock(com.energyict.mdc.upl.security.SecuritySuite.class);
        when(securitySuite.getId()).thenReturn(3001);
        when(securitySuite.getEncryptionAccessLevels()).thenReturn(Collections.singletonList(DeviceProtocolEncryptionAccessLevels.ONE));
        when(((AdvancedDeviceProtocolSecurityCapabilities) deviceProtocol).getSecuritySuites()).thenReturn(Collections.singletonList(securitySuite));
        when(deviceProtocol.getEncryptionAccessLevels()).thenReturn(Arrays.asList(DeviceProtocolEncryptionAccessLevels.values()));

        SecurityPropertySet sps1 = mockSecurityPropertySet(101L, "Primary", "Primary client", 1001, "Auth1", 2001, "Encrypt1", 3001, "Suite1", 4001, "RequestSec1", 5001, "ResponseSec1");
        when(sps1.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        SecurityPropertySet sps2 = mockSecurityPropertySet(102L, "Secondary", "Secondary client", 1002, "Auth2", 2002, "Encrypt2", 3002, "Suite2", 4002, "RequestSec2", 5002, "ResponseSec2");
        when(sps2.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1, sps2));
        when(deviceConfigurationService.findDeviceConfiguration(456L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(456L, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));

        String response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/enclevels").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.data")).hasSize(2);

        response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/enclevels").queryParam("securitySuiteId", "3001").request().get(String.class);
        jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.data")).hasSize(1);
    }

    @Test
    public void testGetSecuritySuitesWhenDeviceProtocolDoesNotSupportThem() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(Optional.of(deviceType));
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);

        SecurityPropertySet sps1 = mockSecurityPropertySet(101L, "Primary", "Primary client", 1001, "Auth1", 2001, "Encrypt1", 3001, "Suite1", 4001, "RequestSec1", 5001, "ResponseSec1");
        when(sps1.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        SecurityPropertySet sps2 = mockSecurityPropertySet(102L, "Secondary", "Secondary client", 1002, "Auth2", 2002, "Encrypt2", 3002, "Suite2", 4002, "RequestSec2", 5002, "ResponseSec2");
        when(sps2.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1, sps2));
        when(deviceConfigurationService.findDeviceConfiguration(456L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(456L, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));
        String response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/securitysuites").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);

        assertThat(jsonModel.<List>get("$.data")).hasSize(0);
    }

    @Test
    public void testGetSecuritySuites() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(Optional.of(deviceType));
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class, withSettings().extraInterfaces(AdvancedDeviceProtocolSecurityCapabilities.class));
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(((AdvancedDeviceProtocolSecurityCapabilities) deviceProtocol).getSecuritySuites()).thenReturn(Arrays.asList(DeviceProtocolSecuritySuite.values()));

        SecurityPropertySet sps1 = mockSecurityPropertySet(101L, "Primary", "Primary client", 1001, "Auth1", 2001, "Encrypt1", 3001, "Suite1", 4001, "RequestSec1", 5001, "ResponseSec1");
        when(sps1.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        SecurityPropertySet sps2 = mockSecurityPropertySet(102L, "Secondary", "Secondary client", 1002, "Auth2", 2002, "Encrypt2", 3002, "Suite2", 4002, "RequestSec2", 5002, "ResponseSec2");
        when(sps2.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1, sps2));
        when(deviceConfigurationService.findDeviceConfiguration(456L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(456L, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));
        String response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/securitysuites").queryParam("securitySuiteId", "3001").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);

        assertThat(jsonModel.<List>get("$.data")).hasSize(2);
    }

    @Test
    public void testGetRequestSecurityLevels() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(Optional.of(deviceType));
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class, withSettings().extraInterfaces(AdvancedDeviceProtocolSecurityCapabilities.class));
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        com.energyict.mdc.upl.security.SecuritySuite securitySuite = mock(com.energyict.mdc.upl.security.SecuritySuite.class);
        when(securitySuite.getId()).thenReturn(3001);
        when(securitySuite.getRequestSecurityLevels()).thenReturn(Collections.singletonList(DeviceProtocolRequestSecurityLevels.ONE));
        when(((AdvancedDeviceProtocolSecurityCapabilities) deviceProtocol).getSecuritySuites()).thenReturn(Collections.singletonList(securitySuite));
        when(((AdvancedDeviceProtocolSecurityCapabilities) deviceProtocol).getRequestSecurityLevels()).thenReturn(Arrays.asList(DeviceProtocolRequestSecurityLevels.values()));

        SecurityPropertySet sps1 = mockSecurityPropertySet(101L, "Primary", "Primary client", 1001, "Auth1", 2001, "Encrypt1", 3001, "Suite1", 4001, "RequestSec1", 5001, "ResponseSec1");
        when(sps1.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        SecurityPropertySet sps2 = mockSecurityPropertySet(102L, "Secondary", "Secondary client", 1002, "Auth2", 2002, "Encrypt2", 3002, "Suite2", 4002, "RequestSec2", 5002, "ResponseSec2");
        when(sps2.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1, sps2));
        when(deviceConfigurationService.findDeviceConfiguration(456L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(456L, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));

        String response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/reqsecuritylevels").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.data")).hasSize(2);

        response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/reqsecuritylevels").queryParam("securitySuiteId", "3001").request().get(String.class);
        jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.data")).hasSize(1);
    }

    @Test
    public void testGetResponseSecurityLevels() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(Optional.of(deviceType));
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class, withSettings().extraInterfaces(AdvancedDeviceProtocolSecurityCapabilities.class));
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        com.energyict.mdc.upl.security.SecuritySuite securitySuite = mock(com.energyict.mdc.upl.security.SecuritySuite.class);
        when(securitySuite.getId()).thenReturn(3001);
        when(securitySuite.getResponseSecurityLevels()).thenReturn(Collections.singletonList(DeviceProtocolResponseSecurityLevels.ONE));
        when(((AdvancedDeviceProtocolSecurityCapabilities) deviceProtocol).getSecuritySuites()).thenReturn(Collections.singletonList(securitySuite));
        when(((AdvancedDeviceProtocolSecurityCapabilities) deviceProtocol).getResponseSecurityLevels()).thenReturn(Arrays.asList(DeviceProtocolResponseSecurityLevels.values()));

        SecurityPropertySet sps1 = mockSecurityPropertySet(101L, "Primary", "Primary client", 1001, "Auth1", 2001, "Encrypt1", 3001, "Suite1", 4001, "RequestSec1", 5001, "ResponseSec1");
        when(sps1.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        SecurityPropertySet sps2 = mockSecurityPropertySet(102L, "Secondary", "Secondary client", 1002, "Auth2", 2002, "Encrypt2", 3002, "Suite2", 4002, "RequestSec2", 5002, "ResponseSec2");
        when(sps2.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1, sps2));
        when(deviceConfigurationService.findDeviceConfiguration(456L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(456L, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));

        String response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/respsecuritylevels").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.data")).hasSize(2);

        response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/respsecuritylevels").queryParam("securitySuiteId", "3001").request().get(String.class);
        jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.data")).hasSize(1);
    }

    @Test
    public void testGetConfSecurityProperties() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        SecurityPropertySetBuilder securityPropertySetBuilder = new SecurityPropertySetBuilder();
        when(deviceConfiguration.createSecurityPropertySet(anyString())).thenReturn(securityPropertySetBuilder);

        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(Optional.of(deviceType));
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class, withSettings().extraInterfaces(AdvancedDeviceProtocolSecurityCapabilities.class));
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        com.energyict.mdc.upl.security.SecuritySuite securitySuite = mock(com.energyict.mdc.upl.security.SecuritySuite.class);
        when(securitySuite.getId()).thenReturn(3001);
        when(securitySuite.getResponseSecurityLevels()).thenReturn(Collections.singletonList(DeviceProtocolResponseSecurityLevels.ONE));
        when(((AdvancedDeviceProtocolSecurityCapabilities) deviceProtocol).getSecuritySuites()).thenReturn(Collections.singletonList(securitySuite));
        when(((AdvancedDeviceProtocolSecurityCapabilities) deviceProtocol).getResponseSecurityLevels()).thenReturn(Arrays.asList(DeviceProtocolResponseSecurityLevels.values()));

        SecurityPropertySet sps1 = mockSecurityPropertySet(101L, "Primary", "Primary client", 1001, "Auth1", 2001, "Encrypt1", 3001, "Suite1", 4001, "RequestSec1", 5001, "ResponseSec1");
        when(sps1.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        Set<PropertySpec> propertySpecs = ImmutableSet.of(mockPropertySpec("Prop1", securityAccessorTypeValueFactory), mockPropertySpec("Prop2", securityAccessorTypeValueFactory));
        when(sps1.getPropertySpecs()).thenReturn(propertySpecs);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Collections.singletonList(sps1));
        when(deviceConfigurationService.findDeviceConfiguration(456L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(456L, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));

        String response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/confsecurityproperties")
                .queryParam("authenticationLevelId", "1")
                .queryParam("encryptionLevelId", "2")
                .queryParam("securitySuiteId", "3")
                .queryParam("requestSecurityLevelId", "4")
                .queryParam("responseSecurityLevelId", "5")
                .request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.data")).hasSize(2);

        response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/confsecurityproperties")
                .queryParam("authenticationLevelId", "10")
                .queryParam("encryptionLevelId", "20")
                .queryParam("securitySuiteId", "30")
                .queryParam("requestSecurityLevelId", "40")
                .queryParam("responseSecurityLevelId", "50")
                .request().get(String.class);
        jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.data")).hasSize(1);

        response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/confsecurityproperties")
                .queryParam("authenticationLevelId", "-1")
                .queryParam("encryptionLevelId", "-1")
                .queryParam("securitySuiteId", "-1")
                .queryParam("requestSecurityLevelId", "-1")
                .queryParam("responseSecurityLevelId", "-1")
                .request().get(String.class);
        jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.data")).isEmpty();

        response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/confsecurityproperties")
                .request().get(String.class);
        jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.data")).isEmpty();
    }

    @Test
    public void testDeleteSecuritySetBadVersion() {
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(15L);
        when(deviceConfiguration.getVersion()).thenReturn(OK_VERSION);
        when(deviceConfigurationService.findDeviceConfiguration(15L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(15L, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(15L, BAD_VERSION)).thenReturn(Optional.empty());

        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(11L);
        when(securityPropertySet.getName()).thenReturn("name");
        when(securityPropertySet.getVersion()).thenReturn(OK_VERSION);
        when(securityPropertySet.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfigurationService.findSecurityPropertySet(11L)).thenReturn(Optional.of(securityPropertySet));
        when(deviceConfigurationService.findAndLockSecurityPropertySetByIdAndVersion(11L, OK_VERSION)).thenReturn(Optional.of(securityPropertySet));
        when(deviceConfigurationService.findAndLockSecurityPropertySetByIdAndVersion(11L, BAD_VERSION)).thenReturn(Optional.empty());

        SecurityPropertySetInfo info = new SecurityPropertySetInfo();
        info.id = 11L;
        info.version = BAD_VERSION;
        info.parent = new VersionInfo<>(15L, OK_VERSION);

        Response response = target("/devicetypes/123/deviceconfigurations/15/securityproperties/11").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testUpdateSecurityPropertySetDoesAddNewAttributes() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        com.energyict.mdc.upl.properties.PropertySpec clientPropertySpec = mock(com.energyict.mdc.upl.properties.PropertySpec.class);
        when(clientPropertySpec.getName()).thenReturn("Client");
        when(clientPropertySpec.getValueFactory()).thenReturn(uplStringValueFactory);
        when(deviceProtocol.getClientSecurityPropertySpec()).thenReturn(Optional.of(clientPropertySpec));
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(Optional.of(deviceType));
        Group group1 = mockUserGroup(66L, "Z - user group 1");
        Group group2 = mockUserGroup(67L, "A - user group 2");
        Group group3 = mockUserGroup(68L, "O - user group 1");
        when(userService.getGroups()).thenReturn(Arrays.asList(group2, group1, group3));
        SecurityPropertySet sps1 = mockSecurityPropertySet(101L, "Primary", "Primary client", 1001, "Auth1", 2001, "Encrypt1", 3001, "Suite1", 4001, "RequestSec1", 5001, "ResponseSec1"
        );
        when(sps1.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        ConfigurationSecurityProperty configurationSecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(configurationSecurityProperty.getName()).thenReturn("Password");
        when(sps1.getConfigurationSecurityProperties()).thenReturn(Collections.singletonList(configurationSecurityProperty));
        SecurityPropertySet sps2 = mockSecurityPropertySet(102L, "Secondary", "Secondary client", 1002, "Auth2", 2002, "Encrypt2", 3002, "Suite2", 4002, "RequestSec2", 5002, "ResponseSec2"
        );
        when(sps2.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1, sps2));
        when(deviceConfigurationService.findDeviceConfiguration(456L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(456L, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockSecurityPropertySetByIdAndVersion(101, OK_VERSION)).thenReturn(Optional.of(sps1));

        PropertyInfo propertyInfo = new PropertyInfo("EncryptionKey", "EncryptionKey", new PropertyValueInfo<>(SECURITY_ACCESSOR_NAME, ""), new PropertyTypeInfo(), true);
        PropertySpec propertySpec = mockPropertySpec("EncryptionKey", securityAccessorTypeValueFactory);
        when(sps1.getPropertySpecs()).thenReturn(Collections.singleton(propertySpec));

        SecurityPropertySetInfo info = new SecurityPropertySetInfo();
        info.name = "New name";
        info.client = new PropertyInfo("Client", "Client", new PropertyValueInfo<>("New client", ""), new PropertyTypeInfo(), true);
        info.authenticationLevelId = 1002;
        info.encryptionLevelId = 2001;
        info.securitySuiteId = 3002;
        info.requestSecurityLevelId = 4002;
        info.responseSecurityLevelId = 5002;
        info.properties = Collections.singletonList(propertyInfo);
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(deviceConfiguration.getId(), OK_VERSION);

        Response response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/101").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(sps1).setName(info.name);
        verify(sps1).setClient(info.client.getPropertyValueInfo().getValue());
        verify(sps1).setAuthenticationLevelId(info.authenticationLevelId);
        verify(sps1).setEncryptionLevelId(info.encryptionLevelId);
        verify(sps1).setSecuritySuiteId(info.securitySuiteId);
        verify(sps1).setRequestSecurityLevelId(info.requestSecurityLevelId);
        verify(sps1).setResponseSecurityLevelId(info.responseSecurityLevelId);
        verify(sps1).removeConfigurationSecurityProperty("Password"); //V Verify the old attribute is removed
        verify(sps1).addConfigurationSecurityProperty("EncryptionKey", securityAccessorType); //V Verify the new attribute is added
    }

    @Test
    public void testUpdateSecurityPropertySetDoesRemoveOldAttributes() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = mock(DeviceProtocolPluggableClass.class);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        com.energyict.mdc.upl.properties.PropertySpec clientPropertySpec = mock(com.energyict.mdc.upl.properties.PropertySpec.class);
        when(clientPropertySpec.getName()).thenReturn("Client");
        when(clientPropertySpec.getValueFactory()).thenReturn(uplStringValueFactory);
        when(deviceProtocol.getClientSecurityPropertySpec()).thenReturn(Optional.of(clientPropertySpec));
        when(deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(Optional.of(deviceType));
        Group group1 = mockUserGroup(66L, "Z - user group 1");
        Group group2 = mockUserGroup(67L, "A - user group 2");
        Group group3 = mockUserGroup(68L, "O - user group 1");
        when(userService.getGroups()).thenReturn(Arrays.asList(group2, group1, group3));
        SecurityPropertySet sps1 = mockSecurityPropertySet(101L, "Primary", "Primary client", 1001, "Auth1", 2001, "Encrypt1", 3001, "Suite1", 4001, "RequestSec1", 5001, "ResponseSec1"
        );
        when(sps1.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        ConfigurationSecurityProperty configurationSecurityProperty = mock(ConfigurationSecurityProperty.class);
        when(configurationSecurityProperty.getName()).thenReturn("Password");
        when(sps1.getConfigurationSecurityProperties()).thenReturn(Collections.singletonList(configurationSecurityProperty));
        SecurityPropertySet sps2 = mockSecurityPropertySet(102L, "Secondary", "Secondary client", 1002, "Auth2", 2002, "Encrypt2", 3002, "Suite2", 4002, "RequestSec2", 5002, "ResponseSec2"
        );
        when(sps2.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(sps1, sps2));
        when(deviceConfigurationService.findDeviceConfiguration(456L)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(456L, OK_VERSION)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigurationService.findAndLockSecurityPropertySetByIdAndVersion(101, OK_VERSION)).thenReturn(Optional.of(sps1));

        SecurityPropertySetInfo info = new SecurityPropertySetInfo();
        info.name = "New name";
        info.client = new PropertyInfo("Client", "Client", new PropertyValueInfo<>("New client", ""), new PropertyTypeInfo(), true);
        info.authenticationLevelId = 1002;
        info.encryptionLevelId = 2001;
        info.securitySuiteId = 3002;
        info.requestSecurityLevelId = 4002;
        info.responseSecurityLevelId = 5002;
        info.properties = Collections.emptyList();
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(deviceConfiguration.getId(), OK_VERSION);

        Response response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/101").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(sps1).setName(info.name);
        verify(sps1).setClient(info.client.getPropertyValueInfo().getValue());
        verify(sps1).setAuthenticationLevelId(info.authenticationLevelId);
        verify(sps1).setEncryptionLevelId(info.encryptionLevelId);
        verify(sps1).setSecuritySuiteId(info.securitySuiteId);
        verify(sps1).setRequestSecurityLevelId(info.requestSecurityLevelId);
        verify(sps1).setResponseSecurityLevelId(info.responseSecurityLevelId);
        verify(sps1).removeConfigurationSecurityProperty("Password"); //V Verify the old attribute is removed
    }

    private Group mockUserGroup(long id, String name) {
        Group mock = mock(Group.class);
        when(mock.hasPrivilege(Matchers.matches("MDC"), Matchers.<String>anyObject())).thenReturn(true);
        when(mock.getName()).thenReturn(name);
        when(mock.getId()).thenReturn(id);
        return mock;
    }

    private Group mockUserGroup(long id, String name, List<String> privileges) {
        Group mock = mock(Group.class);
        when(mock.hasPrivilege(Matchers.matches("MDC"), Matchers.<String>anyObject())).then(invocationOnMock -> privileges.contains(invocationOnMock.getArguments()[1]));
        when(mock.getName()).thenReturn(name);
        when(mock.getId()).thenReturn(id);
        return mock;
    }

    private SecurityPropertySet mockSecurityPropertySet(Long id, String name, String client, Integer authenticationAccessLevelId, String authenticationAccessLevelName, Integer encryptionAccessLevelId, String encryptionAccessLevelName,
                                                        Integer securitySuiteId, String securitySuiteName, Integer requestSecurityLevelId, String requestSecurityLevelName, Integer responseSecurityLevelId, String responseSecurityLevelName) {
        SecurityPropertySet mock = mock(SecurityPropertySet.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        when(mock.getClient()).thenReturn(client);
        PropertySpec clientPropertySpec = mockPropertySpec("Client", new StringFactory());
        when(mock.getClientSecurityPropertySpec()).thenReturn(Optional.of(clientPropertySpec));
        AuthenticationDeviceAccessLevel authenticationAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        when(authenticationAccessLevel.getId()).thenReturn(authenticationAccessLevelId);
        when(authenticationAccessLevel.getTranslation()).thenReturn(authenticationAccessLevelName);
        when(mock.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationAccessLevel);
        EncryptionDeviceAccessLevel encryptionAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionAccessLevel.getId()).thenReturn(encryptionAccessLevelId);
        when(encryptionAccessLevel.getTranslation()).thenReturn(encryptionAccessLevelName);
        when(mock.getEncryptionDeviceAccessLevel()).thenReturn(encryptionAccessLevel);
        SecuritySuite securitySuite = mock(SecuritySuite.class);
        when(securitySuite.getId()).thenReturn(securitySuiteId);
        when(securitySuite.getTranslation()).thenReturn(securitySuiteName);
        when(mock.getSecuritySuite()).thenReturn(securitySuite);
        RequestSecurityLevel requestSecurityLevel = mock(RequestSecurityLevel.class);
        when(requestSecurityLevel.getId()).thenReturn(requestSecurityLevelId);
        when(requestSecurityLevel.getTranslation()).thenReturn(requestSecurityLevelName);
        when(mock.getRequestSecurityLevel()).thenReturn(requestSecurityLevel);
        ResponseSecurityLevel responseSecurityLevel = mock(ResponseSecurityLevel.class);
        when(responseSecurityLevel.getId()).thenReturn(responseSecurityLevelId);
        when(responseSecurityLevel.getTranslation()).thenReturn(responseSecurityLevelName);
        when(mock.getResponseSecurityLevel()).thenReturn(responseSecurityLevel);
        when(mock.getVersion()).thenReturn(OK_VERSION);
        return mock;
    }

    private class SecurityPropertySetBuilder implements com.energyict.mdc.device.config.SecurityPropertySetBuilder {

        private int authenticationLevel;
        private int encryptionLevel;
        private int securitySuite;
        private int requestSecurityLevel;
        private int responseSecurityLevel;

        @Override
        public com.energyict.mdc.device.config.SecurityPropertySetBuilder authenticationLevel(int level) {
            authenticationLevel = level;
            return this;
        }

        @Override
        public com.energyict.mdc.device.config.SecurityPropertySetBuilder encryptionLevel(int level) {
            encryptionLevel = level;
            return this;
        }

        @Override
        public com.energyict.mdc.device.config.SecurityPropertySetBuilder client(Object client) {
            return this;
        }

        @Override
        public com.energyict.mdc.device.config.SecurityPropertySetBuilder securitySuite(int suite) {
            securitySuite = suite;
            return this;
        }

        @Override
        public com.energyict.mdc.device.config.SecurityPropertySetBuilder requestSecurityLevel(int level) {
            requestSecurityLevel = level;
            return this;
        }

        @Override
        public com.energyict.mdc.device.config.SecurityPropertySetBuilder responseSecurityLevel(int level) {
            responseSecurityLevel = level;
            return this;
        }

        @Override
        public com.energyict.mdc.device.config.SecurityPropertySetBuilder addConfigurationSecurityProperty(String name, SecurityAccessorType keyAccessor) {
            return this;
        }

        @Override
        public Set<PropertySpec> getPropertySpecs() {
            if (authenticationLevel == 1 && encryptionLevel == 2 && securitySuite == 3 && requestSecurityLevel == 4 && responseSecurityLevel == 5) {
                return ImmutableSet.of(mockPropertySpec("Prop1", securityAccessorTypeValueFactory), mockPropertySpec("Prop2", securityAccessorTypeValueFactory));
            } else if (authenticationLevel == 10 && encryptionLevel == 20 && securitySuite == 30 && requestSecurityLevel == 40 && responseSecurityLevel == 50) {
                return Collections.singleton(mockPropertySpec("Prop1", securityAccessorTypeValueFactory));
            } else {
                return Collections.emptySet();
            }
        }

        @Override
        public SecurityPropertySet build() {
            return null;
        }
    }

    private enum DeviceProtocolAuthenticationAccessLevels implements com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel {
        ONE {
            @Override
            public int getId() {
                return 1;
            }
        },
        TWO {
            @Override
            public int getId() {
                return 2;
            }
        };

        @Override
        public String getTranslationKey() {
            return "DeviceProtocolAuthenticationAccessLevels" + this.name();
        }

        @Override
        public String getDefaultTranslation() {
            return "DeviceProtocolAuthenticationAccessLevels" + this.name();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }

    }

    private enum DeviceProtocolEncryptionAccessLevels implements com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel {
        ONE {
            @Override
            public int getId() {
                return 1;
            }
        },
        TWO {
            @Override
            public int getId() {
                return 2;
            }
        };

        @Override
        public String getTranslationKey() {
            return "DeviceProtocolEncryptionAccessLevels" + this.name();
        }

        @Override
        public String getDefaultTranslation() {
            return "DeviceProtocolEncryptionAccessLevels" + this.name();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }

    }

    private enum DeviceProtocolSecuritySuite implements com.energyict.mdc.upl.security.SecuritySuite {
        ONE {
            @Override
            public int getId() {
                return 1;
            }
        },
        TWO {
            @Override
            public int getId() {
                return 2;
            }
        };

        @Override
        public String getTranslationKey() {
            return "DeviceProtocolSecuritySuite" + this.name();
        }

        @Override
        public String getDefaultTranslation() {
            return "DeviceProtocolSecuritySuite" + this.name();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }


        @Override
        public List<com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
            return Collections.emptyList();
        }

        @Override
        public List<com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
            return Collections.emptyList();
        }

        @Override
        public List<com.energyict.mdc.upl.security.RequestSecurityLevel> getRequestSecurityLevels() {
            return Collections.emptyList();
        }

        @Override
        public List<com.energyict.mdc.upl.security.ResponseSecurityLevel> getResponseSecurityLevels() {
            return Collections.emptyList();
        }
    }

    private enum DeviceProtocolRequestSecurityLevels implements com.energyict.mdc.upl.security.RequestSecurityLevel {
        ONE {
            @Override
            public int getId() {
                return 1;
            }
        },
        TWO {
            @Override
            public int getId() {
                return 2;
            }
        };

        @Override
        public String getTranslationKey() {
            return "DeviceProtocolRequestSecurityLevels" + this.name();
        }

        @Override
        public String getDefaultTranslation() {
            return "DeviceProtocolRequestSecurityLevels" + this.name();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }
    }

    private enum DeviceProtocolResponseSecurityLevels implements com.energyict.mdc.upl.security.ResponseSecurityLevel {
        ONE {
            @Override
            public int getId() {
                return 1;
            }
        },
        TWO {
            @Override
            public int getId() {
                return 2;
            }
        };

        @Override
        public String getTranslationKey() {
            return "DeviceProtocolResponseSecurityLevels" + this.name();
        }

        @Override
        public String getDefaultTranslation() {
            return "DeviceProtocolResponseSecurityLevels" + this.name();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getSecurityProperties() {
            return Collections.emptyList();
        }
    }
}
