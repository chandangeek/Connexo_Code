package com.elster.jupiter.mdm.usagepoint.data.rest.impl.favorites;

import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoriteUsagePoint;
import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoriteUsagePointGroup;
import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoritesService;
import com.elster.jupiter.mdm.usagepoint.data.rest.impl.MessageSeeds;
import com.elster.jupiter.mdm.usagepoint.data.rest.impl.ResourceHelper;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.Transactional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/favorites")
public class FavoritesResource {
    private static final String USAGE_POINT_GROUPS_RESOURCE = "/usagepointgroups";
    private static final String USAGE_POINTS_RESOURCE = "/usagepoints";
    private final ResourceHelper resourceHelper;
    private final MeteringService meteringService;
    private final MeteringGroupsService meteringGroupsService;
    private final FavoritesService favoritesService;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public FavoritesResource(ResourceHelper resourceHelper,
                             MeteringService meteringService,
                             MeteringGroupsService meteringGroupsService,
                             FavoritesService favoritesService,
                             ConcurrentModificationExceptionFactory conflictFactory) {
        this.resourceHelper = resourceHelper;
        this.meteringService = meteringService;
        this.meteringGroupsService = meteringGroupsService;
        this.favoritesService = favoritesService;
        this.conflictFactory = conflictFactory;
    }

    @GET
    @Transactional
    @Path(USAGE_POINT_GROUPS_RESOURCE + "/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_USAGE_POINT_GROUP,
            com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_USAGE_POINT_ENUMERATED_GROUP,
            com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.VIEW_USAGE_POINT_GROUP_DETAIL})
    public FavoriteUsagePointGroupInfo getFavoriteUsagePointGroup(@PathParam("id") long id) {
        UsagePointGroup usagePointGroup = resourceHelper.findUsagePointGroupOrThrowException(id);
        return favoritesService.findFavoriteUsagePointGroup(usagePointGroup)
                .map(FavoriteUsagePointGroupInfo::new)
                .orElse(new FavoriteUsagePointGroupInfo(usagePointGroup));
    }

    @GET
    @Transactional
    @Path(USAGE_POINTS_RESOURCE + "/{name}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.metering.security.Privileges.Constants.VIEW_ANY_USAGEPOINT,
            com.elster.jupiter.metering.security.Privileges.Constants.VIEW_OWN_USAGEPOINT,
            com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_ANY_USAGEPOINT,
            com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_OWN_USAGEPOINT})
    public FavoriteUsagePointInfo getFavoriteUsagePoint(@PathParam("name") String name) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        return favoritesService.findFavoriteUsagePoint(usagePoint)
                .map(FavoriteUsagePointInfo::new)
                .orElse(new FavoriteUsagePointInfo(usagePoint));
    }

    @PUT
    @Transactional
    @Path(USAGE_POINT_GROUPS_RESOURCE + "/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_USAGE_POINT_GROUP,
            com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_USAGE_POINT_ENUMERATED_GROUP,
            com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.VIEW_USAGE_POINT_GROUP_DETAIL})
    public FavoriteUsagePointGroupInfo createFavoriteUsagePointGroup(@PathParam("id") long id, FavoriteUsagePointGroupInfo info) {
        info.parent.id = id;
        UsagePointGroup usagePointGroup = lockUsagePointGroupOrThrowException(info, false);
        FavoriteUsagePointGroup favoriteUsagePointGroup = favoritesService.findOrCreateFavoriteUsagePointGroup(usagePointGroup);
        favoriteUsagePointGroup.setComment(info.comment);
        return new FavoriteUsagePointGroupInfo(favoriteUsagePointGroup);
    }

    @PUT
    @Transactional
    @Path(USAGE_POINTS_RESOURCE + "/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.metering.security.Privileges.Constants.VIEW_ANY_USAGEPOINT,
            com.elster.jupiter.metering.security.Privileges.Constants.VIEW_OWN_USAGEPOINT,
            com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_ANY_USAGEPOINT,
            com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_OWN_USAGEPOINT})
    public FavoriteUsagePointInfo createFavoriteUsagePoint(@PathParam("name") String name, FavoriteUsagePointInfo info) {
        info.parent.name = name;
        UsagePoint usagePoint = lockUsagePointOrThrowException(info, false);
        FavoriteUsagePoint favoriteUsagePoint = favoritesService.findOrCreateFavoriteUsagePoint(usagePoint);
        favoriteUsagePoint.setComment(info.comment);
        return new FavoriteUsagePointInfo(favoriteUsagePoint);
    }

    @DELETE
    @Transactional
    @Path(USAGE_POINT_GROUPS_RESOURCE + "/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_USAGE_POINT_GROUP,
            com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_USAGE_POINT_ENUMERATED_GROUP,
            com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.VIEW_USAGE_POINT_GROUP_DETAIL})
    public Response removeFavoriteUsagePointGroup(@PathParam("id") long id, FavoriteUsagePointGroupInfo info) {
        info.parent.id = id;
        UsagePointGroup usagePointGroup = lockUsagePointGroupOrThrowException(info, true);
        favoritesService.findFavoriteUsagePointGroup(usagePointGroup)
                .ifPresent(favoritesService::removeFavoriteUsagePointGroup);
        return Response.ok().build();
    }

    @DELETE
    @Transactional
    @Path(USAGE_POINTS_RESOURCE + "/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.metering.security.Privileges.Constants.VIEW_ANY_USAGEPOINT,
            com.elster.jupiter.metering.security.Privileges.Constants.VIEW_OWN_USAGEPOINT,
            com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_ANY_USAGEPOINT,
            com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_OWN_USAGEPOINT})
    public Response removeFavoriteUsagePoint(@PathParam("name") String name, FavoriteUsagePointInfo info) {
        info.parent.name = name;
        UsagePoint usagePoint = lockUsagePointOrThrowException(info, true);
        favoritesService.findFavoriteUsagePoint(usagePoint)
                .ifPresent(favoritesService::removeFavoriteUsagePoint);
        return Response.ok().build();
    }

    private UsagePointGroup lockUsagePointGroupOrThrowException(FavoriteUsagePointGroupInfo info, boolean remove) {
        return meteringGroupsService.findAndLockUsagePointGroupByIdAndVersion(info.parent.id, info.parent.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn("Usage point group")
                        .withActualParent(() -> meteringGroupsService.findUsagePointGroup(info.parent.id)
                                .map(UsagePointGroup::getVersion)
                                .orElse(null), info.parent.id)
                        .withMessageTitle(remove ?
                                        MessageSeeds.REMOVE_FROM_FAVORITES_CONFLICT_TITLE :
                                        MessageSeeds.FLAG_AS_FAVORITE_CONFLICT_TITLE,
                                info.parent.name)
                        .supplier());
    }

    private UsagePoint lockUsagePointOrThrowException(FavoriteUsagePointInfo info, boolean remove) {
        return meteringService.findAndLockUsagePointByIdAndVersion(info.parent.id, info.parent.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn("Usage point")
                        .withActualParent(() -> meteringService.findUsagePointById(info.parent.id)
                                .map(UsagePoint::getVersion)
                                .orElse(null), info.parent.id)
                        .withMessageTitle(remove ?
                                        MessageSeeds.REMOVE_FROM_FAVORITES_CONFLICT_TITLE :
                                        MessageSeeds.FLAG_AS_FAVORITE_CONFLICT_TITLE,
                                info.parent.name)
                        .supplier());
    }
}
