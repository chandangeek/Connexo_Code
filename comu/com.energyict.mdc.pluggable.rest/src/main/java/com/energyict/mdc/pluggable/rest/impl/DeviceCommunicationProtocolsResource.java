package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.energyict.mdc.common.rest.FieldValidationException;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.engine.config.security.Privileges;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.LicensedProtocol;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 05/11/13
 * Time: 12:29
 */
@Path("/devicecommunicationprotocols")
public class DeviceCommunicationProtocolsResource {

    private final ProtocolPluggableService protocolPluggableService;
    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public DeviceCommunicationProtocolsResource(ProtocolPluggableService protocolPluggableService, MdcPropertyUtils mdcPropertyUtils) {
        this.protocolPluggableService = protocolPluggableService;
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION, Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION})
    public PagedInfoList getDeviceCommunicationProtocols(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        List<DeviceProtocolPluggableClass> deviceProtocolPluggableClasses = this.protocolPluggableService.findAllDeviceProtocolPluggableClasses().from(queryParameters).find();
        List<DeviceCommunicationProtocolInfo> deviceCommunicationProtocolInfos = new ArrayList<>(deviceProtocolPluggableClasses.size());
        for (DeviceProtocolPluggableClass deviceProtocolPluggableClass : deviceProtocolPluggableClasses) {
            LicensedProtocol licensedProtocol = this.protocolPluggableService.findLicensedProtocolFor(deviceProtocolPluggableClass);
            deviceCommunicationProtocolInfos.add(new DeviceCommunicationProtocolInfo(uriInfo, deviceProtocolPluggableClass, licensedProtocol, false, mdcPropertyUtils));
        }
        return PagedInfoList.fromPagedList("DeviceProtocolPluggableClass", deviceCommunicationProtocolInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION, Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION})
    public DeviceCommunicationProtocolInfo getDeviceCommunicationProtocol(@Context UriInfo uriInfo, @PathParam("id") long id) {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = findDeviceProtocolPluggableClassOrThrowException(id);
        LicensedProtocol licensedProtocol = this.protocolPluggableService.findLicensedProtocolFor(deviceProtocolPluggableClass);
        return new DeviceCommunicationProtocolInfo(uriInfo, deviceProtocolPluggableClass, licensedProtocol, true, mdcPropertyUtils);
    }

    @GET
    @Path("/connectiontypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION, Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_TYPE, com.energyict.mdc.device.config.security.Privileges.Constants.ADMINISTRATE_DEVICE_TYPE})
    public List<ConnectionTypeInfo> getAllConnectionTypes(@Context UriInfo uriInfo, @BeanParam JsonQueryFilter queryFilter) {
        return this.protocolPluggableService.findAllConnectionTypePluggableClasses().stream()
                .map(p -> ConnectionTypeInfo.from(p, uriInfo, mdcPropertyUtils))
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{deviceProtocolId}/connectiontypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION, Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_TYPE, com.energyict.mdc.device.config.security.Privileges.Constants.ADMINISTRATE_DEVICE_TYPE})
    public List<ConnectionTypeInfo> getSupportedConnectionTypes(@PathParam("deviceProtocolId") long deviceProtocolId, @Context UriInfo uriInfo, @BeanParam JsonQueryFilter queryFilter) {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = findDeviceProtocolPluggableClassOrThrowException(deviceProtocolId);
        List<ConnectionType> supportedConnectionTypes = deviceProtocolPluggableClass.getDeviceProtocol().getSupportedConnectionTypes();
        List<ConnectionTypePluggableClass> allConnectionTypePluggableClassesToCheck = this.protocolPluggableService.findAllConnectionTypePluggableClasses();
        List<ConnectionTypeInfo> infos = new ArrayList<>();
        ConnectionType.Direction direction = ConnectionType.Direction.fromString(queryFilter.getString("direction"));
        for (ConnectionType supportedConnectionType : supportedConnectionTypes) {
            if (ConnectionType.Direction.NULL.equals(direction) || supportedConnectionType.getDirection().equals(direction)) {
                for (ConnectionTypePluggableClass registeredConnectionTypePluggableClass : allConnectionTypePluggableClassesToCheck) {
                    if (registeredConnectionTypePluggableClass.getJavaClassName().equals(supportedConnectionType.getClass().getCanonicalName())) {
                        infos.add(ConnectionTypeInfo.from(registeredConnectionTypePluggableClass, uriInfo, mdcPropertyUtils));
                    }
                }
            }
        }
        return infos;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public Response deleteDeviceCommunicationProtocol(@PathParam("id") long id) {
        try {
            this.protocolPluggableService.deleteDeviceProtocolPluggableClass(id);
        } catch (Exception e) {
            throw new WebApplicationException(Response.serverError().build());
        }
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public DeviceCommunicationProtocolInfo createDeviceCommunicationProtocol(@Context final UriInfo uriInfo, final DeviceCommunicationProtocolInfo deviceCommunicationProtocolInfo) throws WebApplicationException {
        try {
            DeviceProtocolPluggableClass deviceProtocolPluggableClass =
                    protocolPluggableService.newDeviceProtocolPluggableClass(
                            deviceCommunicationProtocolInfo.licensedProtocol.protocolName,
                            deviceCommunicationProtocolInfo.licensedProtocol.protocolJavaClassName);
            deviceCommunicationProtocolInfo.copyProperties(deviceProtocolPluggableClass, mdcPropertyUtils);
            deviceProtocolPluggableClass.save();
            LicensedProtocol licensedProtocol = protocolPluggableService.findLicensedProtocolFor(deviceProtocolPluggableClass);
            return new DeviceCommunicationProtocolInfo(uriInfo, deviceProtocolPluggableClass, licensedProtocol, true, mdcPropertyUtils);
        } catch (FieldValidationException fieldValidationException) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_VALUE, "properties." + fieldValidationException.getFieldName());
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public DeviceCommunicationProtocolInfo updateDeviceCommunicationProtocol(@Context final UriInfo uriInfo, @PathParam("id") final long id, final DeviceCommunicationProtocolInfo deviceCommunicationProtocolInfo) {
        try {
            DeviceProtocolPluggableClass deviceProtocolPluggableClass = findDeviceProtocolPluggableClassOrThrowException(id);
            deviceProtocolPluggableClass.setName(deviceCommunicationProtocolInfo.name);
            deviceCommunicationProtocolInfo.copyProperties(deviceProtocolPluggableClass, mdcPropertyUtils);
            deviceProtocolPluggableClass.save();
            LicensedProtocol licensedProtocol = protocolPluggableService.findLicensedProtocolFor(deviceProtocolPluggableClass);
            return new DeviceCommunicationProtocolInfo(uriInfo, findDeviceProtocolPluggableClassOrThrowException(id), licensedProtocol, true, mdcPropertyUtils);
        } catch (FieldValidationException fieldValidationException) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_VALUE, "properties." + fieldValidationException.getFieldName());
        }
    }

    private DeviceProtocolPluggableClass findDeviceProtocolPluggableClassOrThrowException(long id) {
        return this.protocolPluggableService
                .findDeviceProtocolPluggableClass(id)
                .orElseThrow(() -> new WebApplicationException(
                        "No device protocol with id " + id + " found",
                        Response.status(Response.Status.NOT_FOUND).entity("No device protocol with id " + id + " found").build()));
    }

}