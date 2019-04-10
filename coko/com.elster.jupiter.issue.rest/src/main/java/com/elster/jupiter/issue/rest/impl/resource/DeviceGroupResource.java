package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.response.device.DeviceGroupInfo;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;


import static com.elster.jupiter.util.conditions.Where.where;



@Path("/devicegroups")
public class DeviceGroupResource extends BaseResource{
    private final MeteringGroupsService meteringGroupsService;

    @Inject
    public DeviceGroupResource(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public PagedInfoList getDeviceGroups(@QueryParam("type") String typeName, @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParameters) {
        Query<EndDeviceGroup> query = getDeviceGroupQueryByType(typeName);
        Condition condition = buildCondition(filter);
        Order order = Order.ascending("upper(name)");
        List<EndDeviceGroup> endDeviceGroups;
        if (queryParameters.getStart().isPresent() && queryParameters.getLimit().isPresent()) {
            int from = queryParameters.getStart().get() + 1;
            int to = from + queryParameters.getLimit().get();
            endDeviceGroups = query.select(condition, from, to, order);
        } else {
            endDeviceGroups = query.select(condition, order);
        }
        List<DeviceGroupInfo> deviceGroupInfos = from(endDeviceGroups);
        return PagedInfoList.fromPagedList("devicegroups", deviceGroupInfos, queryParameters);
    }

    private Query<EndDeviceGroup> getDeviceGroupQueryByType(@QueryParam("type") String typeName) {
        if (QueryEndDeviceGroup.class.getSimpleName().equalsIgnoreCase(typeName)) {
            return meteringGroupsService.getQueryEndDeviceGroupQuery();
        } else {
            return meteringGroupsService.getEndDeviceGroupQuery();
        }
    }

    private Condition buildCondition(JsonQueryFilter filter) {
        Condition condition = Condition.TRUE;
        if (filter.hasProperty("name")) {
            condition = condition.and(where("name").isEqualTo(filter.getString("name")));
        }
        return condition;
    }

    private List<DeviceGroupInfo> from(List<EndDeviceGroup> endDeviceGroups) {
        return endDeviceGroups.stream()
                .map(this::basicFrom)
                .collect(Collectors.toList());
    }

    private DeviceGroupInfo basicFrom(EndDeviceGroup endDeviceGroup) {
        DeviceGroupInfo deviceGroupInfo = new DeviceGroupInfo();
        deviceGroupInfo.id = endDeviceGroup.getId();
        deviceGroupInfo.mRID = endDeviceGroup.getMRID();
        deviceGroupInfo.name = endDeviceGroup.getName();
        deviceGroupInfo.dynamic = endDeviceGroup.isDynamic();
        deviceGroupInfo.version = endDeviceGroup.getVersion();
        return deviceGroupInfo;
    }
}
