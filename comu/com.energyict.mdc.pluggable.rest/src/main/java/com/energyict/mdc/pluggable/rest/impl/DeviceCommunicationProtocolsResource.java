package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.LicensedProtocol;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import java.util.ArrayList;
import java.util.List;
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

/**
 * Copyrights EnergyICT
 * Date: 05/11/13
 * Time: 12:29
 */
@Path("/devicecommunicationprotocols")
public class DeviceCommunicationProtocolsResource {

    private final PropertySpecService propertySpecService;
    private final ProtocolPluggableService protocolPluggableService;
    private final LicensedProtocolService licensedProtocolService;

    @Inject
    public DeviceCommunicationProtocolsResource(PropertySpecService propertySpecService, ProtocolPluggableService protocolPluggableService, LicensedProtocolService licensedProtocolService) {
        this.propertySpecService = propertySpecService;
        this.protocolPluggableService = protocolPluggableService;
        this.licensedProtocolService = licensedProtocolService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getDeviceCommunicationProtocols(@Context UriInfo uriInfo, @BeanParam QueryParameters queryParameters) {
        List<DeviceProtocolPluggableClass> deviceProtocolPluggableClasses = this.protocolPluggableService.findAllDeviceProtocolPluggableClasses().from(queryParameters).find();
        List<DeviceCommunicationProtocolInfo> deviceCommunicationProtocolInfos = new ArrayList<>(deviceProtocolPluggableClasses.size());
        for (DeviceProtocolPluggableClass deviceProtocolPluggableClass : deviceProtocolPluggableClasses) {
            LicensedProtocol licensedProtocol = this.licensedProtocolService.findLicensedProtocolFor(deviceProtocolPluggableClass);
            deviceCommunicationProtocolInfos.add(new DeviceCommunicationProtocolInfo(uriInfo, this.propertySpecService, deviceProtocolPluggableClass, licensedProtocol, false));
        }
        return PagedInfoList.asJson("deviceCommunicationProtocolInfos", deviceCommunicationProtocolInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceCommunicationProtocolInfo getDeviceCommunicationProtocol(@Context UriInfo uriInfo, @PathParam("id") long id) {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = this.protocolPluggableService.findDeviceProtocolPluggableClass(id);
        LicensedProtocol licensedProtocol = this.licensedProtocolService.findLicensedProtocolFor(deviceProtocolPluggableClass);
        return new DeviceCommunicationProtocolInfo(uriInfo, this.propertySpecService, deviceProtocolPluggableClass, licensedProtocol, true);
    }

    @GET
    @Path("/{deviceProtocolId}/connectiontypes")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ConnectionTypeInfo> getSupportedConnectionTypes(@PathParam("deviceProtocolId") long deviceProtocolId, @Context UriInfo uriInfo){
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = this.protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolId);
        List<ConnectionType> supportedConnectionTypes = deviceProtocolPluggableClass.getDeviceProtocol().getSupportedConnectionTypes();
        List<ConnectionTypePluggableClass> allConnectionTypePluggableClassesToCheck = this.protocolPluggableService.findAllConnectionTypePluggableClasses();
        List<ConnectionTypeInfo> infos = new ArrayList<>();
        for(ConnectionType supportedConnectionType: supportedConnectionTypes){
            for(ConnectionTypePluggableClass registeredConnectionTypePluggableClass:allConnectionTypePluggableClassesToCheck){
                if(registeredConnectionTypePluggableClass.getJavaClassName().equals(supportedConnectionType.getClass().getCanonicalName())){
                     infos.add(ConnectionTypeInfo.from(registeredConnectionTypePluggableClass, uriInfo));
                }
            }
        }
        return infos;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDeviceCommunicationProtocol(@PathParam("id") int id) {
        try {
            this.protocolPluggableService.deleteDeviceProtocolPluggableClass(id);
        }
        catch (Exception e) {
            throw new WebApplicationException(Response.serverError().build());
        }
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceCommunicationProtocolInfo createDeviceCommunicationProtocol(@Context final UriInfo uriInfo, final DeviceCommunicationProtocolInfo deviceCommunicationProtocolInfo) throws WebApplicationException {
        try {
            DeviceProtocolPluggableClass deviceProtocolPluggableClass =
                    protocolPluggableService.newDeviceProtocolPluggableClass(
                            deviceCommunicationProtocolInfo.licensedProtocol.protocolName,
                            deviceCommunicationProtocolInfo.licensedProtocol.protocolJavaClassName);
            deviceCommunicationProtocolInfo.copyProperties(deviceProtocolPluggableClass);
            deviceProtocolPluggableClass.save();
            LicensedProtocol licensedProtocol = licensedProtocolService.findLicensedProtocolFor(deviceProtocolPluggableClass);
            return new DeviceCommunicationProtocolInfo(uriInfo, propertySpecService, deviceProtocolPluggableClass, licensedProtocol, true);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceCommunicationProtocolInfo updateDeviceCommunicationProtocol(@Context final UriInfo uriInfo, @PathParam("id") final long id, final DeviceCommunicationProtocolInfo deviceCommunicationProtocolInfo) {
        try {
            DeviceProtocolPluggableClass deviceProtocolPluggableClass = protocolPluggableService.findDeviceProtocolPluggableClass(id);
            deviceProtocolPluggableClass.setName(deviceCommunicationProtocolInfo.name);
            deviceCommunicationProtocolInfo.copyProperties(deviceProtocolPluggableClass);
            deviceProtocolPluggableClass.save();
            LicensedProtocol licensedProtocol = licensedProtocolService.findLicensedProtocolFor(deviceProtocolPluggableClass);
            return new DeviceCommunicationProtocolInfo(uriInfo, propertySpecService, deviceProtocolPluggableClass, licensedProtocol, true);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}