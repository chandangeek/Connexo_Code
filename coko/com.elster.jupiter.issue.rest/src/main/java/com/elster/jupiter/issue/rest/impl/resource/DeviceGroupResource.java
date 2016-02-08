package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.issue.rest.response.DeviceGroupInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.stream.Collectors;

@Path("/devicegroups")
public class DeviceGroupResource extends BaseResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE,Privileges.Constants.ASSIGN_ISSUE,Privileges.Constants.CLOSE_ISSUE,Privileges.Constants.COMMENT_ISSUE,Privileges.Constants.ACTION_ISSUE})
    public Response getDeviceGroups(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters params) {
        List<DeviceGroupInfo> infos = queryEndDeviceGroups().stream().map(DeviceGroupInfo::new).collect(Collectors.toList());
        return Response.ok(infos).build();
    }

    private List<EndDeviceGroup> queryEndDeviceGroups() {
        return getMeteringGroupsService().getEndDeviceGroupQuery().select(Condition.TRUE, Order.ascending("upper(name)"));
    }
}
