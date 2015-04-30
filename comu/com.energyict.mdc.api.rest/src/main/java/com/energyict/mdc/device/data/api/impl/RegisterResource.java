package com.energyict.mdc.device.data.api.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.security.Privileges;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Created by bvn on 4/29/15.
 */
public class RegisterResource {


    private Device device;

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response getRegisters(@PathParam("id") int id) {
        Register register = device.getRegisters().get(id);
        RegisterInfo info = RegisterInfo.from(register);
        return Response.ok(info).build();

    }

    @GET
    @Produces("application/hal+json; charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response getHalRegisters(@PathParam("id") int id, @Context UriInfo uriInfo) {
        Register register = device.getRegisters().get(id);
        RegisterInfo info = RegisterInfo.from(register);
        HalInfo.wrap(info, info.uri(uriInfo));
        return Response.ok(info).build();

    }

    public RegisterResource init(Device device) {
        this.device = device;
        return this;
    }
}
