package com.energyict.mdc.device.data.rest.impl;


import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;

import java.util.LinkedHashMap;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/devicegroups")
public class DeviceGroupResource {

    private final MeteringGroupsService meteringGroupsService;
    private final RestQueryService restQueryService;
    private final DeviceConfigurationService deviceConfigurationService;;

    @Inject
    public DeviceGroupResource(MeteringGroupsService meteringGroupsService, DeviceConfigurationService deviceConfigurationService, RestQueryService restQueryService) {
        this.meteringGroupsService = meteringGroupsService;
        this.deviceConfigurationService = deviceConfigurationService;
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

    private List<EndDeviceGroup> queryEndDeviceGroups(com.elster.jupiter.rest.util.QueryParameters queryParameters) {
        Query<EndDeviceGroup> query = meteringGroupsService.getEndDeviceGroupQuery();
        RestQuery<EndDeviceGroup> restQuery = restQueryService.wrap(query);
        return restQuery.select(queryParameters, Order.ascending("upper(name)"));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createDeviceGroup(DeviceGroupInfo deviceGroupInfo) {
        //String groupMRID = deviceGroupInfo.mRID;
        String name = deviceGroupInfo.name;
        boolean dynamic = deviceGroupInfo.dynamic;
        LinkedHashMap filter = (LinkedHashMap) deviceGroupInfo.filter;
        String mRID = (String) filter.get("mRID");
        Condition condition = Condition.TRUE;
        if ((mRID != null) && (!"".equals(mRID))) {
            condition = condition.and(where("mRID").isEqualTo(mRID));
        }

        String serialNumber = (String) filter.get("serialNumber");
        if ((serialNumber != null) && (!"".equals(serialNumber))) {
            condition = condition.and(where("serialNumber").isEqualTo(serialNumber));
        }

        List<Integer> deviceTypes = (List) filter.get("deviceTypes");
        if ((deviceTypes != null) && (!deviceTypes.isEmpty())) {
            for (int deviceTypeId : deviceTypes) {
                DeviceType deviceType = deviceConfigurationService.findDeviceType(deviceTypeId);
                if (deviceType != null) {
                    condition = condition.and(where("deviceConfiguration.deviceType.name").isEqualTo(deviceType.getName()));
                }
            }
        }

        List<Integer> deviceConfigurations = (List) filter.get("deviceConfigurations");
        if ((deviceConfigurations != null) && (!deviceConfigurations.isEmpty())) {
            for (int deviceConfigurationId : deviceConfigurations) {
                DeviceConfiguration deviceConfiguration = deviceConfigurationService.findDeviceConfiguration(deviceConfigurationId);
                if (deviceConfiguration != null) {
                    condition = condition.and(where("deviceConfiguration.name").isEqualTo(deviceConfiguration.getName()));
                }
            }
        }

        EndDeviceGroup endDeviceGroup;
        if (dynamic) {
            endDeviceGroup = meteringGroupsService.createQueryEndDeviceGroup(condition);
            endDeviceGroup.setName(name);
            endDeviceGroup.setQueryProviderName("com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider");
        } else {
            endDeviceGroup = meteringGroupsService.createEnumeratedEndDeviceGroup(name);
        }
        endDeviceGroup.save();


        return Response.ok().build();
    }



}
