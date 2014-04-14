package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTaskBuilder;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.pluggable.rest.PropertyInfo;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
    private final PluggableService pluggableService;
    private final EngineModelService engineModelService;

    @Inject
    public ConnectionMethodResource(ResourceHelper resourceHelper, PluggableService pluggableService, EngineModelService engineModelService) {
        this.resourceHelper = resourceHelper;
        this.pluggableService = pluggableService;
        this.engineModelService = engineModelService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getConnectionMethods(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @BeanParam QueryParameters queryParameters, @Context UriInfo uriInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<ConnectionMethodInfo> connectionMethodInfos = new ArrayList<>();
        for (PartialConnectionTask partialConnectionTask : deviceConfiguration.getPartialConnectionTasks()) {
            connectionMethodInfos.add(ConnectionMethodInfo.from(partialConnectionTask, uriInfo));
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
                return ConnectionMethodInfo.from(partialConnectionTask, uriInfo);
            }
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
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
        PartialInboundConnectionTask created=null;
        switch (connectionMethodInfo.direction) {
            case "Inbound":
                PartialInboundConnectionTaskBuilder connectionTaskBuilder = deviceConfiguration.getCommunicationConfiguration().createPartialInboundConnectionTask();
                connectionTaskBuilder.name(connectionMethodInfo.name);
                Optional<PluggableClass> pluggableClassOptional = findConnectionTypeOrThrowException(connectionMethodInfo);
                connectionTaskBuilder.pluggableClass((ConnectionTypePluggableClass) pluggableClassOptional.get());
                connectionTaskBuilder.comPortPool((InboundComPortPool) engineModelService.findComPortPool(connectionMethodInfo.comPortPool));
                connectionTaskBuilder.asDefault(connectionMethodInfo.isDefault);
                for (PropertyInfo propertyInfo : connectionMethodInfo.propertyInfos) {
                    connectionTaskBuilder.addProperty(propertyInfo.key, propertyInfo.getPropertyValueInfo().value);
                }
                created = connectionTaskBuilder.build();
        }
        return Response.status(Response.Status.CREATED).entity(ConnectionMethodInfo.from(created, uriInfo)).build();

    }

    private Optional<PluggableClass> findConnectionTypeOrThrowException(ConnectionMethodInfo connectionMethodInfo) {
        Optional<PluggableClass> pluggableClassOptional = pluggableService.findByTypeAndName(PluggableClassType.ConnectionType, connectionMethodInfo.connectionType);
        if (!pluggableClassOptional.isPresent()) {
            throw new WebApplicationException("No such connection type", Response.status(Response.Status.NOT_FOUND).entity("No such connection type").build());
        }
        return pluggableClassOptional;
    }
}
