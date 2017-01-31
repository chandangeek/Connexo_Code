/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.users.User;
import com.energyict.mdc.dashboard.rest.status.impl.FavoriteDeviceGroupInfo.SelectionInfo;
import com.energyict.mdc.favorites.FavoriteDeviceGroup;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FavoriteDeviceGroupResourceTest extends DashboardApplicationJerseyTest {

    private static final long OK_VERSION = 47L;
    private static final long BAD_VERSION = 36L;

    @Test
    public void testGetFavoriteDeviceGroups() {
        List<FavoriteDeviceGroup> groups = new ArrayList<>();
        when(favoritesService.getFavoriteDeviceGroups(null)).thenReturn(groups);
        groups.add(mockFavoriteDeviceGroup(mockEndDeviceGroup(1L, "MDC: 1", "End device group 1", true)));
        groups.add(mockFavoriteDeviceGroup(mockEndDeviceGroup(2L, "MDC: 2", "End device group 2", false)));
        groups.add(mockFavoriteDeviceGroup(mockEndDeviceGroup(3L, "MDC: 3", "End device group 3", true)));
        
        String response = target("/favoritedevicegroups").request().get(String.class);
        
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(3);
        assertThat(model.<List<Object>>get("$.favoriteDeviceGroups")).hasSize(3);
        assertThat(model.<List<Integer>>get("$.favoriteDeviceGroups[*].id")).containsExactly(1, 2, 3);
        assertThat(model.<List<String>>get("$.favoriteDeviceGroups[*].mRID")).containsExactly("MDC: 1", "MDC: 2", "MDC: 3");
        assertThat(model.<List<String>>get("$.favoriteDeviceGroups[*].name")).containsExactly("End device group 1", "End device group 2", "End device group 3");
        assertThat(model.<List<Boolean>>get("$.favoriteDeviceGroups[*].dynamic")).containsExactly(true, false, true);
        assertThat(model.<List<Boolean>>get("$.favoriteDeviceGroups[*].favorite")).containsExactly(true, true, true);
    }
    
    @Test
    public void testGetFavoriteDeviceGroupsIncludeAllGroups() {
        List<FavoriteDeviceGroup> groups = new ArrayList<>();
        when(favoritesService.getFavoriteDeviceGroups(null)).thenReturn(groups);
        EndDeviceGroup deviceGroup = mockEndDeviceGroup(1L, "MDC: 1", "End device group 1", true);
        groups.add(mockFavoriteDeviceGroup(deviceGroup));
        
        List<EndDeviceGroup> allGroups = new ArrayList<>();
        when(meteringGroupsService.findEndDeviceGroups()).thenReturn(allGroups);
        allGroups.add(deviceGroup);
        allGroups.add(mockEndDeviceGroup(2L, "MDC: 2", "End device group 2", false));
        allGroups.add(mockEndDeviceGroup(3L, "MDC: 3", "End device group 3", true));
        
        String response = target("/favoritedevicegroups").queryParam("includeAllGroups", true).request().get(String.class);
        
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(3);
        assertThat(model.<List<Object>>get("$.favoriteDeviceGroups")).hasSize(3);
        assertThat(model.<List<Integer>>get("$.favoriteDeviceGroups[*].id")).containsExactly(1, 2, 3);
        assertThat(model.<List<String>>get("$.favoriteDeviceGroups[*].mRID")).containsExactly("MDC: 1", "MDC: 2", "MDC: 3");
        assertThat(model.<List<String>>get("$.favoriteDeviceGroups[*].name")).containsExactly("End device group 1", "End device group 2", "End device group 3");
        assertThat(model.<List<Boolean>>get("$.favoriteDeviceGroups[*].dynamic")).containsExactly(true, false, true);
        assertThat(model.<List<Boolean>>get("$.favoriteDeviceGroups[*].favorite")).containsExactly(true, false, false);
        int version = ((Number) OK_VERSION).intValue();
        assertThat(model.<List<Number>>get("$.favoriteDeviceGroups[*].parent.version")).containsExactly(version, version, version);
    }
    
    @Test
    public void testUpdateFavoriteDeviceGroups() {
        EndDeviceGroup groupToBeAdded = mockEndDeviceGroup(1L, "MDC: 1", "End device group 1", true);
        EndDeviceGroup favGroupToBeUnchanged = mockEndDeviceGroup(2L, "MDC: 2", "End device group 2", true);
        EndDeviceGroup favGroupToBeRemoved = mockEndDeviceGroup(3L, "MDC: 3", "End device group 3", true);
        EndDeviceGroup groupToBeUnchanged = mockEndDeviceGroup(3L, "MDC: 4", "End device group 4", true);

        FavoriteDeviceGroup wrappedFavGroupToBeUnchanged = mockFavoriteDeviceGroup(favGroupToBeUnchanged);
        FavoriteDeviceGroup wrappedFavGroupToBeRemoved = mockFavoriteDeviceGroup(favGroupToBeRemoved);
        List<EndDeviceGroup> currentFavGroupList = Arrays.asList(favGroupToBeUnchanged, favGroupToBeRemoved);

        SelectionInfo selectionInfo = new SelectionInfo();
        selectionInfo.ids = Arrays.asList(
                FavoriteDeviceGroupInfo.asInfo(groupToBeAdded, currentFavGroupList),
                FavoriteDeviceGroupInfo.asInfo(favGroupToBeUnchanged, currentFavGroupList),
                FavoriteDeviceGroupInfo.asInfo(favGroupToBeRemoved, currentFavGroupList),
                FavoriteDeviceGroupInfo.asInfo(groupToBeUnchanged, currentFavGroupList)
        );
        selectionInfo.ids.get(0).favorite = true;
        selectionInfo.ids.get(2).favorite = false;
        when(favoritesService.getFavoriteDeviceGroups(any(User.class))).thenReturn(Arrays.asList(wrappedFavGroupToBeUnchanged, wrappedFavGroupToBeRemoved));

        Response response = target("/favoritedevicegroups").request().put(Entity.entity(selectionInfo, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        
        verify(favoritesService).findOrCreateFavoriteDeviceGroup(eq(groupToBeAdded), any(User.class));
        verify(favoritesService, never()).findOrCreateFavoriteDeviceGroup(eq(groupToBeUnchanged), any(User.class));
        verify(favoritesService).removeFavoriteDeviceGroup(wrappedFavGroupToBeRemoved);
        verify(favoritesService, never()).removeFavoriteDeviceGroup(wrappedFavGroupToBeUnchanged);
    }

    @Test
    public void testUpdateFavoriteDeviceGroupsButSomebodyAlreadyAddedGroup() {
        EndDeviceGroup groupToBeAdded = mockEndDeviceGroup(1L, "MDC: 1", "End device group 1", true);
        EndDeviceGroup favGroupToBeUnchanged = mockEndDeviceGroup(2L, "MDC: 2", "End device group 2", true);
        EndDeviceGroup favGroupToBeRemoved = mockEndDeviceGroup(3L, "MDC: 3", "End device group 3", true);
        EndDeviceGroup groupToBeUnchanged = mockEndDeviceGroup(4L, "MDC: 4", "End device group 4", true);

        FavoriteDeviceGroup wrappedFavGroupToBeUnchanged = mockFavoriteDeviceGroup(favGroupToBeUnchanged);
        FavoriteDeviceGroup wrappedFavGroupToBeRemoved = mockFavoriteDeviceGroup(favGroupToBeRemoved);
        FavoriteDeviceGroup wrappedGroupToBeUnchanged = mockFavoriteDeviceGroup(groupToBeUnchanged);
        List<EndDeviceGroup> currentFavGroupList = Arrays.asList(favGroupToBeUnchanged, favGroupToBeRemoved);

        SelectionInfo selectionInfo = new SelectionInfo();
        selectionInfo.ids = Arrays.asList(
                FavoriteDeviceGroupInfo.asInfo(groupToBeAdded, currentFavGroupList),
                FavoriteDeviceGroupInfo.asInfo(favGroupToBeUnchanged, currentFavGroupList),
                FavoriteDeviceGroupInfo.asInfo(favGroupToBeRemoved, currentFavGroupList),
                FavoriteDeviceGroupInfo.asInfo(groupToBeUnchanged, currentFavGroupList)
        );
        selectionInfo.ids.get(0).favorite = true;
        selectionInfo.ids.get(2).favorite = false;
        when(favoritesService.getFavoriteDeviceGroups(any(User.class))).thenReturn(Arrays.asList(wrappedFavGroupToBeUnchanged, wrappedFavGroupToBeRemoved, wrappedGroupToBeUnchanged));

        Response response = target("/favoritedevicegroups").request().put(Entity.entity(selectionInfo, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());

        verify(favoritesService).findOrCreateFavoriteDeviceGroup(eq(groupToBeAdded), any(User.class));
        verify(favoritesService, never()).findOrCreateFavoriteDeviceGroup(eq(groupToBeUnchanged), any(User.class));
        verify(favoritesService).removeFavoriteDeviceGroup(wrappedFavGroupToBeRemoved);
        verify(favoritesService, never()).removeFavoriteDeviceGroup(wrappedFavGroupToBeUnchanged);
    }

    @Test
    public void testUpdateFavoriteDeviceGroupsButSomebodyAlreadyRemovedGroup() {
        EndDeviceGroup groupToBeAdded = mockEndDeviceGroup(1L, "MDC: 1", "End device group 1", true);
        EndDeviceGroup favGroupToBeUnchanged = mockEndDeviceGroup(2L, "MDC: 2", "End device group 2", true);
        EndDeviceGroup favGroupToBeRemoved = mockEndDeviceGroup(3L, "MDC: 3", "End device group 3", true);
        EndDeviceGroup groupToBeUnchanged = mockEndDeviceGroup(4L, "MDC: 4", "End device group 4", true);

        FavoriteDeviceGroup wrappedFavGroupToBeUnchanged = mockFavoriteDeviceGroup(favGroupToBeUnchanged);
        FavoriteDeviceGroup wrappedFavGroupToBeRemoved = mockFavoriteDeviceGroup(favGroupToBeRemoved);
        List<EndDeviceGroup> currentFavGroupList = Arrays.asList(favGroupToBeUnchanged, favGroupToBeRemoved);

        SelectionInfo selectionInfo = new SelectionInfo();
        selectionInfo.ids = Arrays.asList(
                FavoriteDeviceGroupInfo.asInfo(groupToBeAdded, currentFavGroupList),
                FavoriteDeviceGroupInfo.asInfo(favGroupToBeUnchanged, currentFavGroupList),
                FavoriteDeviceGroupInfo.asInfo(favGroupToBeRemoved, currentFavGroupList),
                FavoriteDeviceGroupInfo.asInfo(groupToBeUnchanged, currentFavGroupList)
        );
        selectionInfo.ids.get(0).favorite = true;
        selectionInfo.ids.get(2).favorite = false;
        when(favoritesService.getFavoriteDeviceGroups(any(User.class))).thenReturn(Arrays.asList(wrappedFavGroupToBeRemoved));

        Response response = target("/favoritedevicegroups").request().put(Entity.entity(selectionInfo, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());

        verify(favoritesService).findOrCreateFavoriteDeviceGroup(eq(groupToBeAdded), any(User.class));
        verify(favoritesService, never()).findOrCreateFavoriteDeviceGroup(eq(groupToBeUnchanged), any(User.class));
        verify(favoritesService, never()).removeFavoriteDeviceGroup(wrappedFavGroupToBeRemoved);
        verify(favoritesService, never()).removeFavoriteDeviceGroup(wrappedFavGroupToBeUnchanged);
    }


    @Test
    public void testUpdateFavoriteDeviceGroupsButGroupWasChanged() {
        EndDeviceGroup groupToBeAdded = mockEndDeviceGroup(1L, "MDC: 1", "End device group 1", true);
        EndDeviceGroup favGroupToBeUnchanged = mockEndDeviceGroup(2L, "MDC: 2", "End device group 2", true);
        EndDeviceGroup favGroupToBeRemoved = mockEndDeviceGroup(3L, "MDC: 3", "End device group 3", true);
        EndDeviceGroup groupToBeUnchanged = mockEndDeviceGroup(4L, "MDC: 4", "End device group 4", true);

        FavoriteDeviceGroup wrappedFavGroupToBeUnchanged = mockFavoriteDeviceGroup(favGroupToBeUnchanged);
        FavoriteDeviceGroup wrappedFavGroupToBeRemoved = mockFavoriteDeviceGroup(favGroupToBeRemoved);
        List<EndDeviceGroup> currentFavGroupList = Arrays.asList(favGroupToBeUnchanged, favGroupToBeRemoved);

        SelectionInfo selectionInfo = new SelectionInfo();
        selectionInfo.ids = Arrays.asList(
                FavoriteDeviceGroupInfo.asInfo(groupToBeAdded, currentFavGroupList),
                FavoriteDeviceGroupInfo.asInfo(favGroupToBeUnchanged, currentFavGroupList),
                FavoriteDeviceGroupInfo.asInfo(favGroupToBeRemoved, currentFavGroupList),
                FavoriteDeviceGroupInfo.asInfo(groupToBeUnchanged, currentFavGroupList)
        );
        selectionInfo.ids.get(0).parent.version = BAD_VERSION;
        selectionInfo.ids.get(0).favorite = true;
        selectionInfo.ids.get(2).favorite = false;
        when(favoritesService.getFavoriteDeviceGroups(any(User.class))).thenReturn(Arrays.asList(wrappedFavGroupToBeRemoved, wrappedFavGroupToBeUnchanged));

        Response response = target("/favoritedevicegroups").request().put(Entity.entity(selectionInfo, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());

        verify(favoritesService, never()).findOrCreateFavoriteDeviceGroup(eq(groupToBeAdded), any(User.class));
        verify(favoritesService, never()).findOrCreateFavoriteDeviceGroup(eq(groupToBeUnchanged), any(User.class));
        verify(favoritesService, never()).removeFavoriteDeviceGroup(wrappedFavGroupToBeRemoved);
        verify(favoritesService, never()).removeFavoriteDeviceGroup(wrappedFavGroupToBeUnchanged);
    }

    private FavoriteDeviceGroup mockFavoriteDeviceGroup(EndDeviceGroup endDeviceGroup) {
        FavoriteDeviceGroup favoriteDeviceGroup = mock(FavoriteDeviceGroup.class);
        when(favoriteDeviceGroup.getEndDeviceGroup()).thenReturn(endDeviceGroup);
        return favoriteDeviceGroup;
    }
    
    private EndDeviceGroup mockEndDeviceGroup(long id, String mRID, String name, boolean isDynamic) {
        EndDeviceGroup endDeviceGroup = mock(EndDeviceGroup.class);
        when(endDeviceGroup.getId()).thenReturn(id);
        when(endDeviceGroup.getMRID()).thenReturn(mRID);
        when(endDeviceGroup.getName()).thenReturn(name);
        when(endDeviceGroup.isDynamic()).thenReturn(isDynamic);
        when(endDeviceGroup.getVersion()).thenReturn(OK_VERSION);

        doReturn(Optional.of(endDeviceGroup)).when(meteringGroupsService).findEndDeviceGroup(id);
        doReturn(Optional.of(endDeviceGroup)).when(meteringGroupsService).findAndLockEndDeviceGroupByIdAndVersion(id, OK_VERSION);
        doReturn(Optional.empty()).when(meteringGroupsService).findAndLockEndDeviceGroupByIdAndVersion(id, BAD_VERSION);
        return endDeviceGroup;
    }
    
}
