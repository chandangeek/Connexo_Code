package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.ConcurrentModificationException;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.rest.FieldValidationException;
import com.energyict.mdc.engine.config.security.Privileges;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.LicensedProtocol;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
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
    private final ResourceHelper resourceHelper;

    @Inject
    public DeviceCommunicationProtocolsResource(ProtocolPluggableService protocolPluggableService, MdcPropertyUtils mdcPropertyUtils, ResourceHelper resourceHelper) {
        this.protocolPluggableService = protocolPluggableService;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.resourceHelper = resourceHelper;
    }

    @GET @Transactional
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

    @GET @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION, Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION})
    public DeviceCommunicationProtocolInfo getDeviceCommunicationProtocol(@Context UriInfo uriInfo, @PathParam("id") long id) {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = resourceHelper.findDeviceProtocolPluggableClassByMrIdOrThrowException(id);
        LicensedProtocol licensedProtocol = this.protocolPluggableService.findLicensedProtocolFor(deviceProtocolPluggableClass);
        return new DeviceCommunicationProtocolInfo(uriInfo, deviceProtocolPluggableClass, licensedProtocol, true, mdcPropertyUtils);
    }

    @GET @Transactional
    @Path("/connectiontypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION, Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_TYPE, com.energyict.mdc.device.config.security.Privileges.Constants.ADMINISTRATE_DEVICE_TYPE})
    public List<ConnectionTypeInfo> getAllConnectionTypes(@Context UriInfo uriInfo, @BeanParam JsonQueryFilter queryFilter) {
        return this.protocolPluggableService.findAllConnectionTypePluggableClasses().stream()
                .map(p -> ConnectionTypeInfo.from(p, uriInfo, mdcPropertyUtils))
                .collect(Collectors.toList());
    }

    @GET @Transactional
    @Path("/{deviceProtocolId}/connectiontypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION, Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_TYPE, com.energyict.mdc.device.config.security.Privileges.Constants.ADMINISTRATE_DEVICE_TYPE})
    public List<ConnectionTypeInfo> getSupportedConnectionTypes(@PathParam("deviceProtocolId") long deviceProtocolId, @Context UriInfo uriInfo, @BeanParam JsonQueryFilter queryFilter) {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = resourceHelper.findDeviceProtocolPluggableClassByMrIdOrThrowException(deviceProtocolId);
        List<? extends ConnectionType> supportedConnectionTypes = deviceProtocolPluggableClass.getDeviceProtocol().getSupportedConnectionTypes();
        List<ConnectionTypePluggableClass> allConnectionTypePluggableClassesToCheck = this.protocolPluggableService.findAllConnectionTypePluggableClasses();
        List<ConnectionTypeInfo> infos = new ArrayList<>();
        ConnectionType.ConnectionTypeDirection connectionTypeDirection = ConnectionType.ConnectionTypeDirection.fromString(queryFilter.getString("direction"));
        for (ConnectionType supportedConnectionType : supportedConnectionTypes) {
            if (ConnectionType.ConnectionTypeDirection.NULL.equals(connectionTypeDirection) || supportedConnectionType.getDirection().equals(connectionTypeDirection)) {
                for (ConnectionTypePluggableClass registeredConnectionTypePluggableClass : allConnectionTypePluggableClassesToCheck) {
                    if (registeredConnectionTypePluggableClass.getJavaClassName().equals(supportedConnectionType.getClass().getCanonicalName())) {
                        infos.add(ConnectionTypeInfo.from(registeredConnectionTypePluggableClass, uriInfo, mdcPropertyUtils));
                    }
                }
            }
        }
        return infos;
    }

    @DELETE @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public Response deleteDeviceCommunicationProtocol(@PathParam("id") long id, DeviceCommunicationProtocolInfo info) {
        info.id = id;
        try {
            DeviceProtocolPluggableClass deviceProtocolPluggableClass = resourceHelper.lockDeviceProtocolPluggableClassOrThrowException(info);
            deviceProtocolPluggableClass.delete();
        } catch (ConcurrentModificationException cme) {
            throw cme;
        } catch (Exception e) {
            throw new WebApplicationException(Response.serverError().build());
        }
        return Response.ok().build();
    }

    @POST @Transactional
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

    @PUT @Transactional
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public DeviceCommunicationProtocolInfo updateDeviceCommunicationProtocol(@Context final UriInfo uriInfo, @PathParam("id") final long id, final DeviceCommunicationProtocolInfo info) {
        try {
            DeviceProtocolPluggableClass deviceProtocolPluggableClass = resourceHelper.lockDeviceProtocolPluggableClassOrThrowException(info);
            deviceProtocolPluggableClass.setName(info.name);
            info.copyProperties(deviceProtocolPluggableClass, mdcPropertyUtils);
            deviceProtocolPluggableClass.save();
            LicensedProtocol licensedProtocol = protocolPluggableService.findLicensedProtocolFor(deviceProtocolPluggableClass);
            return new DeviceCommunicationProtocolInfo(uriInfo, resourceHelper.findDeviceProtocolPluggableClassByMrIdOrThrowException(id), licensedProtocol, true, mdcPropertyUtils);
        } catch (FieldValidationException fieldValidationException) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_VALUE, "properties." + fieldValidationException.getFieldName());
        }
    }
}