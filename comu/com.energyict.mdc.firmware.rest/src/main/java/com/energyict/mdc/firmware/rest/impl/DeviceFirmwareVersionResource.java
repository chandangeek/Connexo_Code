package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.device.data.Device;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/device/{mRID}/firmwares")
public class DeviceFirmwareVersionResource {
    private final DeviceFirmwareVersionInfoFactory versionInfoFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public DeviceFirmwareVersionResource(DeviceFirmwareVersionInfoFactory versionInfoFactory, ResourceHelper resourceHelper) {
        this.versionInfoFactory = versionInfoFactory;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.VIEW_DEVICE, com.energyict.mdc.device.data.security.Privileges.OPERATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response getFirmwareVersionsOnDevice(@PathParam("mRID") String mRID) {
        Device device = resourceHelper.findDeviceByMridOrThrowException(mRID);
        return Response.ok(versionInfoFactory.from(device)).build();
    }

}