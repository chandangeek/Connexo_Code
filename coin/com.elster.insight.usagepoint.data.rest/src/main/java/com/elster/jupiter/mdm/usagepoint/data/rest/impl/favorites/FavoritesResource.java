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
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/favorites")
public class FavoritesResource {
    private static final String USAGE_POINT_GROUPS_RESOURCE = "/usagepointgroups";
    private static final String USAGE_POINTS_RESOURCE = "/usagepoints";
    private final ResourceHelper resourceHelper;
    private final Thesaurus thesaurus;
    private final MeteringService meteringService;
    private final MeteringGroupsService meteringGroupsService;
    private final FavoritesService favoritesService;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public FavoritesResource(ResourceHelper resourceHelper,
                             Thesaurus thesaurus,
                             MeteringService meteringService,
                             MeteringGroupsService meteringGroupsService,
                             FavoritesService favoritesService,
                             ConcurrentModificationExceptionFactory conflictFactory) {
        this.resourceHelper = resourceHelper;
        this.thesaurus = thesaurus;
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
    public FavoriteUsagePointGroupInfo getUsagePointGroupFavoriteFlag(@PathParam("id") long id) {
        UsagePointGroup usagePointGroup = resourceHelper.findUsagePointGroupOrThrowException(id);
        return favoritesService.findFavoriteUsagePointGroup(usagePointGroup)
                .map(FavoriteUsagePointGroupInfo::new)
                .orElseGet(() -> new FavoriteUsagePointGroupInfo(usagePointGroup));
    }

    @GET
    @Transactional
    @Path(USAGE_POINTS_RESOURCE + "/{name}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.metering.security.Privileges.Constants.VIEW_ANY_USAGEPOINT,
            com.elster.jupiter.metering.security.Privileges.Constants.VIEW_OWN_USAGEPOINT,
            com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_ANY_USAGEPOINT,
            com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_OWN_USAGEPOINT})
    public FavoriteUsagePointInfo getUsagePointFavoriteFlag(@PathParam("name") String name) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(name);
        return favoritesService.findFavoriteUsagePoint(usagePoint)
                .map(FavoriteUsagePointInfo::new)
                .orElseGet(() -> new FavoriteUsagePointInfo(usagePoint));
    }

    @PUT
    @Transactional
    @Path(USAGE_POINT_GROUPS_RESOURCE + "/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_USAGE_POINT_GROUP,
            com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_USAGE_POINT_ENUMERATED_GROUP,
            com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.VIEW_USAGE_POINT_GROUP_DETAIL})
    public FavoriteUsagePointGroupInfo updateUsagePointGroupFavoriteFlag(@PathParam("id") long id, FavoriteUsagePointGroupInfo info) {
        info.parent.id = id;
        UsagePointGroup usagePointGroup = lockUsagePointGroupOrThrowException(info, !info.favorite);
        if (info.favorite) {
            FavoriteUsagePointGroup favoriteUsagePointGroup = favoritesService.markFavorite(usagePointGroup);
            if (!Objects.equals(favoriteUsagePointGroup.getComment(), info.comment)) {
                favoriteUsagePointGroup.updateComment(info.comment);
            }
            return new FavoriteUsagePointGroupInfo(favoriteUsagePointGroup);
        } else {
            favoritesService.findFavoriteUsagePointGroup(usagePointGroup)
                    .ifPresent(favoritesService::removeFromFavorites);
            return new FavoriteUsagePointGroupInfo(usagePointGroup);
        }
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
    public FavoriteUsagePointInfo updateUsagePointFavoriteFlag(@PathParam("name") String name, FavoriteUsagePointInfo info) {
        UsagePoint usagePoint = lockUsagePointOrThrowException(info, name, !info.favorite);
        if (info.favorite) {
            FavoriteUsagePoint favoriteUsagePoint = favoritesService.markFavorite(usagePoint);
            if (!Objects.equals(favoriteUsagePoint.getComment(), info.comment)) {
                favoriteUsagePoint.updateComment(info.comment);
            }
            return new FavoriteUsagePointInfo(favoriteUsagePoint);
        } else {
            favoritesService.findFavoriteUsagePoint(usagePoint)
                    .ifPresent(favoritesService::removeFromFavorites);
            return new FavoriteUsagePointInfo(usagePoint);
        }
    }

    @GET
    @Transactional
    @Path(USAGE_POINTS_RESOURCE)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.metering.security.Privileges.Constants.VIEW_ANY_USAGEPOINT,
            com.elster.jupiter.metering.security.Privileges.Constants.VIEW_OWN_USAGEPOINT,
            com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_ANY_USAGEPOINT,
            com.elster.jupiter.metering.security.Privileges.Constants.ADMINISTER_OWN_USAGEPOINT})
    public Response getFavoriteUsagePoints(@BeanParam JsonQueryParameters queryParameters) {
        List<FavoriteUsagePointDetailsInfo> infos = favoritesService.getFavoriteUsagePoints().stream()
                .map(up -> new FavoriteUsagePointDetailsInfo(up, thesaurus))
                .sorted((u1, u2) -> u2.flaggedDate.compareTo(u1.flaggedDate))//descending order
                .collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromPagedList("usagePoints", infos, queryParameters)).build();
    }

    @GET
    @Transactional
    @Path(USAGE_POINT_GROUPS_RESOURCE)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_USAGE_POINT_GROUP,
            com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_USAGE_POINT_ENUMERATED_GROUP,
            com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.VIEW_USAGE_POINT_GROUP_DETAIL})
    public Response getFavoriteUsagePointGroups(@QueryParam("includeAllGroups") boolean includeAllGroups, @BeanParam JsonQueryParameters queryParameters) {
        List<FavoriteUsagePointGroup> favoriteUsagePointGroups = favoritesService.getFavoriteUsagePointGroups();
        List<FavoriteUsagePointGroupDetailsInfo> infos = new ArrayList<>();
        if (includeAllGroups) {
            infos = meteringGroupsService.findUsagePointGroups().stream()
                    .map(upg -> new FavoriteUsagePointGroupDetailsInfo(upg, favoriteUsagePointGroups))
                    .sorted((g1, g2) -> g1.name.compareToIgnoreCase(g2.name))
                    .collect(Collectors.toList());
        } else {
            infos = favoriteUsagePointGroups.stream()
                    .map(FavoriteUsagePointGroupDetailsInfo::new)
                    .sorted((g1, g2) -> g2.flaggedDate.compareTo(g1.flaggedDate))//descending order
                    .collect(Collectors.toList());
        }
        return Response.ok(PagedInfoList.fromPagedList("usagePointGroups", infos, queryParameters)).build();
    }

    @PUT
    @Transactional
    @Path(USAGE_POINT_GROUPS_RESOURCE)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_USAGE_POINT_GROUP,
            com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.ADMINISTER_USAGE_POINT_ENUMERATED_GROUP,
            com.elster.jupiter.mdm.usagepoint.data.security.Privileges.Constants.VIEW_USAGE_POINT_GROUP_DETAIL})
    public Response updateFavoriteUsagePointGroups(FavoriteUsagePointGroupDetailsInfo.FavoriteUsagePointGroups usagePointGroups, @BeanParam JsonQueryParameters queryParameters) {
        List<FavoriteUsagePointGroup> favoriteUsagePointGroups = favoritesService.getFavoriteUsagePointGroups();
        List<FavoriteUsagePointGroupDetailsInfo> savedFavoriteUsagePointGroups = meteringGroupsService.findUsagePointGroups().stream()
                .map(upg -> new FavoriteUsagePointGroupDetailsInfo(upg, favoriteUsagePointGroups))
                .collect(Collectors.toList());
        List<FavoriteUsagePointGroupDetailsInfo> toUpdateFavoriteUsagePointGroups = usagePointGroups.favoriteUsagePointGroups.stream()
                .filter(fav -> (savedFavoriteUsagePointGroups.stream()
                        .filter(savedFav -> {
                            return (savedFav.getId().compareTo(fav.getId())) == 0 && (savedFav.getFavorite() == fav.getFavorite());
                        })
                        .count()) < 1)
                .collect(Collectors.toList());

        toUpdateFavoriteUsagePointGroups.stream().forEach(fav -> {
            FavoriteUsagePointGroupInfo info = fav.toFavoriteUsagePointGroupInfo();
            UsagePointGroup usagePointGroup = lockUsagePointGroupOrThrowException(info, !fav.favorite);
            if (fav.favorite) {
                favoritesService.markFavorite(usagePointGroup);
            } else {
                favoritesService.findFavoriteUsagePointGroup(usagePointGroup)
                        .ifPresent(favoritesService::removeFromFavorites);
            }
        });
        return Response.ok().build();
    }

    private UsagePointGroup lockUsagePointGroupOrThrowException(FavoriteUsagePointGroupInfo info, boolean remove) {
        return meteringGroupsService.findAndLockUsagePointGroupByIdAndVersion(info.parent.id, info.parent.version)
                .orElseThrow(() -> {
                    Optional<UsagePointGroup> usagePointGroup = meteringGroupsService.findUsagePointGroup(info.parent.id);
                    return conflictFactory.contextDependentConflictOn("Usage point group")
                            .withActualParent(() -> usagePointGroup.map(UsagePointGroup::getVersion).orElse(null),
                                    info.parent.id)
                            .withMessageTitle(remove ?
                                            MessageSeeds.REMOVE_FROM_FAVORITES_CONFLICT_TITLE :
                                            MessageSeeds.FLAG_AS_FAVORITE_CONFLICT_TITLE,
                                    usagePointGroup.map(UsagePointGroup::getName).orElse(null))
                            .build();
                });
    }

    private UsagePoint lockUsagePointOrThrowException(FavoriteUsagePointInfo info, String name, boolean remove) {
        return meteringService.findAndLockUsagePointByIdAndVersion(info.parent.id, info.parent.version)
                .orElseThrow(() -> {
                    Optional<UsagePoint> usagePoint = meteringService.findUsagePointById(info.parent.id);
                    return conflictFactory.contextDependentConflictOn("Usage point")
                            .withActualParent(() -> usagePoint.map(UsagePoint::getVersion).orElse(null),
                                    info.parent.id)
                            .withMessageTitle(remove ?
                                            MessageSeeds.REMOVE_FROM_FAVORITES_CONFLICT_TITLE :
                                            MessageSeeds.FLAG_AS_FAVORITE_CONFLICT_TITLE,
                                    usagePoint.map(UsagePoint::getName).orElse(name))
                            .build();
                });
    }
}
