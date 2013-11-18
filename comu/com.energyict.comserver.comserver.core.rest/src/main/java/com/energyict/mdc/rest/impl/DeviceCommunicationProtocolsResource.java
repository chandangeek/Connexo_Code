package com.energyict.mdc.rest.impl;

import com.energyict.mdc.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.services.DeviceProtocolPluggableClassService;
import com.energyict.mdc.services.DeviceProtocolService;
import com.energyict.mdw.core.PluggableClass;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Copyrights EnergyICT
 * Date: 05/11/13
 * Time: 12:29
 */
@Path("/devicecommunicationprotocols")
public class DeviceCommunicationProtocolsResource {

    private final DeviceProtocolService deviceProtocolService;
    private final DeviceProtocolPluggableClassService deviceProtocolPluggableClassService;

    public DeviceCommunicationProtocolsResource(@Context Application application) {
        this.deviceProtocolPluggableClassService = ((MdcApplication) ((ResourceConfig) application).getApplication()).getDeviceProtocolPluggableClassService();
        this.deviceProtocolService = ((MdcApplication) ((ResourceConfig) application).getApplication()).getDeviceProtocolService();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceCommunicationProtocolsInfo getDeviceCommunicationProtocols() {
        DeviceCommunicationProtocolsInfo deviceCommunicationProtocolInfos = new DeviceCommunicationProtocolsInfo();
        for (DeviceProtocolPluggableClass deviceProtocolPluggableClass : this.deviceProtocolPluggableClassService.findAll()) {
            deviceCommunicationProtocolInfos.deviceCommunicationProtocolInfos.add(new DeviceCommunicationProtocolInfo(deviceProtocolPluggableClass));
        }
        return deviceCommunicationProtocolInfos;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceCommunicationProtocolInfo getDeviceCommunicationProtocol(@PathParam("id") int id) {
        return new DeviceCommunicationProtocolInfo(this.deviceProtocolPluggableClassService.find(id));
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDeviceCommunicationProtocol(@PathParam("id") int id) {
        try {
            this.deviceProtocolService.delete(id);
        } catch (Exception e) {
            throw new WebApplicationException(Response.serverError().build());
        }
        return Response.ok().build();

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceCommunicationProtocolInfo createDeviceCommunicationProtocol(DeviceCommunicationProtocolInfo deviceCommunicationProtocolInfo) throws WebApplicationException {
        try {
            PluggableClass pluggableClass = deviceProtocolService.create(deviceCommunicationProtocolInfo.asShadow());
            //TODO check if we just can't return the object we received
            return new DeviceCommunicationProtocolInfo(this.deviceProtocolPluggableClassService.find(deviceCommunicationProtocolInfo.id));
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceCommunicationProtocolInfo updateDeviceCommunicationProtocol(@PathParam("id") int id, DeviceCommunicationProtocolInfo deviceCommunicationProtocolInfo) {
        try {
            PluggableClass pluggableClass = deviceProtocolService.update(id, deviceCommunicationProtocolInfo.asShadow());
            //TODO check if we just can't return the object we received
            return new DeviceCommunicationProtocolInfo(this.deviceProtocolPluggableClassService.find(deviceCommunicationProtocolInfo.id));
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
}
