package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialConnectionTaskBuilder;
import com.energyict.mdc.device.config.PartialInboundConnectionTaskBuilder;
import com.energyict.mdc.device.config.PartialScheduledConnectionTaskBuilder;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.pluggable.rest.PropertyInfo;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
        PartialConnectionTask partialConnectionTaskToDelete = getPartialConnectionTaskOrThrowException(connectionMethodId, deviceConfiguration);
        deviceConfiguration.remove(partialConnectionTaskToDelete);
        return Response.ok().build();
    }

    private PartialConnectionTask getPartialConnectionTaskOrThrowException(long connectionMethodId, DeviceConfiguration deviceConfiguration) {
        for (PartialConnectionTask partialConnectionTask : deviceConfiguration.getPartialConnectionTasks()) {
            if (partialConnectionTask.getId()==connectionMethodId) {
                return partialConnectionTask;
            }
        }
        throw new WebApplicationException("No such connection task", Response.Status.NOT_FOUND);
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
        PartialConnectionTask created;
        switch (connectionMethodInfo.direction) {
            case "Inbound":
                PartialInboundConnectionTaskBuilder connectionTaskBuilder = deviceConfiguration.getCommunicationConfiguration().createPartialInboundConnectionTask()
                    .name(connectionMethodInfo.name)
                    .pluggableClass(findConnectionTypeOrThrowException(connectionMethodInfo.connectionType))
                    .comPortPool((InboundComPortPool) engineModelService.findComPortPool(connectionMethodInfo.comPortPool))
                    .asDefault(connectionMethodInfo.isDefault);
                addPropertiesToPartialConnectionTask(connectionMethodInfo, connectionTaskBuilder);
                created = connectionTaskBuilder.build();
                break;
            case "Scheduled":
                PartialScheduledConnectionTaskBuilder scheduledConnectionTaskBuilder = deviceConfiguration.createPartialScheduledConnectionTask()
                    .name(connectionMethodInfo.name)
                    .pluggableClass(findConnectionTypeOrThrowException(connectionMethodInfo.connectionType))
                    .comPortPool((OutboundComPortPool) engineModelService.findComPortPool(connectionMethodInfo.comPortPool))
                    .asDefault(connectionMethodInfo.isDefault)
                    .connectionStrategy(connectionMethodInfo.connectionStrategy)
                    .allowSimultaneousConnections(connectionMethodInfo.allowSimultaneousConnections);

                addPropertiesToPartialConnectionTask(connectionMethodInfo, scheduledConnectionTaskBuilder);
                created = scheduledConnectionTaskBuilder.build();
                break;
            default:
                throw new WebApplicationException("Unsupported direction:" +connectionMethodInfo.direction, Response.Status.NOT_ACCEPTABLE);
        }
        return Response.status(Response.Status.CREATED).entity(ConnectionMethodInfoFactory.asInfo(created, uriInfo)).build();

    }

    private void addPropertiesToPartialConnectionTask(ConnectionMethodInfo connectionMethodInfo, PartialConnectionTaskBuilder<?,?,?> connectionTaskBuilder) {
        if (connectionMethodInfo.propertyInfos!=null) {
            for (PropertyInfo propertyInfo : connectionMethodInfo.propertyInfos) {
                connectionTaskBuilder.addProperty(propertyInfo.key, propertyInfo.getPropertyValueInfo().value);
            }
        }
    }

    private ConnectionTypePluggableClass findConnectionTypeOrThrowException(String pluggableClassName) {
        Optional<? extends ConnectionTypePluggableClass> pluggableClassOptional = protocolPluggableService.findConnectionTypePluggableClassByName(pluggableClassName);
        if (!pluggableClassOptional.isPresent()) {
            throw new WebApplicationException("No such connection type", Response.status(Response.Status.NOT_FOUND).entity("No such connection type").build());
        }
        return pluggableClassOptional.get();
    }
}
