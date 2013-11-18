package com.energyict.mdc.rest.impl;

import com.energyict.mdc.protocol.inbound.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.services.InboundDeviceProtocolPluggableClassService;
import com.energyict.mdc.services.InboundDeviceProtocolService;
import com.energyict.mdw.core.PluggableClass;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
 * Date: 15/11/13
 * Time: 12:11
 */
@Path("/devicediscoveryprotocols")
public class DeviceDiscoveryProtocolsResource {

    private final InboundDeviceProtocolService inboundDeviceProtocolService;
    private final InboundDeviceProtocolPluggableClassService inboundDeviceProtocolPluggableClassService;

    public DeviceDiscoveryProtocolsResource(@Context Application application) {
        this.inboundDeviceProtocolPluggableClassService = ((MdcApplication) ((ResourceConfig) application).getApplication()).getInboundDeviceProtocolPluggableClassService();
        this.inboundDeviceProtocolService = ((MdcApplication) ((ResourceConfig) application).getApplication()).getInboundDeviceProtocolService();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceDiscoveryProtocolsInfo getDeviceDiscoveryProtocols() {
        DeviceDiscoveryProtocolsInfo deviceDiscoveryProtocolsInfo = new DeviceDiscoveryProtocolsInfo();
        for (InboundDeviceProtocolPluggableClass inboundDeviceProtocolPluggableClass : this.inboundDeviceProtocolPluggableClassService.findAll()) {
            deviceDiscoveryProtocolsInfo.deviceDiscoveryProtocolInfos.add(new DeviceDiscoveryProtocolInfo(inboundDeviceProtocolPluggableClass));
        }
        return deviceDiscoveryProtocolsInfo;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceDiscoveryProtocolInfo getDeviceDiscoveryProtocol(@PathParam("id") int id) {
        return new DeviceDiscoveryProtocolInfo(this.inboundDeviceProtocolPluggableClassService.find(id));
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDeviceDiscoveryProtocol(@PathParam("id") int id) {
        try {
            this.inboundDeviceProtocolService.delete(id);
        } catch (Exception e) {
            throw new WebApplicationException(Response.serverError().build());
        }
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceDiscoveryProtocolInfo createDeviceDiscoveryProtocol(DeviceDiscoveryProtocolInfo deviceDiscoveryProtocolInfo) throws WebApplicationException {
        try {
            PluggableClass pluggableClass = inboundDeviceProtocolService.create(deviceDiscoveryProtocolInfo.asShadow());
            //TODO check if we just can't return the object we received
            return new DeviceDiscoveryProtocolInfo(this.inboundDeviceProtocolPluggableClassService.find(deviceDiscoveryProtocolInfo.id));
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceDiscoveryProtocolInfo updateDeviceDiscoveryProtocol(@PathParam("id") int id, DeviceDiscoveryProtocolInfo deviceDiscoveryProtocolInfo) throws WebApplicationException {
        try {
            PluggableClass pluggableClass = inboundDeviceProtocolService.update(id, deviceDiscoveryProtocolInfo.asShadow());
            //TODO check if we just can't return the object we received
            return new DeviceDiscoveryProtocolInfo(this.inboundDeviceProtocolPluggableClassService.find(deviceDiscoveryProtocolInfo.id));
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}
