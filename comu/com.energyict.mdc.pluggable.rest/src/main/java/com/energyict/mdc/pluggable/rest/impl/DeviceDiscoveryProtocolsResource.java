package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.rest.util.ConcurrentModificationException;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.engine.config.security.Privileges;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
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
    private final MdcPropertyUtils mdcPropertyUtils;


    @Inject
    public DeviceDiscoveryProtocolsResource(ProtocolPluggableService protocolPluggableService, ResourceHelper resourceHelper, MdcPropertyUtils mdcPropertyUtils) {
        this.protocolPluggableService = protocolPluggableService;
        this.resourceHelper = resourceHelper;
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION, Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION})
    public DeviceDiscoveryProtocolsInfo getDeviceDiscoveryProtocols(@Context UriInfo uriInfo) {
        DeviceDiscoveryProtocolsInfo deviceDiscoveryProtocolsInfo = new DeviceDiscoveryProtocolsInfo();
        deviceDiscoveryProtocolsInfo.deviceDiscoveryProtocolInfos =
                this.protocolPluggableService
                        .findAllInboundDeviceProtocolPluggableClass()
                        .stream()
                        .map(inboundDeviceProtocolPluggableClass -> new DeviceDiscoveryProtocolInfo(inboundDeviceProtocolPluggableClass, uriInfo, mdcPropertyUtils))
                        .collect(Collectors.toSet());
        return deviceDiscoveryProtocolsInfo;
    }

    @GET @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION, Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION})
    public DeviceDiscoveryProtocolInfo getDeviceDiscoveryProtocol(@PathParam("id") long id, @Context UriInfo uriInfo) {
        return new DeviceDiscoveryProtocolInfo(resourceHelper.findInboundDeviceProtocolPluggableClassOrThrowException(id), uriInfo, mdcPropertyUtils);
    }

    @DELETE @Transactional
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

    @POST @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public DeviceDiscoveryProtocolInfo createDeviceDiscoveryProtocol(@Context UriInfo uriInfo, DeviceDiscoveryProtocolInfo deviceDiscoveryProtocolInfo) throws WebApplicationException {
        try {
            InboundDeviceProtocolPluggableClass pluggableClass = this.protocolPluggableService.newInboundDeviceProtocolPluggableClass(deviceDiscoveryProtocolInfo.name, deviceDiscoveryProtocolInfo.javaClassName);
            return new DeviceDiscoveryProtocolInfo(pluggableClass, uriInfo, mdcPropertyUtils);
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT @Transactional
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public DeviceDiscoveryProtocolInfo updateDeviceDiscoveryProtocol(@Context UriInfo uriInfo, @PathParam("id") long id, DeviceDiscoveryProtocolInfo info) throws WebApplicationException {
        try {
            InboundDeviceProtocolPluggableClass pluggableClass = resourceHelper.lockInboundDeviceProtocolPluggableClassOrThrowException(info);
            pluggableClass.setName(info.name);
            pluggableClass.save();
            return new DeviceDiscoveryProtocolInfo(pluggableClass, uriInfo, mdcPropertyUtils);
        } catch (ConcurrentModificationException cme) {
            throw cme;
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }



}