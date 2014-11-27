package com.energyict.mdc.dashboard.rest.status.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Test;

import com.elster.jupiter.favorites.FavoriteDeviceGroup;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.dashboard.rest.status.impl.FavoriteDeviceGroupInfo.SelectionInfo;
import com.jayway.jsonpath.JsonModel;

public class FavoriteDeviceGroupResourceTest extends DashboardApplicationJerseyTest {

    @Test
    public void testGetFavoriteDeviceGroups() {
        List<FavoriteDeviceGroup> groups = new ArrayList<>();
        when(favoritesService.getFavoriteDeviceGroups(null)).thenReturn(groups);
        groups.add(mockFavoriteDeviceGroup(mockEndDeviceGroup(1L, "MDC: 1", "End device group 1", true)));
        groups.add(mockFavoriteDeviceGroup(mockEndDeviceGroup(2L, "MDC: 2", "End device group 2", false)));
        groups.add(mockFavoriteDeviceGroup(mockEndDeviceGroup(3L, "MDC: 3", "End device group 3", true)));
        
        String response = target("/favouritedevicegroups").request().get(String.class);
        
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
        
        String response = target("/favouritedevicegroups").queryParam("includeAllGroups", true).request().get(String.class);
        
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(3);
        assertThat(model.<List<Object>>get("$.favoriteDeviceGroups")).hasSize(3);
        assertThat(model.<List<Integer>>get("$.favoriteDeviceGroups[*].id")).containsExactly(1, 2, 3);
        assertThat(model.<List<String>>get("$.favoriteDeviceGroups[*].mRID")).containsExactly("MDC: 1", "MDC: 2", "MDC: 3");
        assertThat(model.<List<String>>get("$.favoriteDeviceGroups[*].name")).containsExactly("End device group 1", "End device group 2", "End device group 3");
        assertThat(model.<List<Boolean>>get("$.favoriteDeviceGroups[*].dynamic")).containsExactly(true, false, true);
        assertThat(model.<List<Boolean>>get("$.favoriteDeviceGroups[*].favorite")).containsExactly(true, false, false);
    }
    
    @Test
    public void testUpdateFavoriteDeviceGroups() {
        SelectionInfo selectionInfo = new SelectionInfo();
        selectionInfo.ids = Arrays.asList(1L, 3L);
        EndDeviceGroup endDeviceGroup = mockEndDeviceGroup(1L, "MDC: 1", "End device group 1", true);
        EndDeviceGroup endDeviceGroup2 = mockEndDeviceGroup(2L, "MDC: 2", "End device group 2", true);
        EndDeviceGroup endDeviceGroup3 = mockEndDeviceGroup(3L, "MDC: 3", "End device group 3", true);
        FavoriteDeviceGroup favoriteDeviceGroup2 = mockFavoriteDeviceGroup(endDeviceGroup2);
        FavoriteDeviceGroup favoriteDeviceGroup3 = mockFavoriteDeviceGroup(endDeviceGroup3);
        when(favoritesService.getFavoriteDeviceGroups(null)).thenReturn(Arrays.asList(favoriteDeviceGroup2, favoriteDeviceGroup3));
        when(meteringGroupsService.findEndDeviceGroup(1L)).thenReturn(Optional.of(endDeviceGroup));
        
        Response response = target("/favouritedevicegroups").request().put(Entity.entity(selectionInfo, MediaType.APPLICATION_JSON));
        
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        
        verify(favoritesService).findOrCreateFavoriteDeviceGroup(endDeviceGroup, null);
        verify(favoritesService).removeFavoriteDeviceGroup(favoriteDeviceGroup2);
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
        return endDeviceGroup;
    }
    
}
