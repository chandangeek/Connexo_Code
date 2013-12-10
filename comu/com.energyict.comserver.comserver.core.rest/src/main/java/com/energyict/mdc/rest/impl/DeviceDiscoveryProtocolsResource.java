package com.energyict.mdc.rest.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.protocol.inbound.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.services.InboundDeviceProtocolPluggableClassService;
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
import java.sql.SQLException;

/**
 * Copyrights EnergyICT
 * Date: 15/11/13
 * Time: 12:11
 */
@Path("/devicediscoveryprotocols")
public class DeviceDiscoveryProtocolsResource {

    private final InboundDeviceProtocolPluggableClassService inboundDeviceProtocolPluggableClassService;

    @Inject
    public DeviceDiscoveryProtocolsResource(InboundDeviceProtocolPluggableClassService inboundDeviceProtocolPluggableClassService) {
        this.inboundDeviceProtocolPluggableClassService = inboundDeviceProtocolPluggableClassService;
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
            this.inboundDeviceProtocolPluggableClassService.delete(id);
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
            InboundDeviceProtocolPluggableClass pluggableClass = inboundDeviceProtocolPluggableClassService.create(deviceDiscoveryProtocolInfo.asShadow());
            return new DeviceDiscoveryProtocolInfo(this.inboundDeviceProtocolPluggableClassService.find(pluggableClass.getId()));
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
            InboundDeviceProtocolPluggableClass pluggableClass = inboundDeviceProtocolPluggableClassService.update(id, deviceDiscoveryProtocolInfo.asShadow());
            return new DeviceDiscoveryProtocolInfo(this.inboundDeviceProtocolPluggableClassService.find(pluggableClass.getId()));
        }
        catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}