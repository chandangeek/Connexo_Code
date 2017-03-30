/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryParameters;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RegisterGroupResourceTest extends DeviceConfigurationApplicationJerseyTest {
    public static final long REGISTER_GROUP_ID = 78L;

    public static final long OK_VERSION = 24L;
    public static final long BAD_VERSION = 17L;

    @Test
    public void testGetRegisterGroups() {

        RegisterGroup registerGroup1 = mockRegisterGroup(REGISTER_GROUP_ID);
        RegisterGroup registerGroup2 = mockRegisterGroup(REGISTER_GROUP_ID + 1);

        Finder finder = mock(Finder.class);
        when(finder.find()).thenReturn(Arrays.asList(registerGroup1, registerGroup2));
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(masterDataService.findAllRegisterGroups()).thenReturn(finder);
        String response = target("/registergroups").request().get(String.class);
        JsonModel model = JsonModel.create(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(2);
        assertThat(model.<List>get("$.registerGroups")).isNotEmpty();
        assertThat(model.<Number>get("$.registerGroups[0].id")).isEqualTo(((Number)REGISTER_GROUP_ID).intValue());
        assertThat(model.<String>get("$.registerGroups[0].name")).isNotEmpty();
        assertThat(model.<Number>get("$.registerGroups[0].version")).isEqualTo(((Number) OK_VERSION).intValue());
    }

    @Test
    public void testDeleteRegisterGroupOkVersion() {
        RegisterGroup registerGroup = mockRegisterGroup(REGISTER_GROUP_ID);
        RegisterGroupInfo info = registerGroupInfoFactory.asInfo(registerGroup);
        Response response = target("/registergroups/" + REGISTER_GROUP_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(registerGroup).delete();
    }

    @Test
    public void testDeleteRegisterGroupBadVersion() {
        RegisterGroup registerGroup = mockRegisterGroup(REGISTER_GROUP_ID);
        RegisterGroupInfo info = registerGroupInfoFactory.asInfo(registerGroup);
        info.version = BAD_VERSION;
        Response response = target("/registergroups/" + REGISTER_GROUP_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(registerGroup, never()).delete();
    }

    @Test
    public void testUpdateRegisterGroupOkVersion() {
        RegisterGroup registerGroup = mockRegisterGroup(REGISTER_GROUP_ID);
        RegisterGroupInfo info = registerGroupInfoFactory.asInfo(registerGroup);
        info.name = "new name";
        Response response = target("/registergroups/" + REGISTER_GROUP_ID).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(registerGroup).setName("new name");
    }

    @Test
    public void testUpdateRegisterGroupBadVersion() {
        RegisterGroup registerGroup = mockRegisterGroup(REGISTER_GROUP_ID);
        RegisterGroupInfo info = registerGroupInfoFactory.asInfo(registerGroup);
        info.name = "new name";
        info.version = BAD_VERSION;
        Response response = target("/registergroups/" + REGISTER_GROUP_ID).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(registerGroup, never()).setName("new name");
    }

    private RegisterGroup mockRegisterGroup(long id) {
        RegisterGroup registerGroup = mock(RegisterGroup.class);
        when(registerGroup.getName()).thenReturn("Register group " + id);
        when(registerGroup.getId()).thenReturn(id);
        List<RegisterType> registerTypes = new ArrayList<>();
        when(registerGroup.getRegisterTypes()).thenReturn(registerTypes);
        when(registerGroup.getVersion()).thenReturn(OK_VERSION);
        when(masterDataService.findRegisterGroup(id)).thenReturn(Optional.of(registerGroup));
        when(masterDataService.findAndLockRegisterGroupByIdAndVersion(id, OK_VERSION)).thenReturn(Optional.of(registerGroup));
        when(masterDataService.findAndLockRegisterGroupByIdAndVersion(id, BAD_VERSION)).thenReturn(Optional.empty());
        return registerGroup;
    }
}
