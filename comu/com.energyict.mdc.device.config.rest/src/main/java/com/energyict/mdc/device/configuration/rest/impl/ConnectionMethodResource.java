package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialConnectionTaskProperty;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.pluggable.rest.PropertyInfo;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import java.util.ArrayList;
import java.util.Iterator;
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
 * Date: 1/04/14
 * Time: 8:59
 */
public class ConnectionMethodResource {
    private final ResourceHelper resourceHelper;
    private final ProtocolPluggableService protocolPluggableService;
    private final EngineModelService engineModelService;

    @Inject
    public ConnectionMethodResource(ResourceHelper resourceHelper, ProtocolPluggableService protocolPluggableService, EngineModelService engineModelService) {
        this.resourceHelper = resourceHelper;
        this.protocolPluggableService = protocolPluggableService;
        this.engineModelService = engineModelService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getConnectionMethods(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @BeanParam QueryParameters queryParameters, @Context UriInfo uriInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<ConnectionMethodInfo> connectionMethodInfos = new ArrayList<>();
        for (PartialConnectionTask partialConnectionTask : deviceConfiguration.getPartialConnectionTasks()) {
            connectionMethodInfos.add(ConnectionMethodInfoFactory.asInfo(partialConnectionTask, uriInfo));
        }
        List<ConnectionMethodInfo> pagedConnectionMethodInfos = ListPager.of(connectionMethodInfos).from(queryParameters).find();
        return PagedInfoList.asJson("connectionMethods", pagedConnectionMethodInfos, queryParameters);
    }

    @GET
    @Path("/{connectionMethodId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ConnectionMethodInfo getConnectionMethods(@PathParam("deviceTypeId") long deviceTypeId,
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
                                           ConnectionMethodInfo connectionMethodInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        PartialConnectionTask created = connectionMethodInfo.createPartialTask(deviceConfiguration, engineModelService, protocolPluggableService);
        return Response.status(Response.Status.CREATED).entity(ConnectionMethodInfoFactory.asInfo(created, uriInfo)).build();
    }

    @PUT
    @Path("/{connectionMethodId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ConnectionMethodInfo updateConnectionMethod(@PathParam("deviceTypeId") long deviceTypeId,
                                                       @PathParam("deviceConfigurationId") long deviceConfigurationId,
                                                       @PathParam("connectionMethodId") long connectionMethodId,
                                                       @Context UriInfo uriInfo,
                                                       ConnectionMethodInfo connectionMethodInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        PartialConnectionTask partialConnectionTask = findPartialConnectionTaskOrThrowException(connectionMethodId, deviceConfiguration);
        partialConnectionTask.setName(connectionMethodInfo.name);
        updateProperties(connectionMethodInfo, partialConnectionTask);
        partialConnectionTask.save();
        return ConnectionMethodInfoFactory.asInfo(partialConnectionTask, uriInfo);
    }

    /**
     * Add new properties, update existing and remove properties no longer listed
     * Converts String values to correct type and discards properties if there is no matching propertySpec
     */
    private void updateProperties(ConnectionMethodInfo connectionMethodInfo, PartialConnectionTask partialConnectionTask) {
        for (PartialConnectionTaskProperty partialConnectionTaskProperty : partialConnectionTask.getProperties()) {
            for (Iterator<PropertyInfo> iterator = connectionMethodInfo.propertyInfos.iterator(); iterator.hasNext(); ) {
                PropertyInfo propertyInfo =  iterator.next();
                if (propertyInfo.key.equals(partialConnectionTaskProperty.getName()) && propertyInfo.propertyValueInfo.value!=null) {
                    PropertySpec propertySpec = partialConnectionTask.getPluggableClass().getPropertySpec(propertyInfo.key);
                    if (propertySpec==null) {
                        iterator.remove();
                        break;
                    }
                    Object value = MdcPropertyUtils.convertPropertyInfoValueToPropertyValue(propertySpec, propertyInfo.propertyValueInfo.value);
                    partialConnectionTaskProperty.setValue(value);
                    iterator.remove();
                    break;
                }
                partialConnectionTask.removeProperty(partialConnectionTaskProperty.getName());
            }
        }
        for (PropertyInfo propertyInfo : connectionMethodInfo.propertyInfos) {
            partialConnectionTask.setProperty(propertyInfo.key, propertyInfo.getPropertyValueInfo().value);
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
