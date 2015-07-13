package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.users.Group;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.jayway.jsonpath.JsonModel;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 9/12/14.
 */
public class ExecutionLevelResourceTest extends DeviceConfigurationApplicationJerseyTest {

    @Test
    public void testAddPrivilege() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(Optional.of(deviceType));
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(999L);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(securityPropertySet));

        List<String> executionLevels = Arrays.asList(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1.getPrivilege(), DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES4.getPrivilege());
        Response response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/999/executionlevels").request().post(Entity.json(executionLevels));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        ArgumentCaptor<DeviceSecurityUserAction> argumentCaptor = ArgumentCaptor.forClass(DeviceSecurityUserAction.class);
        verify(securityPropertySet, times(2)).addUserAction(argumentCaptor.capture());
        assertThat(argumentCaptor.getAllValues().get(0)).isEqualTo(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1);
        assertThat(argumentCaptor.getAllValues().get(1)).isEqualTo(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES4);
    }

    @Test
    public void testAddUnknownPrivilege() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(Optional.of(deviceType));
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(999L);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(securityPropertySet));

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
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(Optional.of(deviceType));
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(999L);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(securityPropertySet));

        Response response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/999/executionlevels/edit.device.security.properties.level4").request().delete();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        ArgumentCaptor<DeviceSecurityUserAction> argumentCaptor = ArgumentCaptor.forClass(DeviceSecurityUserAction.class);
        verify(securityPropertySet, times(1)).removeUserAction(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES4);
    }

    @Test
    public void testGetAvailableExecutionLevelsForVirginSecurityPropertySet() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(Optional.of(deviceType));
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(999L);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(securityPropertySet));

        String response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/999/executionlevels/").queryParam("available", true).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.executionLevels")).hasSize(8);
    }

    @Test
    public void testGetAvailableExecutionLevels() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(Optional.of(deviceType));
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(999L);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(securityPropertySet));
        when(securityPropertySet.getUserActions()).thenReturn(EnumSet.of(DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES1, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES2, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES3, DeviceSecurityUserAction.VIEWDEVICESECURITYPROPERTIES4));

        String response = target("/devicetypes/123/deviceconfigurations/456/securityproperties/999/executionlevels/").queryParam("available", true).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<List>get("$.executionLevels[*].id"))
                .hasSize(4)
                .containsExactly(DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES1.getPrivilege(), DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES2.getPrivilege(), DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES3.getPrivilege(), DeviceSecurityUserAction.EDITDEVICESECURITYPROPERTIES4.getPrivilege());
        assertThat(jsonModel.<List>get("$.executionLevels[*].name")).containsExactly("Edit device security settings (level 1)", "Edit device security settings (level 2)", "Edit device security settings (level 3)", "Edit device security settings (level 4)");
    }
    @Test
    public void testGetExecutionLevels() throws Exception {
        DeviceType deviceType = mock(DeviceType.class);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(456L);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(deviceConfiguration));
        when(deviceConfigurationService.findDeviceType(123L)).thenReturn(Optional.of(deviceType));
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(securityPropertySet.getId()).thenReturn(999L);
        when(deviceConfiguration.getSecurityPropertySets()).thenReturn(Arrays.asList(securityPropertySet));
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
        assertThat(jsonModel.<List>get("$.executionLevels[*].name")).containsExactly("View device security settings (level 1)", "View device security settings (level 2)");
        assertThat(jsonModel.<List>get("$.executionLevels[0].userRoles")).hasSize(3);
        assertThat(jsonModel.<List<String>>get("$.executionLevels[0].userRoles[*].name")).isSortedAccordingTo((n1,n2)->n1.compareToIgnoreCase(n2));
    }

    private Group mockUserGroup(long id, String name) {
        Group mock = mock(Group.class);
        when(mock.hasPrivilege(Matchers.matches("MDC"), Matchers.<String>anyObject())).thenReturn(true);
        when(mock.getName()).thenReturn(name);
        when(mock.getId()).thenReturn(id);
        return mock;
    }


}
