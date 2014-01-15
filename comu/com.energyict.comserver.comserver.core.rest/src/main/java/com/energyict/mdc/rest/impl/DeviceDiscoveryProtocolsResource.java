package com.energyict.mdc.rest.impl;

import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Copyrights EnergyICT
 * Date: 15/11/13
 * Time: 12:11
 */
@Path("/devicediscoveryprotocols")
public class DeviceDiscoveryProtocolsResource {

    private final ProtocolPluggableService protocolPluggableService;

    @Inject
    public DeviceDiscoveryProtocolsResource(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceDiscoveryProtocolsInfo getDeviceDiscoveryProtocols() {
        DeviceDiscoveryProtocolsInfo deviceDiscoveryProtocolsInfo = new DeviceDiscoveryProtocolsInfo();
        for (InboundDeviceProtocolPluggableClass inboundDeviceProtocolPluggableClass : this.protocolPluggableService.findAllInboundDeviceProtocolPluggableClass()) {
            deviceDiscoveryProtocolsInfo.deviceDiscoveryProtocolInfos.add(new DeviceDiscoveryProtocolInfo(inboundDeviceProtocolPluggableClass));
        }
        return deviceDiscoveryProtocolsInfo;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceDiscoveryProtocolInfo getDeviceDiscoveryProtocol(@PathParam("id") long id) {
        return new DeviceDiscoveryProtocolInfo(this.protocolPluggableService.findInboundDeviceProtocolPluggableClass(id));
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDeviceDiscoveryProtocol(@PathParam("id") long id) {
        try {
            this.protocolPluggableService.deleteInboundDeviceProtocolPluggableClass(id);
        }
        catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceDiscoveryProtocolInfo createDeviceDiscoveryProtocol(DeviceDiscoveryProtocolInfo deviceDiscoveryProtocolInfo) throws WebApplicationException {
        try {
            InboundDeviceProtocolPluggableClass pluggableClass = this.protocolPluggableService.newInboundDeviceProtocolPluggableClass(deviceDiscoveryProtocolInfo.name, deviceDiscoveryProtocolInfo.javaClassName);
            return new DeviceDiscoveryProtocolInfo(pluggableClass);
        }
        catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceDiscoveryProtocolInfo updateDeviceDiscoveryProtocol(@PathParam("id") int id, DeviceDiscoveryProtocolInfo deviceDiscoveryProtocolInfo) throws WebApplicationException {
        try {
            InboundDeviceProtocolPluggableClass pluggableClass = protocolPluggableService.findInboundDeviceProtocolPluggableClass(id);
            pluggableClass.setName(deviceDiscoveryProtocolInfo.name);
            pluggableClass.save();
            return new DeviceDiscoveryProtocolInfo(pluggableClass);
        }
        catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}