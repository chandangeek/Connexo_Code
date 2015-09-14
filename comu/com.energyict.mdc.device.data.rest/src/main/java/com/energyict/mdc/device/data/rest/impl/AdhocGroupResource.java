package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.yellowfin.groups.AdHocDeviceGroup;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.domain.util.Finder;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;


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
import java.util.stream.Collectors;


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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(com.elster.jupiter.yellowfin.security.Privileges.VIEW_REPORTS)
    public AdhocGroupInfo cacheAdHocGroup(@BeanParam JsonQueryParameters queryParameters, @BeanParam StandardParametersBean params,  @Context UriInfo uriInfo) {
        AdhocGroupInfo groupInfo = new AdhocGroupInfo();

        Condition condition;
        MultivaluedMap<String, String> uriParams = uriInfo.getQueryParameters();
        if (uriParams.containsKey("filter")) {
            condition = resourceHelper.getQueryConditionForDevice(uriInfo.getQueryParameters());
        } else {
            condition = resourceHelper.getQueryConditionForDevice(params);
        }
        Finder<Device> allDevicesFinder = deviceService.findAllDevices(condition);
        List<Long> allDevices = allDevicesFinder.from(queryParameters).stream().map(HasId::getId).collect(Collectors.toList());

        Optional<AdHocDeviceGroup>  adhocGroup = yellowfinGroupsService.cacheAdHocDeviceGroup(allDevices);
        if(adhocGroup.isPresent()){
            groupInfo.name = adhocGroup.get().getName();
        }
        return groupInfo;
    }

}
