package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
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
import javax.ws.rs.QueryParam;
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
    private final DeviceConfigurationService deviceConfigurationService;
    private final ConnectionMethodInfoFactory connectionMethodInfoFactory;
    private final MdcPropertyUtils mdcPropertyUtils;
    private final DeviceDataService deviceDataService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ConnectionMethodResource(ResourceHelper resourceHelper, ProtocolPluggableService protocolPluggableService, EngineModelService engineModelService, DeviceConfigurationService deviceConfigurationService, ConnectionMethodInfoFactory connectionMethodInfoFactory, MdcPropertyUtils mdcPropertyUtils, DeviceDataService deviceDataService, ExceptionFactory exceptionFactory) {
        this.resourceHelper = resourceHelper;
        this.protocolPluggableService = protocolPluggableService;
        this.engineModelService = engineModelService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.connectionMethodInfoFactory = connectionMethodInfoFactory;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.deviceDataService = deviceDataService;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getConnectionMethods(@PathParam("deviceTypeId") long deviceTypeId,
                                              @PathParam("deviceConfigurationId") long deviceConfigurationId,
                                              @BeanParam QueryParameters queryParameters,
                                              @Context UriInfo uriInfo,
                                              @QueryParam("available") Boolean available,
                                              @QueryParam("mrId") String mrId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<ConnectionMethodInfo<?>> connectionMethodInfos = new ArrayList<>();
        List<PartialConnectionTask> partialConnectionTasks = new ArrayList<>();
        if (available!=null) {
            Device device = deviceDataService.findByUniqueMrid(mrId);
            if (device==null) {
                throw exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE);
            }
            if (device.getDeviceConfiguration().getId()!=deviceConfigurationId) {
                throw exceptionFactory.newException(MessageSeeds.DEVICE_DOES_NOT_MATCH_CONFIG);
            }
            partialConnectionTasks.addAll(findAvailablePartialConnectionTasksByDevice(device, deviceConfiguration));
        } else {
            partialConnectionTasks.addAll(deviceConfiguration.getPartialConnectionTasks());
        }

        for (PartialConnectionTask partialConnectionTask : partialConnectionTasks) {
            connectionMethodInfos.add(connectionMethodInfoFactory.asInfo(partialConnectionTask, uriInfo));
        }
        List<ConnectionMethodInfo<?>> pagedConnectionMethodInfos = ListPager.of(connectionMethodInfos).from(queryParameters).find();
        return PagedInfoList.asJson("data", pagedConnectionMethodInfos, queryParameters);
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
        PartialConnectionTask partialConnectionTask = findPartialConnectionTaskOrThrowException(connectionMethodId, deviceConfiguration);
        return connectionMethodInfoFactory.asInfo(partialConnectionTask, uriInfo);
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
        PartialConnectionTask created = connectionMethodInfo.createPartialTask(deviceConfiguration, engineModelService, protocolPluggableService,mdcPropertyUtils);
        return Response.status(Response.Status.CREATED).entity(connectionMethodInfoFactory.asInfo(created, uriInfo)).build();
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

        return connectionMethodInfoFactory.asInfo(deviceConfigurationService.getPartialConnectionTask(partialConnectionTask.getId()).get(), uriInfo);
    }

    /**
     * Add new properties, update existing and remove properties no longer listed
     * Converts String values to correct type
     * Discards properties if there is no matching propertySpec
     */
    private void updateProperties(ConnectionMethodInfo<?> connectionMethodInfo, PartialConnectionTask partialConnectionTask) {
        if (connectionMethodInfo.properties !=null) {
            for (PropertySpec<?> propertySpec : partialConnectionTask.getPluggableClass().getPropertySpecs()) {
                Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, connectionMethodInfo.properties);
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
        throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CONNECTION_TASK);
    }

    /**
     * Finds the {@link ConnectionTask}s that are available for configuration on a device, that is, the PartialConnectionTasks that are not yet used in a ConnectionTask
     *
     * @param device the Device
     * @param deviceConfiguration
     * @return the List of ConnectionTask
     */
    private List<PartialConnectionTask> findAvailablePartialConnectionTasksByDevice(Device device, DeviceConfiguration deviceConfiguration) {
        List<PartialConnectionTask> availableConnectionTasks = new ArrayList<>(deviceConfiguration.getPartialConnectionTasks());
        List<ConnectionTask<?, ?>> connectionTasks = device.getConnectionTasks();
        for (Iterator<PartialConnectionTask> iterator = availableConnectionTasks.iterator(); iterator.hasNext(); ) {
            PartialConnectionTask next = iterator.next();
            for (ConnectionTask<?, ?> connectionTask : connectionTasks) {
                if (connectionTask.getPartialConnectionTask().getId()==next.getId()) {
                    iterator.remove();
                }
            }
        }

        return availableConnectionTasks;
    }

}
