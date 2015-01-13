package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.yellowfin.groups.AdHocDeviceGroup;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.rest.AdhocGroupInfo;
import com.energyict.mdc.device.data.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;

@Path("/cachegroups")
public class AdhocGroupResource {
    private final DeviceService deviceService;
    private final ResourceHelper resourceHelper;

    private final YellowfinGroupsService yellowfinGroupsService;

    @Inject
    public AdhocGroupResource(
            ResourceHelper resourceHelper,
            DeviceService deviceService,
            YellowfinGroupsService yellowfinGroupsService) {

        this.resourceHelper = resourceHelper;
        this.deviceService = deviceService;
        this.yellowfinGroupsService = yellowfinGroupsService;
    }

    @POST
    @Path("/adhoc")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_DEVICE})
    public AdhocGroupInfo cacheAdHocGroup(@BeanParam QueryParameters queryParameters, @BeanParam StandardParametersBean params,  @Context UriInfo uriInfo) {
        AdhocGroupInfoImpl groupInfo = new AdhocGroupInfoImpl();

        Condition condition;
        MultivaluedMap<String, String> uriParams = uriInfo.getQueryParameters();
        if (uriParams.containsKey("filter")) {
            condition = resourceHelper.getQueryConditionForDevice(uriInfo.getQueryParameters());
        } else {
            condition = resourceHelper.getQueryConditionForDevice(params);
        }
        Finder<Device> allDevicesFinder = deviceService.findAllDevices(condition);
        List<Device> allDevices = allDevicesFinder.from(queryParameters).find();

        Optional<AdHocDeviceGroup>  adhocGroup = yellowfinGroupsService.cacheAdHocDeviceGroup(allDevices);
        if(adhocGroup.isPresent()){
            groupInfo.name = adhocGroup.get().getName();
        }
        return groupInfo;
    }

}
