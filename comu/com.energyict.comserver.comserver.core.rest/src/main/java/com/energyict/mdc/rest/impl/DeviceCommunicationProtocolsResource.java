package com.energyict.mdc.rest.impl;

import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.LicensedProtocol;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.services.DeviceProtocolPluggableClassService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Copyrights EnergyICT
 * Date: 05/11/13
 * Time: 12:29
 */
@Path("/devicecommunicationprotocols")
public class DeviceCommunicationProtocolsResource {

    private final DeviceProtocolPluggableClassService deviceProtocolPluggableClassService;
    private final LicensedProtocolService licensedProtocolService;

    @Inject
    public DeviceCommunicationProtocolsResource(DeviceProtocolPluggableClassService deviceProtocolPluggableClassService, LicensedProtocolService licensedProtocolService) {
        this.deviceProtocolPluggableClassService = deviceProtocolPluggableClassService;
        this.licensedProtocolService = licensedProtocolService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceCommunicationProtocolsInfo getDeviceCommunicationProtocols(@Context UriInfo uriInfo) {
        DeviceCommunicationProtocolsInfo deviceCommunicationProtocolInfos = new DeviceCommunicationProtocolsInfo();
        for (DeviceProtocolPluggableClass deviceProtocolPluggableClass : this.deviceProtocolPluggableClassService.findAll()) {
            LicensedProtocol licensedProtocol = this.licensedProtocolService.findLicensedProtocolFor(deviceProtocolPluggableClass);
            deviceCommunicationProtocolInfos.deviceCommunicationProtocolInfos.add(new DeviceCommunicationProtocolInfo(uriInfo, deviceProtocolPluggableClass, licensedProtocol, false));
        }
        return deviceCommunicationProtocolInfos;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceCommunicationProtocolInfo getDeviceCommunicationProtocol(@Context UriInfo uriInfo, @PathParam("id") int id) {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = this.deviceProtocolPluggableClassService.find(id);
        LicensedProtocol licensedProtocol = this.licensedProtocolService.findLicensedProtocolFor(deviceProtocolPluggableClass);
        return new DeviceCommunicationProtocolInfo(uriInfo, deviceProtocolPluggableClass, licensedProtocol, true);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDeviceCommunicationProtocol(@PathParam("id") int id) {
        try {
            this.deviceProtocolPluggableClassService.delete(id);
        }
        catch (Exception e) {
            throw new WebApplicationException(Response.serverError().build());
        }
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceCommunicationProtocolInfo createDeviceCommunicationProtocol(@Context UriInfo uriInfo, DeviceCommunicationProtocolInfo deviceCommunicationProtocolInfo) throws WebApplicationException {
        try {
            DeviceProtocolPluggableClass deviceProtocolPluggableClass = this.deviceProtocolPluggableClassService.create(deviceCommunicationProtocolInfo.asShadow());
            LicensedProtocol licensedProtocol = this.licensedProtocolService.findLicensedProtocolFor(deviceProtocolPluggableClass);
            return new DeviceCommunicationProtocolInfo(uriInfo, deviceProtocolPluggableClass, licensedProtocol, true);
        }
        catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceCommunicationProtocolInfo updateDeviceCommunicationProtocol(@Context UriInfo uriInfo, @PathParam("id") int id, DeviceCommunicationProtocolInfo deviceCommunicationProtocolInfo) {
        try {
            DeviceProtocolPluggableClass deviceProtocolPluggableClass = deviceProtocolPluggableClassService.update(id, deviceCommunicationProtocolInfo.asShadow());
            LicensedProtocol licensedProtocol = this.licensedProtocolService.findLicensedProtocolFor(deviceProtocolPluggableClass);
            return new DeviceCommunicationProtocolInfo(uriInfo, deviceProtocolPluggableClass, licensedProtocol, true);
        }
        catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}