package com.elster.jupiter.yellowfin.rest.impl;

import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;


@Path("/cachegroups")
public class YellowfinDeviceGroupsResource {

    private final YellowfinGroupsService yellowfinGroupsService;
    private final DeviceService deviceService;
    private final TransactionService transactionService;

    @Inject
    private YellowfinDeviceGroupsResource(YellowfinGroupsService yellowfinGroupsService, DeviceService deviceService, TransactionService transactionService){
        this.yellowfinGroupsService = yellowfinGroupsService;
        this.transactionService = transactionService;
        this.deviceService = deviceService;
    }

    @POST
    @Path("/dynamic")
    @Consumes(MediaType.APPLICATION_JSON)
    public void cacheDynamicGroup(YellowfinDeviceGroupInfos groupInfos) {
        try(TransactionContext context = transactionService.getContext()){
            for(YellowfinDeviceGroupInfo group : groupInfos.groups){
                yellowfinGroupsService.cacheDynamicDeviceGroup(group.name);
            }
            context.commit();
        }
    }
}
