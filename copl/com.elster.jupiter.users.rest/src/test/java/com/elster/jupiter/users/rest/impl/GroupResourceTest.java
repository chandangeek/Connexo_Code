/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.PrivilegeCategory;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.GroupInfo;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GroupResourceTest extends UsersRestApplicationJerseyTest {

    private static final Instant NOW = Instant.now();

    private Group mockGroup() {
        Group group = mock(Group.class);
        when(group.getId()).thenReturn(1L);
        when(group.getName()).thenReturn("TestGroup");
        when(group.getVersion()).thenReturn(1L);
        when(group.getDescription()).thenReturn("Test group description");
        when(group.getCreationDate()).thenReturn(NOW);
        when(group.getModifiedDate()).thenReturn(NOW);
        return group;
    }

    @Test
    public void testUpdateGroupRevokeAllPrivileges() {
        Group group = mockGroup();
        Privilege privilege = mock(Privilege.class);
        PrivilegeCategory category = mock(PrivilegeCategory.class);
        when(privilege.getCategory()).thenReturn(category);
        when(category.getName()).thenReturn(UserService.DEFAULT_CATEGORY_NAME);
        when(group.getPrivileges()).thenReturn(Collections.singletonMap("test", Collections.singletonList(privilege)));
        when(userService.findAndLockGroupByIdAndVersion(1L, 1L)).thenReturn(Optional.of(group));
        when(userService.getGroup(1L)).thenReturn(Optional.of(group));
        GroupInfo info = new GroupInfo();
        info.id = 1L;
        info.privileges = Collections.emptyList();
        info.version = 1L;
        Entity<GroupInfo> json = Entity.json(info);

        Response response = target("/groups/1").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(group, times(1)).revoke("test", privilege);
    }

    @Test
    public void testUpdateGroupConcurrentModification() {
        when(userService.findAndLockGroupByIdAndVersion(1L, 1L)).thenReturn(Optional.empty());
        when(userService.getGroup(1L)).thenReturn(Optional.empty());
        GroupInfo info = new GroupInfo();
        info.id = 1L;
        info.version = 1L;
        Entity<GroupInfo> json = Entity.json(info);

        Response response = target("/groups/1").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testDeleteGroup() {
        Group group = mockGroup();
        when(userService.findAndLockGroupByIdAndVersion(1L, 1L)).thenReturn(Optional.of(group));
        when(userService.getGroup(1L)).thenReturn(Optional.of(group));

        GroupInfo info = new GroupInfo();
        info.id = 1L;
        info.version = 1L;
        Entity<GroupInfo> json = Entity.json(info);

        Response response = target("/groups/1").request().build(HttpMethod.DELETE, json).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(group, times(1)).delete();
    }

    @Test
    public void testDeleteGroupConcurrentModification() {
        when(userService.findAndLockGroupByIdAndVersion(1L, 1L)).thenReturn(Optional.empty());
        when(userService.getGroup(1L)).thenReturn(Optional.empty());
        GroupInfo info = new GroupInfo();
        info.id = 1L;
        info.version = 1L;
        Entity<GroupInfo> json = Entity.json(info);

        Response response = target("/groups/1").request().build(HttpMethod.DELETE, json).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
}
