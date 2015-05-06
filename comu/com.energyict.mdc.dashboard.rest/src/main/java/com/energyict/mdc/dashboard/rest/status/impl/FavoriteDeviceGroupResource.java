package com.energyict.mdc.dashboard.rest.status.impl;

import static com.energyict.mdc.dashboard.rest.status.impl.FavoriteDeviceGroupInfo.byNameComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.users.User;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.JsonQueryParameters;
import com.energyict.mdc.favorites.FavoriteDeviceGroup;
import com.energyict.mdc.favorites.FavoritesService;

@Path("/favoritedevicegroups")
public class FavoriteDeviceGroupResource {
    
    private final MeteringGroupsService meteringGroupsService;
    private final FavoritesService favoritesService;

    @Inject
    public FavoriteDeviceGroupResource(MeteringGroupsService meteringGroupsService, FavoritesService favoritesService) {
        this.meteringGroupsService = meteringGroupsService;
        this.favoritesService = favoritesService;
    }
    
    @GET
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
    
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    //@RolesAllowed({Privileges.ADMINISTRATE_DEVICE_GROUP, Privileges.ADMINISTRATE_DEVICE_ENUMERATED_GROUP})
    public Response updateFavoriteDeviceGroups(FavoriteDeviceGroupInfo.SelectionInfo selection, @Context SecurityContext securityContext) {
        User user = (User) securityContext.getUserPrincipal();
        Map<Long, FavoriteDeviceGroup> groups = favoritesService.getFavoriteDeviceGroups(user).stream().collect(Collectors.toMap(fdg -> fdg.getEndDeviceGroup().getId(), Function.identity()));
        selection.ids.stream().distinct().forEach(id -> {
            if (groups.containsKey(id)) {
                groups.remove(id);
            } else {
                Optional<EndDeviceGroup> endDeviceGroupToAdd = meteringGroupsService.findEndDeviceGroup(id);
                endDeviceGroupToAdd.ifPresent(edg -> favoritesService.findOrCreateFavoriteDeviceGroup(edg, user));
            }
        });
        groups.values().stream().forEach(favoritesService::removeFavoriteDeviceGroup);
        return Response.ok().build();
    }
}
