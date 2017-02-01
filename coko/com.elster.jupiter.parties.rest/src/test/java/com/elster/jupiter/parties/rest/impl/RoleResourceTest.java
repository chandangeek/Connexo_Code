/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.PartyRole;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RoleResourceTest extends PartiesApplicationJerseyTest {
    public static final long OK_VERSION = 7L;
    public static final long BAD_VERSION = 5L;
    public static final String ROLE_MRID = "mrid";

    private PartyRole mockPartyRole() {
        PartyRole role = mock(PartyRole.class);
        when(role.getComponentName()).thenReturn("PTR");
        when(role.getMRID()).thenReturn(ROLE_MRID);
        when(role.getName()).thenReturn("Role");
        when(role.getAliasName()).thenReturn("Alias");
        when(role.getDescription()).thenReturn("Description");
        when(role.getVersion()).thenReturn(OK_VERSION);
        when(partyService.findPartyRoleByMRID(ROLE_MRID)).thenReturn(Optional.of(role));
        when(partyService.findAndLockRoleByMridAndVersion(ROLE_MRID, OK_VERSION)).thenReturn(Optional.of(role));
        when(partyService.findAndLockRoleByMridAndVersion(ROLE_MRID, BAD_VERSION)).thenReturn(Optional.empty());
        return role;
    }

    @Test
    public void testGetRoleById(){
        mockPartyRole();
        String answer = target("/roles/" + ROLE_MRID).request().get(String.class);
        JsonModel model = JsonModel.create(answer);
        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.roles")).hasSize(1);
        assertThat(model.<String>get("$.roles[0].componentName")).isEqualTo("PTR");
        assertThat(model.<String>get("$.roles[0].mRID")).isEqualTo(ROLE_MRID);
        assertThat(model.<String>get("$.roles[0].name")).isEqualTo("Role");
        assertThat(model.<String>get("$.roles[0].aliasName")).isEqualTo("Alias");
        assertThat(model.<String>get("$.roles[0].description")).isEqualTo("Description");
        assertThat(model.<Number>get("$.roles[0].version")).isEqualTo(((Number)OK_VERSION).intValue());
    }

    @Test
    public void testDeleteRole(){
        PartyRole partyRole = mockPartyRole();
        PartyRoleInfo info = new PartyRoleInfo();
        info.mRID = ROLE_MRID;
        info.version = OK_VERSION;

        Response response = target("/roles/" + ROLE_MRID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(partyService, times(1)).deletePartyRole(partyRole);
    }

    @Test
    public void testDeleteRoleBadVersion(){
        PartyRole partyRole = mockPartyRole();
        PartyRoleInfo info = new PartyRoleInfo();
        info.mRID = ROLE_MRID;
        info.version = BAD_VERSION;

        Response response = target("/roles/" + ROLE_MRID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(partyService, never()).deletePartyRole(partyRole);
    }

    @Test
    public void testUpdateRole(){
        PartyRole partyRole = mockPartyRole();
        PartyRoleInfo info = new PartyRoleInfo();
        info.mRID = ROLE_MRID;
        info.name = "new name";
        info.version = OK_VERSION;

        Response response = target("/roles/" + ROLE_MRID).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(partyRole, times(1)).setName(info.name);
        verify(partyService, times(1)).updateRole(partyRole);
    }

    @Test
    public void testUpdateRoleBadVersion(){
        PartyRole partyRole = mockPartyRole();
        PartyRoleInfo info = new PartyRoleInfo();
        info.mRID = ROLE_MRID;
        info.name = "new name";
        info.version = BAD_VERSION;

        Response response = target("/roles/" + ROLE_MRID).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(partyService, never()).updateRole(partyRole);
    }
}
