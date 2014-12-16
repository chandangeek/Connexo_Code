package com.elster.jupiter.yellowfin.rest.impl;

import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("/devicegroups")
public class YellowfinDeviceGroupsResource {

    private YellowfinGroupsService yellowfinGroupsService;

    @Inject
    private YellowfinDeviceGroupsResource(YellowfinGroupsService yellowfinGroupsService){
        this.yellowfinGroupsService = yellowfinGroupsService;
    }

    @POST
    @Path("/dynamic/{groupname}")
    public void cacheDynamicGroup(@QueryParam("groupname") String groupname) {
        yellowfinGroupsService.cacheDeviceGroup(groupname);
    }
}
