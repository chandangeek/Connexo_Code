/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoriteUsagePoint;
import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoriteUsagePointGroup;
import com.elster.jupiter.mdm.usagepoint.data.rest.impl.favorites.FavoriteUsagePointGroupDetailsInfo;
import com.elster.jupiter.mdm.usagepoint.data.rest.impl.favorites.FavoriteUsagePointGroupInfo;
import com.elster.jupiter.mdm.usagepoint.data.rest.impl.favorites.FavoriteUsagePointInfo;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.rest.util.ConcurrentModificationInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class FavoritesResourceTest extends UsagePointDataRestApplicationJerseyTest {

    @Mock
    private UsagePoint usagePoint;
    @Mock
    private UsagePointGroup usagePointGroup;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(usagePoint.getId()).thenReturn(100L);
        when(usagePoint.getName()).thenReturn("name");
        when(usagePoint.getVersion()).thenReturn(1L);
        when(meteringService.findUsagePointById(usagePoint.getId())).thenReturn(Optional.of(usagePoint));
        when(meteringService.findUsagePointByName(usagePoint.getName())).thenReturn(Optional.of(usagePoint));
        when(meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), usagePoint.getVersion()))
                .thenReturn(Optional.of(usagePoint));
        when(usagePointGroup.getId()).thenReturn(100L);
        when(usagePointGroup.getName()).thenReturn("name");
        when(usagePointGroup.getVersion()).thenReturn(1L);
        when(meteringGroupsService.findUsagePointGroup(usagePointGroup.getId())).thenReturn(Optional.of(usagePointGroup));
        when(meteringGroupsService.findAndLockUsagePointGroupByIdAndVersion(usagePointGroup.getId(), usagePointGroup.getVersion()))
                .thenReturn(Optional.of(usagePointGroup));
    }

    @Test
    public void testUPGIsNotFavorite() {
        when(favoritesService.findFavoriteUsagePointGroup(usagePointGroup)).thenReturn(Optional.empty());

        String response = target("/favorites/usagepointgroups/100").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Number>get("$.parent.id")).isEqualTo(100);
        assertThat(jsonModel.<Number>get("$.parent.version")).isEqualTo(1);
        assertThat(jsonModel.<Boolean>get("$.favorite")).isFalse();
    }

    @Test
    public void testUPIsNotFavorite() {
        when(favoritesService.findFavoriteUsagePoint(usagePoint)).thenReturn(Optional.empty());

        String response = target("/favorites/usagepoints/name").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Number>get("$.parent.id")).isEqualTo(100);
        assertThat(jsonModel.<Number>get("$.parent.version")).isEqualTo(1);
        assertThat(jsonModel.<Boolean>get("$.favorite")).isFalse();
    }

    @Test
    public void testUPGIsFavorite() {
        Instant now = Instant.now();

        FavoriteUsagePointGroup favoriteUsagePointGroup = mock(FavoriteUsagePointGroup.class);
        when(favoriteUsagePointGroup.getComment()).thenReturn("Comment1");
        when(favoriteUsagePointGroup.getCreationDate()).thenReturn(now);
        when(favoriteUsagePointGroup.getUsagePointGroup()).thenReturn(usagePointGroup);
        when(favoritesService.findFavoriteUsagePointGroup(usagePointGroup))
                .thenReturn(Optional.of(favoriteUsagePointGroup));

        String response = target("/favorites/usagepointgroups/100").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Number>get("$.parent.id")).isEqualTo(100);
        assertThat(jsonModel.<Number>get("$.parent.version")).isEqualTo(1);
        assertThat(jsonModel.<Boolean>get("$.favorite")).isTrue();
        assertThat(jsonModel.<Number>get("$.creationDate")).isEqualTo(now.toEpochMilli());
        assertThat(jsonModel.<String>get("$.comment")).isEqualTo("Comment1");
    }

    @Test
    public void testUPIsFavorite() {
        Instant now = Instant.now();

        FavoriteUsagePoint favoriteUsagePoint = mock(FavoriteUsagePoint.class);
        when(favoriteUsagePoint.getComment()).thenReturn("Comment1");
        when(favoriteUsagePoint.getCreationDate()).thenReturn(now);
        when(favoriteUsagePoint.getUsagePoint()).thenReturn(usagePoint);
        when(favoritesService.findFavoriteUsagePoint(usagePoint))
                .thenReturn(Optional.of(favoriteUsagePoint));

        String response = target("/favorites/usagepoints/name").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Number>get("$.parent.id")).isEqualTo(100);
        assertThat(jsonModel.<Number>get("$.parent.version")).isEqualTo(1);
        assertThat(jsonModel.<Boolean>get("$.favorite")).isTrue();
        assertThat(jsonModel.<Number>get("$.creationDate")).isEqualTo(now.toEpochMilli());
        assertThat(jsonModel.<String>get("$.comment")).isEqualTo("Comment1");
    }

    @Test
    public void testUPGIsMadeFavorite() {
        FavoriteUsagePointGroupInfo info = new FavoriteUsagePointGroupInfo();
        info.parent = new VersionInfo<>();
        info.parent.id = usagePointGroup.getId();
        info.parent.version = usagePointGroup.getVersion();
        info.favorite = true;
        info.comment = "My comment";

        Instant now = Instant.now();
        FavoriteUsagePointGroup favoriteUsagePointGroup = mock(FavoriteUsagePointGroup.class);
        when(favoriteUsagePointGroup.getComment()).thenReturn(null, info.comment);
        when(favoriteUsagePointGroup.getCreationDate()).thenReturn(now);
        when(favoriteUsagePointGroup.getUsagePointGroup()).thenReturn(usagePointGroup);
        when(favoritesService.findFavoriteUsagePointGroup(usagePointGroup)).thenReturn(Optional.empty());
        when(favoritesService.markFavorite(usagePointGroup))
                .thenReturn(favoriteUsagePointGroup);

        Response response = target("/favorites/usagepointgroups/100").request().put(Entity.json(info));

        verify(favoritesService).markFavorite(usagePointGroup);
        verify(favoriteUsagePointGroup, times(2)).getComment();
        verify(favoriteUsagePointGroup).updateComment(info.comment);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        FavoriteUsagePointGroupInfo createdFavoriteInfo = response.readEntity(FavoriteUsagePointGroupInfo.class);
        assertThat(createdFavoriteInfo.parent.id).isEqualTo(100);
        assertThat(createdFavoriteInfo.parent.version).isEqualTo(1);
        assertThat(createdFavoriteInfo.favorite).isTrue();
        assertThat(createdFavoriteInfo.comment).isEqualTo(info.comment);
        assertThat(createdFavoriteInfo.creationDate).isEqualTo(now);
    }

    @Test
    public void testUPIsMadeFavorite() {
        FavoriteUsagePointInfo info = new FavoriteUsagePointInfo();
        info.parent = new VersionInfo<>();
        info.parent.id = usagePoint.getId();
        info.parent.version = usagePoint.getVersion();
        info.favorite = true;
        info.comment = "My comment";

        Instant now = Instant.now();
        FavoriteUsagePoint favoriteUsagePoint = mock(FavoriteUsagePoint.class);
        when(favoriteUsagePoint.getComment()).thenReturn(null, info.comment);
        when(favoriteUsagePoint.getCreationDate()).thenReturn(now);
        when(favoriteUsagePoint.getUsagePoint()).thenReturn(usagePoint);
        when(favoritesService.findFavoriteUsagePoint(usagePoint)).thenReturn(Optional.empty());
        when(favoritesService.markFavorite(usagePoint))
                .thenReturn(favoriteUsagePoint);

        Response response = target("/favorites/usagepoints/name").request().put(Entity.json(info));

        verify(favoritesService).markFavorite(usagePoint);
        verify(favoriteUsagePoint, times(2)).getComment();
        verify(favoriteUsagePoint).updateComment(info.comment);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        FavoriteUsagePointInfo createdFavoriteInfo = response.readEntity(FavoriteUsagePointInfo.class);
        assertThat(createdFavoriteInfo.parent.id).isEqualTo(100);
        assertThat(createdFavoriteInfo.parent.version).isEqualTo(1);
        assertThat(createdFavoriteInfo.favorite).isTrue();
        assertThat(createdFavoriteInfo.comment).isEqualTo(info.comment);
        assertThat(createdFavoriteInfo.creationDate).isEqualTo(now);
    }

    @Test
    public void testUPGIsUpdatedForCommentButItIsTheSame() {
        FavoriteUsagePointGroupInfo info = new FavoriteUsagePointGroupInfo();
        info.parent = new VersionInfo<>();
        info.parent.id = usagePointGroup.getId();
        info.parent.version = usagePointGroup.getVersion();
        info.favorite = true;
        info.comment = "My comment";

        Instant now = Instant.now();
        FavoriteUsagePointGroup favoriteUsagePointGroup = mock(FavoriteUsagePointGroup.class);
        when(favoriteUsagePointGroup.getComment()).thenReturn(info.comment, info.comment);
        when(favoriteUsagePointGroup.getCreationDate()).thenReturn(now);
        when(favoriteUsagePointGroup.getUsagePointGroup()).thenReturn(usagePointGroup);
        when(favoritesService.findFavoriteUsagePointGroup(usagePointGroup)).thenReturn(Optional.empty());
        when(favoritesService.markFavorite(usagePointGroup))
                .thenReturn(favoriteUsagePointGroup);

        Response response = target("/favorites/usagepointgroups/100").request().put(Entity.json(info));

        verify(favoritesService).markFavorite(usagePointGroup);
        verify(favoriteUsagePointGroup, times(2)).getComment();
        verify(favoriteUsagePointGroup, never()).updateComment(anyString());

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        FavoriteUsagePointGroupInfo createdFavoriteInfo = response.readEntity(FavoriteUsagePointGroupInfo.class);
        assertThat(createdFavoriteInfo.parent.id).isEqualTo(100);
        assertThat(createdFavoriteInfo.parent.version).isEqualTo(1);
        assertThat(createdFavoriteInfo.favorite).isTrue();
        assertThat(createdFavoriteInfo.comment).isEqualTo(info.comment);
        assertThat(createdFavoriteInfo.creationDate).isEqualTo(now);
    }

    @Test
    public void testUPIsUpdatedForCommentButItIsTheSame() {
        FavoriteUsagePointInfo info = new FavoriteUsagePointInfo();
        info.parent = new VersionInfo<>();
        info.parent.id = usagePoint.getId();
        info.parent.version = usagePoint.getVersion();
        info.favorite = true;
        info.comment = "My comment";

        Instant now = Instant.now();
        FavoriteUsagePoint favoriteUsagePoint = mock(FavoriteUsagePoint.class);
        when(favoriteUsagePoint.getComment()).thenReturn(info.comment, info.comment);
        when(favoriteUsagePoint.getCreationDate()).thenReturn(now);
        when(favoriteUsagePoint.getUsagePoint()).thenReturn(usagePoint);
        when(favoritesService.findFavoriteUsagePoint(usagePoint)).thenReturn(Optional.empty());
        when(favoritesService.markFavorite(usagePoint))
                .thenReturn(favoriteUsagePoint);

        Response response = target("/favorites/usagepoints/name").request().put(Entity.json(info));

        verify(favoritesService).markFavorite(usagePoint);
        verify(favoriteUsagePoint, times(2)).getComment();
        verify(favoriteUsagePoint, never()).updateComment(anyString());

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        FavoriteUsagePointInfo createdFavoriteInfo = response.readEntity(FavoriteUsagePointInfo.class);
        assertThat(createdFavoriteInfo.parent.id).isEqualTo(100);
        assertThat(createdFavoriteInfo.parent.version).isEqualTo(1);
        assertThat(createdFavoriteInfo.favorite).isTrue();
        assertThat(createdFavoriteInfo.comment).isEqualTo(info.comment);
        assertThat(createdFavoriteInfo.creationDate).isEqualTo(now);
    }

    @Test
    public void testUPGIsRemovedFromFavorites() {
        FavoriteUsagePointGroup favoriteUsagePointGroup = mock(FavoriteUsagePointGroup.class);
        when(favoritesService.findFavoriteUsagePointGroup(usagePointGroup)).thenReturn(Optional.of(favoriteUsagePointGroup));

        FavoriteUsagePointGroupInfo info = new FavoriteUsagePointGroupInfo();
        info.parent = new VersionInfo<>();
        info.parent.id = usagePointGroup.getId();
        info.parent.version = usagePointGroup.getVersion();
        info.favorite = false;

        Response response = target("/favorites/usagepointgroups/100").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(favoritesService).removeFromFavorites(favoriteUsagePointGroup);
    }

    @Test
    public void testUPIsRemovedFromFavorites() {
        FavoriteUsagePoint favoriteUsagePoint = mock(FavoriteUsagePoint.class);
        when(favoritesService.findFavoriteUsagePoint(usagePoint)).thenReturn(Optional.of(favoriteUsagePoint));

        FavoriteUsagePointInfo info = new FavoriteUsagePointInfo();
        info.parent = new VersionInfo<>();
        info.parent.id = usagePoint.getId();
        info.parent.version = usagePoint.getVersion();
        info.favorite = false;

        Response response = target("/favorites/usagepoints/name").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(favoritesService).removeFromFavorites(favoriteUsagePoint);
    }

    @Test
    public void testUPGIsNotFavoriteAndRemovedFromFavorites() {
        when(favoritesService.findFavoriteUsagePointGroup(usagePointGroup)).thenReturn(Optional.empty());

        FavoriteUsagePointGroupInfo info = new FavoriteUsagePointGroupInfo();
        info.parent = new VersionInfo<>();
        info.parent.id = usagePointGroup.getId();
        info.parent.version = usagePointGroup.getVersion();
        info.favorite = false;

        Response response = target("/favorites/usagepointgroups/100").request().put(Entity.json(info));

        verify(favoritesService).findFavoriteUsagePointGroup(usagePointGroup);
        verifyNoMoreInteractions(favoritesService);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testUPIsNotFavoriteAndRemovedFromFavorites() {
        when(favoritesService.findFavoriteUsagePoint(usagePoint)).thenReturn(Optional.empty());

        FavoriteUsagePointInfo info = new FavoriteUsagePointInfo();
        info.parent = new VersionInfo<>();
        info.parent.id = usagePoint.getId();
        info.parent.version = usagePoint.getVersion();
        info.favorite = false;

        Response response = target("/favorites/usagepoints/name").request().put(Entity.json(info));

        verify(favoritesService).findFavoriteUsagePoint(usagePoint);
        verifyNoMoreInteractions(favoritesService);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testUPGIsMadeFavoriteWithBadUPGVersion() {
        long badVersion = usagePointGroup.getVersion() - 1;
        when(meteringGroupsService.findAndLockUsagePointGroupByIdAndVersion(usagePointGroup.getId(), badVersion))
                .thenReturn(Optional.empty());

        FavoriteUsagePointGroupInfo info = new FavoriteUsagePointGroupInfo();
        info.parent = new VersionInfo<>();
        info.parent.id = usagePointGroup.getId();
        info.parent.version = badVersion;
        info.favorite = true;

        Response response = target("/favorites/usagepointgroups/100").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        ConcurrentModificationInfo concurrentModificationInfo = response.readEntity(ConcurrentModificationInfo.class);
        assertThat(concurrentModificationInfo.messageTitle).isEqualTo("Failed to flag 'name' as favorite");
        assertThat(concurrentModificationInfo.messageBody).isEqualTo("Usage point group has changed since the page was last updated.");
        assertThat(concurrentModificationInfo.parent.id.toString()).isEqualTo(Long.toString(usagePointGroup.getId()));
        assertThat(concurrentModificationInfo.parent.version).isEqualTo(usagePointGroup.getVersion());
    }

    @Test
    public void testUPIsMadeFavoriteWithBadUPVersion() {
        long badVersion = usagePoint.getVersion() - 1;
        when(meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), badVersion))
                .thenReturn(Optional.empty());

        FavoriteUsagePointInfo info = new FavoriteUsagePointInfo();
        info.parent = new VersionInfo<>();
        info.parent.id = usagePoint.getId();
        info.parent.version = badVersion;
        info.favorite = true;

        Response response = target("/favorites/usagepoints/name").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        ConcurrentModificationInfo concurrentModificationInfo = response.readEntity(ConcurrentModificationInfo.class);
        assertThat(concurrentModificationInfo.messageTitle).isEqualTo("Failed to flag 'name' as favorite");
        assertThat(concurrentModificationInfo.messageBody).isEqualTo("Usage point has changed since the page was last updated.");
        assertThat(concurrentModificationInfo.parent.id.toString()).isEqualTo(Long.toString(usagePoint.getId()));
        assertThat(concurrentModificationInfo.parent.version).isEqualTo(usagePoint.getVersion());
    }

    @Test
    public void testUPGIsRemovedFromFavoritesWithBadUPGVersion() {
        long badVersion = usagePoint.getVersion() - 1;
        when(meteringGroupsService.findAndLockUsagePointGroupByIdAndVersion(usagePointGroup.getId(), badVersion))
                .thenReturn(Optional.empty());

        FavoriteUsagePointGroupInfo info = new FavoriteUsagePointGroupInfo();
        info.parent = new VersionInfo<>();
        info.parent.id = usagePointGroup.getId();
        info.parent.version = badVersion;
        info.favorite = false;

        Response response = target("/favorites/usagepointgroups/100").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        ConcurrentModificationInfo concurrentModificationInfo = response.readEntity(ConcurrentModificationInfo.class);
        assertThat(concurrentModificationInfo.messageTitle).isEqualTo("Failed to remove 'name' from the favorites");
        assertThat(concurrentModificationInfo.messageBody).isEqualTo("Usage point group has changed since the page was last updated.");
        assertThat(concurrentModificationInfo.parent.id.toString()).isEqualTo(Long.toString(usagePointGroup.getId()));
        assertThat(concurrentModificationInfo.parent.version).isEqualTo(usagePointGroup.getVersion());
    }

    @Test
    public void testUPIsRemovedFromFavoritesWithBadUPVersion() {
        long badVersion = usagePoint.getVersion() - 1;
        when(meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), badVersion))
                .thenReturn(Optional.empty());

        FavoriteUsagePointInfo info = new FavoriteUsagePointInfo();
        info.parent = new VersionInfo<>();
        info.parent.id = usagePoint.getId();
        info.parent.version = badVersion;
        info.favorite = false;

        Response response = target("/favorites/usagepoints/name").request().put(Entity.json(info));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        ConcurrentModificationInfo concurrentModificationInfo = response.readEntity(ConcurrentModificationInfo.class);
        assertThat(concurrentModificationInfo.messageTitle).isEqualTo("Failed to remove 'name' from the favorites");
        assertThat(concurrentModificationInfo.messageBody).isEqualTo("Usage point has changed since the page was last updated.");
        assertThat(concurrentModificationInfo.parent.id.toString()).isEqualTo(Long.toString(usagePoint.getId()));
        assertThat(concurrentModificationInfo.parent.version).isEqualTo(usagePoint.getVersion());
    }

    @Test
    public void testGetUsagePointFavoriteFlag(){
        FavoriteUsagePoint favoriteUsagePoint = mock(FavoriteUsagePoint.class);
        when(favoritesService.getFavoriteUsagePoints()).thenReturn(Collections.singletonList(favoriteUsagePoint));
        when(favoriteUsagePoint.getCreationDate()).thenReturn(Instant.now());
        when(favoriteUsagePoint.getComment()).thenReturn("Comment1");
        when(favoriteUsagePoint.getUsagePoint()).thenReturn(usagePoint);
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(serviceCategory.getDisplayName()).thenReturn("ServiceCategory");
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.empty());
        when(usagePoint.getCurrentConnectionState()).thenReturn(Optional.empty());
        when(usagePoint.getCreateDate()).thenReturn(Instant.now());
        State usagePointState = mock(State.class);
        when(usagePoint.getState()).thenReturn(usagePointState);
        when(usagePointState.getName()).thenReturn("TestState");

        Response response = target("/favorites/usagepoints").request().get();
        assertThat(response.getStatus()).isEqualTo(200);

    }

    @Test
    public void testGetUsagePointGroupsFavorite(){
        FavoriteUsagePointGroup favoriteUsagePointGroup = mock(FavoriteUsagePointGroup.class);
        UsagePointGroup usagePointGroup = mock(UsagePointGroup.class);
        when(favoritesService.getFavoriteUsagePointGroups()).thenReturn(Collections.singletonList(favoriteUsagePointGroup));
        when(favoriteUsagePointGroup.getComment()).thenReturn("Comment");
        when(favoriteUsagePointGroup.getCreationDate()).thenReturn(Instant.now());
        when(favoriteUsagePointGroup.getUsagePointGroup()).thenReturn(usagePointGroup);
        when(usagePointGroup.getId()).thenReturn(1L);
        when(usagePointGroup.getName()).thenReturn("UPG name");
        when(usagePointGroup.isDynamic()).thenReturn(false);
        when(usagePointGroup.getVersion()).thenReturn(1L);
        Response response = target("/favorites/usagepointgroups").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testUpdateUsagePointGroupsFavorite(){
        FavoriteUsagePointGroup favoriteUsagePointGroup = mock(FavoriteUsagePointGroup.class);
        UsagePointGroup usagePointGroup = mock(UsagePointGroup.class);
        when(favoritesService.getFavoriteUsagePointGroups()).thenReturn(Collections.singletonList(favoriteUsagePointGroup));
        when(favoriteUsagePointGroup.getComment()).thenReturn("Comment");
        when(favoriteUsagePointGroup.getCreationDate()).thenReturn(Instant.now());
        when(favoriteUsagePointGroup.getUsagePointGroup()).thenReturn(usagePointGroup);
        when(usagePointGroup.getId()).thenReturn(1L);
        when(usagePointGroup.getName()).thenReturn("UPG name");
        when(usagePointGroup.isDynamic()).thenReturn(false);
        when(usagePointGroup.getVersion()).thenReturn(1L);
        when(meteringGroupsService.findAndLockUsagePointGroupByIdAndVersion(1L, 1L)).thenReturn(Optional.of(usagePointGroup));

        FavoriteUsagePointGroupDetailsInfo favInfo = new FavoriteUsagePointGroupDetailsInfo(favoriteUsagePointGroup);
        FavoriteUsagePointGroupDetailsInfo.FavoriteUsagePointGroups info = new FavoriteUsagePointGroupDetailsInfo.FavoriteUsagePointGroups();
        info.favoriteUsagePointGroups = Collections.singletonList(favInfo);

        Response response = target("/favorites/usagepointgroups").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

}
