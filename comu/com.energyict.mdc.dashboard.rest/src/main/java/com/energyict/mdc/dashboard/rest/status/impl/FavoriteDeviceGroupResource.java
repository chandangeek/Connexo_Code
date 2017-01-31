/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.users.User;
import com.energyict.mdc.favorites.FavoriteDeviceGroup;
import com.energyict.mdc.favorites.FavoritesService;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.energyict.mdc.dashboard.rest.status.impl.FavoriteDeviceGroupInfo.byNameComparator;

@Path("/favoritedevicegroups")
public class FavoriteDeviceGroupResource {
    
    private final MeteringGroupsService meteringGroupsService;
    private final FavoritesService favoritesService;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public FavoriteDeviceGroupResource(MeteringGroupsService meteringGroupsService, FavoritesService favoritesService, ConcurrentModificationExceptionFactory conflictFactory) {
        this.meteringGroupsService = meteringGroupsService;
        this.favoritesService = favoritesService;
        this.conflictFactory = conflictFactory;
    }
    
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    //@RolesAllowed({Privileges.VIEW_DEVICE_GROUP_DETAIL, Privileges.ADMINISTRATE_DEVICE_GROUP, Privileges.ADMINISTRATE_DEVICE_ENUMERATED_GROUP})
    public PagedInfoList getFavoriteDeviceGroups(@QueryParam("includeAllGroups") boolean includeAllGroups, @BeanParam JsonQueryParameters queryParameters, @Context SecurityContext securityContext) {
        User user = (User) securityContext.getUserPrincipal();
        List<EndDeviceGroup> favoriteDeviceGroups = favoritesService.getFavoriteDeviceGroups(user).stream().map(FavoriteDeviceGroup::getEndDeviceGroup).collect(Collectors.toList());
        List<FavoriteDeviceGroupInfo> infos = new ArrayList<>();
        if (includeAllGroups) {
            infos = meteringGroupsService.findEndDeviceGroups().stream().map(edg -> FavoriteDeviceGroupInfo.asInfo(edg, favoriteDeviceGroups)).sorted(byNameComparator).collect(Collectors.toList());
        } else {
            infos = favoriteDeviceGroups.stream().map(edg -> FavoriteDeviceGroupInfo.asInfo(edg)).sorted(byNameComparator).collect(Collectors.toList());
        }
        return PagedInfoList.fromPagedList("favoriteDeviceGroups", infos, queryParameters);
    }
    
    @PUT @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    //@RolesAllowed({Privileges.ADMINISTRATE_DEVICE_GROUP, Privileges.ADMINISTRATE_DEVICE_ENUMERATED_GROUP})
    public Response updateFavoriteDeviceGroups(FavoriteDeviceGroupInfo.SelectionInfo selection, @Context SecurityContext securityContext) {
        User user = (User) securityContext.getUserPrincipal();
        Map<Long, FavoriteDeviceGroup> currentFavGroupList = favoritesService.getFavoriteDeviceGroups(user)
                .stream()
                .collect(Collectors.toMap(fdg -> fdg.getEndDeviceGroup().getId(), Function.identity()));
        for (FavoriteDeviceGroupInfo info : selection.ids) {
            boolean alreadyMarkedAsFavorite = currentFavGroupList.containsKey(info.id);
            /*
            +--------------+-----------+----------+--------+
            | was favorite | should be | already  | action |
            |(version == 1)| favorite  | favorite |        |
            +--------------+-----------+----------+--------+
            |      -       |     +     |     +    |nothing |
            +--------------+-----------+----------+--------+
            |      -       |     +     |     -    |add     |
            +--------------+-----------+----------+--------+
            |      -       |     -     |     +    |thr ex  |
            +--------------+-----------+----------+--------+
            |      -       |     -     |     -    |nothing |
            +--------------+-----------+----------+--------+
            |      +       |     +     |     +    |nothing |
            +--------------+-----------+----------+--------+
            |      +       |     +     |     -    |thr ex  |
            +--------------+-----------+----------+--------+
            |      +       |     -     |     +    |remove  |
            +--------------+-----------+----------+--------+
            |      +       |     -     |     -    |nothing |
            +--------------+-----------+----------+--------+
             */
            if (info.favorite != alreadyMarkedAsFavorite){
                if (info.favorite && info.version == 0){ // group was marked as favorite
                    EndDeviceGroup endDeviceGroup = getLockedEndDeviceGroup(info);
                    favoritesService.findOrCreateFavoriteDeviceGroup(endDeviceGroup, user);
                } else if (alreadyMarkedAsFavorite && info.version == 1){ // group was unmarked as favorite
                    getLockedEndDeviceGroup(info);
                    favoritesService.removeFavoriteDeviceGroup(currentFavGroupList.get(info.id));
                } else {
                    throw conflictFactory.contextDependentConflictOn(info.name)
                            .withActualVersion(() -> meteringGroupsService.findEndDeviceGroup(info.id).map(EndDeviceGroup::getVersion).orElse(null))
                            .build();
                }
            }
        }
        return Response.ok().build();
    }

    private EndDeviceGroup getLockedEndDeviceGroup(FavoriteDeviceGroupInfo info) {
        return meteringGroupsService.findAndLockEndDeviceGroupByIdAndVersion(info.parent.id, info.parent.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> meteringGroupsService.findEndDeviceGroup(info.id).map(EndDeviceGroup::getVersion).orElse(null))
                        .supplier());
    }
}
