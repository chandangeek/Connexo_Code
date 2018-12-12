/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest;


import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import static com.energyict.mdc.device.alarms.rest.i18n.DeviceAlarmTranslationKeys.ALARM_ASSIGNEE_UNASSIGNED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WorkGroupResourceTest extends DeviceAlarmApplicationTest {

    @Test
    public void testGetWorkGroups(){
        WorkGroup workGroupOne = mock(WorkGroup.class);
        WorkGroup workGroupTwo = mock(WorkGroup.class);
        when(workGroupOne.getId()).thenReturn(1L);
        when(workGroupOne.getName()).thenReturn("WorkGroup one");
        when(workGroupTwo.getId()).thenReturn(2L);
        when(workGroupTwo.getName()).thenReturn("WorkGroup two");
        List<WorkGroup> workGroupList = new ArrayList<>();
        workGroupList.add(workGroupOne);
        workGroupList.add(workGroupTwo);

        mockTranslation(ALARM_ASSIGNEE_UNASSIGNED);

        when(userService.getWorkGroups()).thenReturn(workGroupList);
        Map<String, Object> map = target("/workgroups").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(3);
    }

    @Test
    public void testGetMyWorkGroups(){
        User user = mockUser(13L, "admin");
        WorkGroup workGroupOne = mock(WorkGroup.class);
        when(workGroupOne.getId()).thenReturn(1L);
        when(workGroupOne.getName()).thenReturn("WorkGroup one");
        when(workGroupOne.getUsersInWorkGroup()).thenReturn(Collections.singletonList(user));
        when(securityContext.getUserPrincipal()).thenReturn(user);
        mockTranslation(ALARM_ASSIGNEE_UNASSIGNED);
        when(userService.getWorkGroups()).thenReturn(Collections.singletonList(workGroupOne));
        Map<String, Object> map = target("/workgroups").request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(2);
    }

    @Test
    public void testGetUsersFromWorkGroups(){
        User user = mockUser(13L, "admin");
        WorkGroup workGroupOne = mock(WorkGroup.class);
        when(workGroupOne.getId()).thenReturn(1L);
        when(workGroupOne.getName()).thenReturn("WorkGroup one");
        when(workGroupOne.getUsersInWorkGroup()).thenReturn(Collections.singletonList(user));
        when(securityContext.getUserPrincipal()).thenReturn(user);
        mockTranslation(ALARM_ASSIGNEE_UNASSIGNED);
        when(userService.getWorkGroup(1L)).thenReturn(Optional.of(workGroupOne));
        Map<String, Object> map = target("workgroups/1/users").request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(2);
    }

}
