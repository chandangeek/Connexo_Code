/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.impl;


import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.users.rest.WorkGroupInfo;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;

import static javax.ws.rs.HttpMethod.DELETE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WorkGroupResourceTest extends UsersRestApplicationJerseyTest {

    private final static String WORK_GROUP_DESCRIPTION = "Work group description";
    private final static String WORK_GROUP_NAME = "WorkGroup Name";
    private final static long WORK_GROUP_ID = 1L;
    private final static long WORK_GROUP_VERSION = 1L;

    private WorkGroup mockWorkGroup(){
        WorkGroup workGroup = mock(WorkGroup.class);
        when(workGroup.getId()).thenReturn(WORK_GROUP_ID);
        when(workGroup.getDescription()).thenReturn(WORK_GROUP_DESCRIPTION);
        when(workGroup.getName()).thenReturn(WORK_GROUP_NAME);
        when(workGroup.getVersion()).thenReturn(WORK_GROUP_VERSION);
        return workGroup;
    }

    @Test
    public void testDeleteWorkGroup() {
        WorkGroup workGroup = mockWorkGroup();
        when(userService.findAndLockWorkGroupByIdAndVersion(1L, 1L)).thenReturn(Optional.of(workGroup));
        when(userService.getWorkGroup(1L)).thenReturn(Optional.of(workGroup));

        WorkGroupInfo info = new WorkGroupInfo();
        info.id = 1L;
        info.version = 1L;
        Entity<WorkGroupInfo> json = Entity.json(info);

        Response response = target("/workgroups/1").request().build(DELETE, json).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(workGroup, times(1)).delete();
    }

    @Test
    public void testUpdateWorkGroupRevokeUsers() {
        WorkGroup workGroup = mockWorkGroup();
        User user = mock(User.class);
        when(workGroup.getUsersInWorkGroup()).thenReturn(Collections.singletonList(user));
        when(userService.findAndLockWorkGroupByIdAndVersion(1L, 1L)).thenReturn(Optional.of(workGroup));
        when(userService.getWorkGroup(1L)).thenReturn(Optional.of(workGroup));
        WorkGroupInfo info = new WorkGroupInfo();
        info.id = 1L;
        info.users = Collections.emptyList();
        info.version = 1L;
        Entity<WorkGroupInfo> json = Entity.json(info);

        Response response = target("/workgroups/1").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(workGroup, times(1)).revoke(user);
    }

    @Test
    public void testUpdateWorkGroupConcurrentModification() {
        when(userService.findAndLockWorkGroupByIdAndVersion(1L, 1L)).thenReturn(Optional.empty());
        when(userService.getWorkGroup(1L)).thenReturn(Optional.empty());
        WorkGroupInfo info = new WorkGroupInfo();
        info.id = 1L;
        info.version = 1L;
        Entity<WorkGroupInfo> json = Entity.json(info);

        Response response = target("/workgroups/1").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testGetWorkGroup() {
        WorkGroup workGroup = mockWorkGroup();
        User user = mock(User.class);
        when(workGroup.getUsersInWorkGroup()).thenReturn(Collections.singletonList(user));
        when(userService.findAndLockWorkGroupByIdAndVersion(1L, 1L)).thenReturn(Optional.of(workGroup));
        when(userService.getWorkGroup(1L)).thenReturn(Optional.of(workGroup));
        Response response = target("/workgroups/1").request().get();
        WorkGroupInfo info = response.readEntity(WorkGroupInfo.class);
        assertThat(info.name).isEqualTo(WORK_GROUP_NAME);
        assertThat(info.description).isEqualTo(WORK_GROUP_DESCRIPTION);
        assertThat(info.users.size()).isEqualTo(1);
    }
}
