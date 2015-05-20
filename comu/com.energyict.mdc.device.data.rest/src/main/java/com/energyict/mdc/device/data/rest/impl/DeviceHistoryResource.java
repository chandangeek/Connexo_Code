package com.energyict.mdc.device.data.rest.impl;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.elster.jupiter.rest.util.QueryParameters;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.impl.DeviceLifeCycleStateHistoryInfoFactory;
import com.energyict.mdc.device.lifecycle.config.Privileges;

public class DeviceHistoryResource {

    private ResourceHelper resourceHelper;
    private DeviceLifeCycleStateHistoryInfoFactory deviceLifeCycleStatesHistoryInfoFactory;

    @Inject
    public DeviceHistoryResource(ResourceHelper resourceHelper, DeviceLifeCycleStateHistoryInfoFactory deviceLifeCycleStatesHistoryInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.deviceLifeCycleStatesHistoryInfoFactory = deviceLifeCycleStatesHistoryInfoFactory;
    }

    @GET
    @Path("/devicelifecyclestates")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({ Privileges.VIEW_DEVICE_LIFE_CYCLE, Privileges.CONFIGURE_DEVICE_LIFE_CYCLE })
    public Response getDeviceLifeCycleStatesHistory(@PathParam("mRID") String mRID, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        return Response.ok(deviceLifeCycleStatesHistoryInfoFactory.asInfo(device.getStateTimeline())).build();
    }

}
