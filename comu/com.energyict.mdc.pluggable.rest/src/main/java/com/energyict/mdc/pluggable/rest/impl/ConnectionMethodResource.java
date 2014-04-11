package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.PartialConnectionTask;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/connectionmethods")
public class ConnectionMethodResource {

    private DeviceConfigurationService deviceConfigurationService;

    @Inject
    public ConnectionMethodResource(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{deviceConfigId}")
    public PagedInfoList getConnectionTypeForDeviceConfiguration(@PathParam("deviceConfigId") long deviceConfigId, @BeanParam QueryParameters queryParameters, @Context UriInfo uriInfo) {
        DeviceConfiguration deviceConfiguration = deviceConfigurationService.findDeviceConfiguration(deviceConfigId);
        if (deviceConfiguration==null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        List<ConnectionMethodInfo> connectionMethodInfos = new ArrayList<>();
        for (PartialConnectionTask partialConnectionTask : deviceConfiguration.getPartialConnectionTasks()) {
            connectionMethodInfos.add(ConnectionMethodInfo.from(partialConnectionTask, uriInfo));
        }
        List<ConnectionMethodInfo> pagedConnectionMethodInfos = ListPager.of(connectionMethodInfos).from(queryParameters).find();
        return PagedInfoList.asJson("connectionMethods", pagedConnectionMethodInfos, queryParameters);
    }
}
