package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

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

/**
 * Copyrights EnergyICT
 * Date: 1/04/14
 * Time: 8:59
 */
public class ConnectionMethodResource {
    private final ResourceHelper resourceHelper;
    private final ProtocolPluggableService protocolPluggableService;
    private final EngineModelService engineModelService;
    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public ConnectionMethodResource(ResourceHelper resourceHelper, ProtocolPluggableService protocolPluggableService, EngineModelService engineModelService, DeviceConfigurationService deviceConfigurationService) {
        this.resourceHelper = resourceHelper;
        this.protocolPluggableService = protocolPluggableService;
        this.engineModelService = engineModelService;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getConnectionMethods(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @BeanParam QueryParameters queryParameters, @Context UriInfo uriInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<ConnectionMethodInfo<?>> connectionMethodInfos = new ArrayList<>();
        for (PartialConnectionTask partialConnectionTask : deviceConfiguration.getPartialConnectionTasks()) {
            connectionMethodInfos.add(ConnectionMethodInfoFactory.asInfo(partialConnectionTask, uriInfo));
        }
        List<ConnectionMethodInfo<?>> pagedConnectionMethodInfos = ListPager.of(connectionMethodInfos).from(queryParameters).find();
        return PagedInfoList.asJson("connectionMethods", pagedConnectionMethodInfos, queryParameters);
    }

    @GET
    @Path("/{connectionMethodId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ConnectionMethodInfo<?> getConnectionMethods(@PathParam("deviceTypeId") long deviceTypeId,
                                                     @PathParam("deviceConfigurationId") long deviceConfigurationId,
                                                     @PathParam("connectionMethodId") long connectionMethodId,
                                                     @Context UriInfo uriInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        for (PartialConnectionTask partialConnectionTask : deviceConfiguration.getPartialConnectionTasks()) {
            if (partialConnectionTask.getId()==connectionMethodId) {
                return ConnectionMethodInfoFactory.asInfo(partialConnectionTask, uriInfo);
            }
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @DELETE
    @Path("/{connectionMethodId}")
    public Response deleteConnectionMethods(@PathParam("deviceTypeId") long deviceTypeId,
                                            @PathParam("deviceConfigurationId") long deviceConfigurationId,
                                            @PathParam("connectionMethodId") long connectionMethodId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        PartialConnectionTask partialConnectionTaskToDelete = findPartialConnectionTaskOrThrowException(connectionMethodId, deviceConfiguration);
        deviceConfiguration.remove(partialConnectionTaskToDelete);
        return Response.ok().build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createConnectionMethod(@PathParam("deviceTypeId") long deviceTypeId,
                                           @PathParam("deviceConfigurationId") long deviceConfigurationId,
                                           @Context UriInfo uriInfo,
                                           ConnectionMethodInfo<?> connectionMethodInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        PartialConnectionTask created = connectionMethodInfo.createPartialTask(deviceConfiguration, engineModelService, protocolPluggableService);
        return Response.status(Response.Status.CREATED).entity(ConnectionMethodInfoFactory.asInfo(created, uriInfo)).build();
    }

    @PUT
    @Path("/{connectionMethodId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ConnectionMethodInfo<?> updateConnectionMethod(@PathParam("deviceTypeId") long deviceTypeId,
                                                       @PathParam("deviceConfigurationId") long deviceConfigurationId,
                                                       @PathParam("connectionMethodId") long connectionMethodId,
                                                       @Context UriInfo uriInfo,
                                                       ConnectionMethodInfo<? super PartialConnectionTask> connectionMethodInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        PartialConnectionTask partialConnectionTask = findPartialConnectionTaskOrThrowException(connectionMethodId, deviceConfiguration);
        connectionMethodInfo.writeTo(partialConnectionTask, engineModelService);
        updateProperties(connectionMethodInfo, partialConnectionTask);
        partialConnectionTask.save();
        return ConnectionMethodInfoFactory.asInfo(deviceConfigurationService.getPartialConnectionTask(partialConnectionTask.getId()).get(), uriInfo);
    }

    /**
     * Add new properties, update existing and remove properties no longer listed
     * Converts String values to correct type
     * Discards properties if there is no matching propertySpec
     */
    private void updateProperties(ConnectionMethodInfo<?> connectionMethodInfo, PartialConnectionTask partialConnectionTask) {
        if (connectionMethodInfo.properties !=null) {
            for (PropertySpec<?> propertySpec : partialConnectionTask.getPluggableClass().getPropertySpecs()) {
                Object propertyValue = MdcPropertyUtils.findPropertyValue(propertySpec, connectionMethodInfo.properties);
                if (propertyValue!=null) {
                    partialConnectionTask.setProperty(propertySpec.getName(), propertyValue);
                } else {
                    partialConnectionTask.removeProperty(propertySpec.getName());
                }
            }
        }
    }

    private PartialConnectionTask findPartialConnectionTaskOrThrowException(long connectionMethodId, DeviceConfiguration deviceConfiguration) {
        for (PartialConnectionTask partialConnectionTask : deviceConfiguration.getPartialConnectionTasks()) {
            if (partialConnectionTask.getId()==connectionMethodId) {
                return partialConnectionTask;
            }
        }
        throw new WebApplicationException("No such connection task", Response.Status.NOT_FOUND);
    }

}
