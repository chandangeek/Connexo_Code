/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.users.Group;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.configuration.rest.KeyFunctionTypePrivilegeTranslationKeys;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 9/12/14.
 */
public class ExecutionLevelResourceTest extends DeviceConfigurationApplicationJerseyTest {

    @Override
    protected void setupThesaurus() {
        super.setupThesaurus();
        Stream.of(KeyFunctionTypePrivilegeTranslationKeys.values()).forEach(this::mockTranslation);
        Stream.of(MessageSeeds.values()).forEach(this::mockTranslation);
    }

    private void mockTranslation(KeyFunctionTypePrivilegeTranslationKeys translationKey) {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(translationKey.getDefaultFormat());
        doReturn(messageFormat).when(thesaurus).getFormat(translationKey);
    }

    private void mockTranslation(MessageSeeds messageSeed) {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg()))
            .thenAnswer(invocationOnMock -> MessageFormat.format(messageSeed.getDefaultFormat(), invocationOnMock.getArguments()));
        doReturn(messageFormat).when(thesaurus).getFormat(messageSeed);
    }

    @Test
    public void testAddPrivilege() throws Exception {
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(999L);

        when(deviceConfigurationService.findSecurityPropertySet(999L)).thenReturn(Optional.of(securityPropertySet));
        List<String> executionLevels = Arrays.asList(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1.getPrivilege(),
                DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES4.getPrivilege());
        Response response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/999/executionlevels").request().post(Entity.json(executionLevels));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        ArgumentCaptor<DeviceSecurityUserAction> argumentCaptor = ArgumentCaptor.forClass(DeviceSecurityUserAction.class);
        verify(securityPropertySet, times(2)).addUserAction(argumentCaptor.capture());
        assertThat(argumentCaptor.getAllValues().get(0)).isEqualTo(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1);
        assertThat(argumentCaptor.getAllValues().get(1)).isEqualTo(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES4);
    }

    @Test
    public void testAddUnknownPrivilege() throws Exception {
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(999L);
        when(deviceConfigurationService.findSecurityPropertySet(999L)).thenReturn(Optional.of(securityPropertySet));

        List<String> executionLevels = Arrays.asList("UNKNOWN", DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2.getPrivilege());
        Response response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/999/executionlevels").request().post(Entity.json(executionLevels));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verify(securityPropertySet, never()).addUserAction(anyObject());
        JsonModel jsonModel = JsonModel.create(response.readEntity(String.class));
        assertThat(jsonModel.<Boolean>get("$.success")).isEqualTo(Boolean.FALSE);
        assertThat(jsonModel.<String>get("$.message")).isEqualTo("No such execution levels: UNKNOWN");
        assertThat(jsonModel.<String>get("$.error")).isEqualTo("NoSuchExecutionLevels");
    }

    @Test
    public void testDeletePrivilegeFromSecurityPropertySet() throws Exception {
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(999L);
        when(deviceConfigurationService.findAndLockSecurityPropertySetByIdAndVersion(999L, 1L)).thenReturn(Optional.of(securityPropertySet));

        ExecutionLevelInfo info = new ExecutionLevelInfo();
        info.parent = new VersionInfo<>(999L, 1L);
        Response response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/999/executionlevels/edit.device.security.properties.level4").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        ArgumentCaptor<DeviceSecurityUserAction> argumentCaptor = ArgumentCaptor.forClass(DeviceSecurityUserAction.class);
        verify(securityPropertySet, times(1)).removeUserAction(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES4);
    }

    @Test
    public void testDeletePrivilegeBadVersion() throws Exception {
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(999L);
        when(deviceConfigurationService.findSecurityPropertySet(999L)).thenReturn(Optional.empty());
        when(deviceConfigurationService.findAndLockSecurityPropertySetByIdAndVersion(999L, 1L)).thenReturn(Optional.empty());

        ExecutionLevelInfo info = new ExecutionLevelInfo();
        info.parent = new VersionInfo<>(999L, 1L);
        Response response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/999/executionlevels/edit.device.security.properties.level4").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testGetAvailableExecutionLevelsForVirginSecurityPropertySet() throws Exception {
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(999L);
        when(deviceConfigurationService.findSecurityPropertySet(999L)).thenReturn(Optional.of(securityPropertySet));

        String response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/999/executionlevels/").queryParam("available", true).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.executionLevels")).hasSize(8);
    }

    @Test
    public void testGetAvailableExecutionLevels() throws Exception {
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(999L);
        when(securityPropertySet.getUserActions()).thenReturn(EnumSet.of(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES3, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES4));
        when(deviceConfigurationService.findSecurityPropertySet(999L)).thenReturn(Optional.of(securityPropertySet));

        String response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/999/executionlevels/").queryParam("available", true).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.executionLevels[*].id"))
                .hasSize(4)
                .containsExactly(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1.getPrivilege(), DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2.getPrivilege(), DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES3.getPrivilege(), DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES4.getPrivilege());
        assertThat(jsonModel.<List>get("$.executionLevels[*].name")).containsExactly("Level 1", "Level 2", "Level 3", "Level 4");
    }
    @Test
    public void testGetExecutionLevels() throws Exception {
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(999L);
        when(deviceConfigurationService.findSecurityPropertySet(999L)).thenReturn(Optional.of(securityPropertySet));
        when(securityPropertySet.getUserActions()).thenReturn(EnumSet.of(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2));
        Group group1 = mockUserGroup(66L, "Zulu");
        Group group2 = mockUserGroup(67L, "Alpha");
        Group group3 = mockUserGroup(68L, "Omega");
        when(userService.getGroups()).thenReturn(Arrays.asList(group1, group2, group3));

        String response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/999/executionlevels/").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.executionLevels[*].id"))
                .hasSize(2)
                .containsExactly(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1.getPrivilege(), DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2.getPrivilege());
        assertThat(jsonModel.<List>get("$.executionLevels[*].name")).containsExactly("Level 1", "Level 2");
        assertThat(jsonModel.<List>get("$.executionLevels[0].userRoles")).hasSize(3);
        assertThat(jsonModel.<List<String>>get("$.executionLevels[0].userRoles[*].name")).isSortedAccordingTo(String::compareToIgnoreCase);
    }

    private Group mockUserGroup(long id, String name) {
        Group mock = mock(Group.class);
        when(mock.hasPrivilege(Matchers.matches("MDC"), Matchers.<String>anyObject())).thenReturn(true);
        when(mock.getName()).thenReturn(name);
        when(mock.getId()).thenReturn(id);
        return mock;
    }


}
