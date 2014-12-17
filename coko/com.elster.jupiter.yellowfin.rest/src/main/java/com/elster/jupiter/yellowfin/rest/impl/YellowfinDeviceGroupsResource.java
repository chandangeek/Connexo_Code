package com.elster.jupiter.yellowfin.rest.impl;

import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/devicegroups")
public class YellowfinDeviceGroupsResource {

    private final YellowfinGroupsService yellowfinGroupsService;
    private final TransactionService transactionService;

    @Inject
    private YellowfinDeviceGroupsResource(YellowfinGroupsService yellowfinGroupsService, TransactionService transactionService){
        this.yellowfinGroupsService = yellowfinGroupsService;
        this.transactionService = transactionService;
    }

    @POST
    @Path("/dynamic/{groupname}")
    public void cacheDynamicGroup(@PathParam("groupname") String groupname) {
        try(TransactionContext context = transactionService.getContext()){
            yellowfinGroupsService.cacheDynamicDeviceGroup(groupname);
            context.commit();
        }
    }
}
