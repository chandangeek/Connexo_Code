package com.elster.insight.usagepoint.data.rest.impl;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;

@Path("/usagepointgroups")
public class UsagePointGroupResource {
    private final MeteringGroupsService meteringGroupsService;
    private final UsagePointGroupInfoFactory usagePointGroupInfoFactory;

    @Inject
    public UsagePointGroupResource(MeteringGroupsService meteringGroupsService, UsagePointGroupInfoFactory usagePointGroupInfoFactory) {
        this.meteringGroupsService = meteringGroupsService;
        this.usagePointGroupInfoFactory = usagePointGroupInfoFactory;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @Consumes(MediaType.APPLICATION_JSON)
    // not protected by privileges yet because a combo-box containing all the groups needs to be shown when creating an export task
    public PagedInfoList getDeviceGroups(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        List<UsagePointGroup> allUsagePointGroups = meteringGroupsService.findUsagePointGroups();
        List<UsagePointGroupInfo> usagePointGroupInfos = usagePointGroupInfoFactory.from(allUsagePointGroups);
        return PagedInfoList.fromPagedList("usagepointgroups", usagePointGroupInfos, queryParameters);
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_GROUP, Privileges.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, Privileges.VIEW_DEVICE_GROUP_DETAIL})
    public UsagePointGroupInfo getDeviceGroup(@PathParam("id") long id) {
        return usagePointGroupInfoFactory.from(fetchUsagePointGroup(id));
    }

    @GET
    @Path("/{id}/usagepoints")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed({ Privileges.ADMINISTRATE_DEVICE_GROUP, Privileges.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, Privileges.VIEW_DEVICE_GROUP_DETAIL })
    public PagedInfoList getUsagePoints(@PathParam("id") long deviceGroupId, @BeanParam JsonQueryParameters queryParameters) {
        UsagePointGroup usagePointGroup = fetchUsagePointGroup(deviceGroupId);
        List<UsagePointGroupMemberInfo> usagePointsInfos = usagePointGroup
                .getMembers(Instant.now())
                .stream()
                .map(usagePoint -> UsagePointGroupMemberInfo.from(usagePoint))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("usagepoints", usagePointsInfos, queryParameters);
    }

    private UsagePointGroup fetchUsagePointGroup(long id) {
        return meteringGroupsService.findUsagePointGroup(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }
}