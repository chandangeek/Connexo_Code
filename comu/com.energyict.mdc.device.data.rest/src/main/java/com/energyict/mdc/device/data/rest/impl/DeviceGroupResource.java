package com.energyict.mdc.device.data.rest.impl;


import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.data.imp.DeviceImportService;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/devicegroups")
public class DeviceGroupResource {

    private final MeteringGroupsService meteringGroupsService;
    private final ResourceHelper resourceHelper;

    @Inject
    public DeviceGroupResource(ResourceHelper resourceHelper,
                               MeteringGroupsService meteringGroupsService) {
        this.resourceHelper = resourceHelper;
        this.meteringGroupsService = meteringGroupsService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getDeviceGroups(@BeanParam QueryParameters queryParameters) {
        List<EndDeviceGroup> allDevicesGroups = meteringGroupsService.findEndDeviceGroups();
        List<DeviceGroupInfo> deviceGroupsInfos = DeviceGroupInfo.from(allDevicesGroups);
        return PagedInfoList.asJson("devicegroups", deviceGroupsInfos, queryParameters);
    }

}
