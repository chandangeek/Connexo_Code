package com.energyict.mdc.device.data.rest.impl;


import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/devicegroups")
public class DeviceGroupResource {

    private final MeteringGroupsService meteringGroupsService;
    private final RestQueryService restQueryService;

    @Inject
    public DeviceGroupResource(MeteringGroupsService meteringGroupsService, RestQueryService restQueryService) {
        this.meteringGroupsService = meteringGroupsService;
        this.restQueryService = restQueryService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PagedInfoList getDeviceGroups(@BeanParam QueryParameters queryParameters, @QueryParam("type") String typeName) {
        com.elster.jupiter.rest.util.QueryParameters koreQueryParameters =
                com.elster.jupiter.rest.util.QueryParameters.wrap(queryParameters.getQueryParameters());
        Query<EndDeviceGroup> query;
        if (QueryEndDeviceGroup.class.getSimpleName().equalsIgnoreCase(typeName)) {
            query = meteringGroupsService.getQueryEndDeviceGroupQuery();
        } else {
            query = meteringGroupsService.getEndDeviceGroupQuery();
        }
        RestQuery<EndDeviceGroup> restQuery = restQueryService.wrap(query);
        List<EndDeviceGroup> allDeviceGroups = restQuery.select(koreQueryParameters, Order.ascending("upper(name)"));
        List<DeviceGroupInfo> deviceGroupInfos = DeviceGroupInfo.from(allDeviceGroups);
        return PagedInfoList.asJson("devicegroups", deviceGroupInfos, queryParameters);
    }


}
