package com.energyict.mdc.device.data.rest.impl;


import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfos;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/devicegroups")
public class DeviceGroupResource {

    private final MeteringGroupsService meteringGroupsService;
    private final RestQueryService queryService;

    @Inject
    public DeviceGroupResource(MeteringGroupsService meteringGroupsService, RestQueryService queryService) {
        this.meteringGroupsService = meteringGroupsService;
        this.queryService = queryService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getDeviceGroups(@BeanParam QueryParameters queryParameters) {
        com.elster.jupiter.rest.util.QueryParameters koreQueryParameters =
                com.elster.jupiter.rest.util.QueryParameters.wrap(queryParameters.getQueryParameters());
        List<EndDeviceGroup> allDeviceGroups = queryEndDeviceGroups(koreQueryParameters);
        List<DeviceGroupInfo> deviceGroupInfos = DeviceGroupInfo.from(allDeviceGroups);
        return PagedInfoList.asJson("devicegroups", deviceGroupInfos, queryParameters);
    }

    private List<EndDeviceGroup> queryEndDeviceGroups(com.elster.jupiter.rest.util.QueryParameters queryParameters) {
        Query<EndDeviceGroup> query = meteringGroupsService.getEndDeviceGroupQuery();
        RestQuery<EndDeviceGroup> restQuery = queryService.wrap(query);
        return restQuery.select(queryParameters, Order.ascending("upper(name)"));
    }



}
