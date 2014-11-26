package com.energyict.mdc.dashboard.rest.status.impl;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;

@Path("/favouritedevicegroups")
public class FavoriteDeviceGroupResource {
    
    private final MeteringGroupsService meteringGroupsService;

    @Inject
    public FavoriteDeviceGroupResource(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PagedInfoList getFavouriteDeviceGroups(@QueryParam("includeAllGroups") boolean includeAllGroups, @BeanParam QueryParameters queryParameters) {
        return null;
    }    
}
