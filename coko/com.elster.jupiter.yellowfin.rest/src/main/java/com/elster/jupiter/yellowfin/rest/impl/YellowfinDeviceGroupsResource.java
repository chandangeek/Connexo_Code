package com.elster.jupiter.yellowfin.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;
import com.elster.jupiter.yellowfin.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;


@Path("/cachegroups")
public class YellowfinDeviceGroupsResource {

    private final YellowfinGroupsService yellowfinGroupsService;
    private final TransactionService transactionService;
    private final MeteringGroupsService meteringGroupsService;


    @Inject
    private YellowfinDeviceGroupsResource(YellowfinGroupsService yellowfinGroupsService, TransactionService transactionService, MeteringGroupsService meteringGroupsService){
        this.yellowfinGroupsService = yellowfinGroupsService;
        this.transactionService = transactionService;
        this.meteringGroupsService = meteringGroupsService;
    }

    @POST
    @Path("/dynamic")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.VIEW_REPORTS)
    public void cacheDynamicGroup(YellowfinDeviceGroupInfos groupInfos) {
        try(TransactionContext context = transactionService.getContext()){
            for(YellowfinDeviceGroupInfo group : groupInfos.groups){
                yellowfinGroupsService.cacheDynamicDeviceGroup(group.name);
            }
            context.commit();
        }
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_REPORTS)
    @Path("/list")
    public YellowfinDeviceGroupInfos getDeviceGroups() {

        YellowfinDeviceGroupInfos groupInfos = new YellowfinDeviceGroupInfos();
        List<EndDeviceGroup> groups = meteringGroupsService.findEndDeviceGroups();
        groupInfos.addAll(groups);
        return groupInfos;
    }


}
