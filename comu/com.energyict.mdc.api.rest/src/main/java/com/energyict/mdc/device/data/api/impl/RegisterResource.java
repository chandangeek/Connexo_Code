package com.energyict.mdc.device.data.api.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.JsonQueryParameters;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.security.Privileges;
import java.util.List;
import javax.annotation.security.RolesAllowed;
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

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 4/29/15.
 */
@Path("/devices/{mRID}/registers")
public class RegisterResource {

    private final RegisterInfoFactory registerInfoFactory;
    private final DeviceService deviceService;

    @Inject
    public RegisterResource(RegisterInfoFactory registerInfoFactory, DeviceService deviceService) {
        this.registerInfoFactory = registerInfoFactory;
        this.deviceService = deviceService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response getRegisters(@PathParam("mRID") String mRID, @BeanParam JsonQueryParameters queryParameters) {
        Device device = deviceService.findByUniqueMrid(mRID).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
        List<RegisterInfo> registers = device.getRegisters().stream().map(registerInfoFactory::plain).collect(toList());
        return Response.ok(PagedInfoList.fromCompleteList("registers", registers, queryParameters)).build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response getRegister(@PathParam("mRID") String mRID, @PathParam("id") int id) {
        Device device = deviceService.findByUniqueMrid(mRID).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
        Register register = device.getRegisters().get(id);
        RegisterInfo info = registerInfoFactory.plain(register);
        return Response.ok(info).build();
    }

    @GET
    @Produces("application/h+json;charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response getHypermediaRegister(@PathParam("mRID") String mRID, @PathParam("id") int id, @Context UriInfo uriInfo) {
        Device device = deviceService.findByUniqueMrid(mRID).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
        Register register = device.getRegisters().get(id);
        RegisterInfo info = registerInfoFactory.asHypermedia(register, uriInfo);
        return Response.ok(info).build();

    }

    @GET
    @Produces("application/hal+json;charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response getHalRegister(@PathParam("mRID") String mRID, @PathParam("id") int id, @Context UriInfo uriInfo) {
        Device device = deviceService.findByUniqueMrid(mRID).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
        Register register = device.getRegisters().get(id);
        return Response.ok(registerInfoFactory.asHal(register, uriInfo)).build();
    }

}
