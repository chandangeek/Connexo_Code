package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoriteUsagePoint;
import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoriteUsagePointGroup;
import com.elster.jupiter.mdm.usagepoint.data.rest.impl.favorites.FavoriteUsagePointGroupInfo;
import com.elster.jupiter.mdm.usagepoint.data.rest.impl.favorites.FavoriteUsagePointInfo;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.rest.util.ConcurrentModificationInfo;
import com.elster.jupiter.rest.util.VersionInfo;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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
        info.comment = "My comment";

        Instant now = Instant.now();
        FavoriteUsagePointGroup favoriteUsagePointGroup = mock(FavoriteUsagePointGroup.class);
        when(favoriteUsagePointGroup.getComment()).thenReturn(info.comment);
        when(favoriteUsagePointGroup.getCreationDate()).thenReturn(now);
        when(favoriteUsagePointGroup.getUsagePointGroup()).thenReturn(usagePointGroup);
        when(favoritesService.findFavoriteUsagePointGroup(usagePointGroup)).thenReturn(Optional.empty());
        when(favoritesService.findOrCreateFavoriteUsagePointGroup(usagePointGroup))
                .thenReturn(favoriteUsagePointGroup);

        Response response = target("/favorites/usagepointgroups/100").request().put(Entity.json(info));

        verify(favoritesService).findOrCreateFavoriteUsagePointGroup(usagePointGroup);
        verify(favoriteUsagePointGroup).setComment(info.comment);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        FavoriteUsagePointGroupInfo createdFavoriteInfo = response.readEntity(FavoriteUsagePointGroupInfo.class);
        assertThat(createdFavoriteInfo.parent.id).isEqualTo(100);
        assertThat(createdFavoriteInfo.parent.version).isEqualTo(1);
        assertThat(createdFavoriteInfo.favorite).isTrue();
        assertThat(createdFavoriteInfo.comment).isEqualTo("My comment");
        assertThat(createdFavoriteInfo.creationDate).isEqualTo(now);
    }

    @Test
    public void testUPIsMadeFavorite() {
        FavoriteUsagePointInfo info = new FavoriteUsagePointInfo();
        info.parent = new VersionInfo<>();
        info.parent.id = usagePoint.getId();
        info.parent.version = usagePoint.getVersion();
        info.comment = "My comment";

        Instant now = Instant.now();
        FavoriteUsagePoint favoriteUsagePoint = mock(FavoriteUsagePoint.class);
        when(favoriteUsagePoint.getComment()).thenReturn(info.comment);
        when(favoriteUsagePoint.getCreationDate()).thenReturn(now);
        when(favoriteUsagePoint.getUsagePoint()).thenReturn(usagePoint);
        when(favoritesService.findFavoriteUsagePoint(usagePoint)).thenReturn(Optional.empty());
        when(favoritesService.findOrCreateFavoriteUsagePoint(usagePoint))
                .thenReturn(favoriteUsagePoint);

        Response response = target("/favorites/usagepoints/name").request().put(Entity.json(info));

        verify(favoritesService).findOrCreateFavoriteUsagePoint(usagePoint);
        verify(favoriteUsagePoint).setComment(info.comment);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        FavoriteUsagePointInfo createdFavoriteInfo = response.readEntity(FavoriteUsagePointInfo.class);
        assertThat(createdFavoriteInfo.parent.id).isEqualTo(100);
        assertThat(createdFavoriteInfo.parent.version).isEqualTo(1);
        assertThat(createdFavoriteInfo.favorite).isTrue();
        assertThat(createdFavoriteInfo.comment).isEqualTo("My comment");
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

        Response response = target("/favorites/usagepointgroups/100").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(favoritesService).removeFavoriteUsagePointGroup(favoriteUsagePointGroup);
    }

    @Test
    public void testUPIsRemovedFromFavorites() {
        FavoriteUsagePoint favoriteUsagePoint = mock(FavoriteUsagePoint.class);
        when(favoritesService.findFavoriteUsagePoint(usagePoint)).thenReturn(Optional.of(favoriteUsagePoint));

        FavoriteUsagePointInfo info = new FavoriteUsagePointInfo();
        info.parent = new VersionInfo<>();
        info.parent.id = usagePoint.getId();
        info.parent.version = usagePoint.getVersion();

        Response response = target("/favorites/usagepoints/name").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(favoritesService).removeFavoriteUsagePoint(favoriteUsagePoint);
    }

    @Test
    public void testUPGIsNotFavoriteAndRemovedFromFavorites() {
        when(favoritesService.findFavoriteUsagePointGroup(usagePointGroup)).thenReturn(Optional.empty());

        FavoriteUsagePointGroupInfo info = new FavoriteUsagePointGroupInfo();
        info.parent = new VersionInfo<>();
        info.parent.id = usagePointGroup.getId();
        info.parent.version = usagePointGroup.getVersion();

        Response response = target("/favorites/usagepointgroups/100").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();

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

        Response response = target("/favorites/usagepoints/name").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();

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

        Response response = target("/favorites/usagepointgroups/100").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();

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

        Response response = target("/favorites/usagepoints/name").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        ConcurrentModificationInfo concurrentModificationInfo = response.readEntity(ConcurrentModificationInfo.class);
        assertThat(concurrentModificationInfo.messageTitle).isEqualTo("Failed to remove 'name' from the favorites");
        assertThat(concurrentModificationInfo.messageBody).isEqualTo("Usage point has changed since the page was last updated.");
        assertThat(concurrentModificationInfo.parent.id.toString()).isEqualTo(Long.toString(usagePoint.getId()));
        assertThat(concurrentModificationInfo.parent.version).isEqualTo(usagePoint.getVersion());
    }
}
