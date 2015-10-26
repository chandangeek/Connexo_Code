package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.rest.util.ConcurrentModificationException;
import com.energyict.mdc.engine.config.security.Privileges;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.annotation.security.RolesAllowed;
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
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 15/11/13
 * Time: 12:11
 */
@Path("/devicediscoveryprotocols")
public class DeviceDiscoveryProtocolsResource {

    private final ProtocolPluggableService protocolPluggableService;
    private final ResourceHelper resourceHelper;


    @Inject
    public DeviceDiscoveryProtocolsResource(ProtocolPluggableService protocolPluggableService, ResourceHelper resourceHelper) {
        this.protocolPluggableService = protocolPluggableService;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION, Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION})
    public DeviceDiscoveryProtocolsInfo getDeviceDiscoveryProtocols() {
        DeviceDiscoveryProtocolsInfo deviceDiscoveryProtocolsInfo = new DeviceDiscoveryProtocolsInfo();
        deviceDiscoveryProtocolsInfo.deviceDiscoveryProtocolInfos =
                this.protocolPluggableService
                        .findAllInboundDeviceProtocolPluggableClass()
                        .stream()
                        .map(DeviceDiscoveryProtocolInfo::new)
                        .collect(Collectors.toSet());
        return deviceDiscoveryProtocolsInfo;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION, Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION})
    public DeviceDiscoveryProtocolInfo getDeviceDiscoveryProtocol(@PathParam("id") long id) {
        return new DeviceDiscoveryProtocolInfo(resourceHelper.findInboundDeviceProtocolPluggableClassOrThrowException(id));
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public Response deleteDeviceDiscoveryProtocol(@PathParam("id") long id, DeviceDiscoveryProtocolInfo info) {
        info.id = id;
        try {
            resourceHelper.lockInboundDeviceProtocolPluggableClassOrThrowException(info).delete();
        } catch (ConcurrentModificationException cme) {
            throw cme;
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public DeviceDiscoveryProtocolInfo createDeviceDiscoveryProtocol(DeviceDiscoveryProtocolInfo deviceDiscoveryProtocolInfo) throws WebApplicationException {
        try {
            InboundDeviceProtocolPluggableClass pluggableClass = this.protocolPluggableService.newInboundDeviceProtocolPluggableClass(deviceDiscoveryProtocolInfo.name, deviceDiscoveryProtocolInfo.javaClassName);
            return new DeviceDiscoveryProtocolInfo(pluggableClass);
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public DeviceDiscoveryProtocolInfo updateDeviceDiscoveryProtocol(@PathParam("id") long id, DeviceDiscoveryProtocolInfo info) throws WebApplicationException {
        try {
            InboundDeviceProtocolPluggableClass pluggableClass = resourceHelper.lockInboundDeviceProtocolPluggableClassOrThrowException(info);
            pluggableClass.setName(info.name);
            pluggableClass.save();
            return new DeviceDiscoveryProtocolInfo(pluggableClass);
        } catch (ConcurrentModificationException cme) {
            throw cme;
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }



}