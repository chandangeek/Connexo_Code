/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ConnectionMethodResource {
    private final ResourceHelper resourceHelper;
    private final ProtocolPluggableService protocolPluggableService;
    private final EngineConfigurationService engineConfigurationService;
    private final ConnectionMethodInfoFactory connectionMethodInfoFactory;
    private final MdcPropertyUtils mdcPropertyUtils;
    private final DeviceService deviceService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ConnectionMethodResource(ResourceHelper resourceHelper,
                                    ProtocolPluggableService protocolPluggableService,
                                    EngineConfigurationService engineConfigurationService,
                                    ConnectionMethodInfoFactory connectionMethodInfoFactory,
                                    MdcPropertyUtils mdcPropertyUtils,
                                    DeviceService deviceService,
                                    ExceptionFactory exceptionFactory) {
        this.resourceHelper = resourceHelper;
        this.protocolPluggableService = protocolPluggableService;
        this.engineConfigurationService = engineConfigurationService;
        this.connectionMethodInfoFactory = connectionMethodInfoFactory;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.deviceService = deviceService;
        this.exceptionFactory = exceptionFactory;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList getConnectionMethods(@PathParam("deviceTypeId") long deviceTypeId,
                                              @PathParam("deviceConfigurationId") long deviceConfigurationId,
                                              @BeanParam JsonQueryParameters queryParameters,
                                              @Context UriInfo uriInfo,
                                              @QueryParam("available") Boolean available,
                                              @QueryParam("deviceId") long deviceId) {
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);
        List<PartialConnectionTask> partialConnectionTasks;
        if (available != null) {
            Device device = deviceService.findDeviceById(deviceId).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE, deviceId));
            if (device.getDeviceConfiguration().getId() != deviceConfigurationId) {
                throw exceptionFactory.newException(MessageSeeds.DEVICE_DOES_NOT_MATCH_CONFIG);
            }
            partialConnectionTasks = findAvailablePartialConnectionTasksByDevice(device, deviceConfiguration);
        } else {
            partialConnectionTasks = deviceConfiguration.getPartialConnectionTasks();
        }
        List<ConnectionMethodInfo<?>> connectionMethodInfos = partialConnectionTasks.stream()
                .sorted(Comparator.comparing(PartialConnectionTask::getName, String.CASE_INSENSITIVE_ORDER))
                .map(partialConnectionTask -> connectionMethodInfoFactory.asInfo(partialConnectionTask, uriInfo))
                .collect(Collectors.toList());
        List<ConnectionMethodInfo<?>> pagedConnectionMethodInfos = ListPager.of(connectionMethodInfos).from(queryParameters).find();
        return PagedInfoList.fromPagedList("data", pagedConnectionMethodInfos, queryParameters);
    }

    @GET @Transactional
    @Path("/{connectionMethodId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public ConnectionMethodInfo<?> getConnectionMethods(@PathParam("connectionMethodId") long connectionMethodId,
                                                        @Context UriInfo uriInfo) {
        PartialConnectionTask partialConnectionTask = resourceHelper.findPartialConnectionTaskByIdOrThrowException(connectionMethodId);
        return connectionMethodInfoFactory.asInfo(partialConnectionTask, uriInfo);
    }

    @DELETE @Transactional
    @Path("/{connectionMethodId}")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response deleteConnectionMethods(@PathParam("deviceTypeId") long deviceTypeId,
                                            @PathParam("deviceConfigurationId") long deviceConfigurationId,
                                            @PathParam("connectionMethodId") long connectionMethodId,
                                            ConnectionMethodInfo<?> info) {
        info.id = connectionMethodId;
        PartialConnectionTask partialConnectionTask = resourceHelper.lockPartialConnectionTaskOrThrowException(info);
        partialConnectionTask.getConfiguration().remove(partialConnectionTask);
        return Response.ok().build();
    }

    @POST @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response createConnectionMethod(@PathParam("deviceTypeId") long deviceTypeId,
                                           @PathParam("deviceConfigurationId") long deviceConfigurationId,
                                           @Context UriInfo uriInfo,
                                           ConnectionMethodInfo<PartialConnectionTask> connectionMethodInfo) {
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);
        PartialConnectionTask created = connectionMethodInfo.createPartialTask(deviceConfiguration, engineConfigurationService, protocolPluggableService, mdcPropertyUtils);
        return Response.status(Response.Status.CREATED).entity(connectionMethodInfoFactory.asInfo(created, uriInfo)).build();
    }

    @PUT @Transactional
    @Path("/{connectionMethodId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response updateConnectionMethod(@PathParam("deviceTypeId") long deviceTypeId,
                                           @PathParam("deviceConfigurationId") long deviceConfigurationId,
                                           @PathParam("connectionMethodId") long connectionMethodId,
                                           @Context UriInfo uriInfo,
                                           ConnectionMethodInfo<PartialConnectionTask> info) {
        info.id = connectionMethodId;
        PartialConnectionTask partialConnectionTask = resourceHelper.lockPartialConnectionTaskOrThrowException(info);
        info.writeTo(partialConnectionTask, engineConfigurationService, protocolPluggableService);
        updateProperties(info, partialConnectionTask);
        partialConnectionTask.save();

        return Response.ok(connectionMethodInfoFactory.asInfo(resourceHelper.findPartialConnectionTaskByIdOrThrowException(connectionMethodId), uriInfo)).build();
    }

    /**
     * Add new properties, update existing and remove properties no longer listed
     * Converts String values to correct type
     * Discards properties if there is no matching propertySpec
     */
    private void updateProperties(ConnectionMethodInfo<PartialConnectionTask> connectionMethodInfo, PartialConnectionTask partialConnectionTask) {
        if (connectionMethodInfo.properties != null) {
            try {
                for (PropertySpec propertySpec : partialConnectionTask.getPluggableClass().getPropertySpecs()) {
                    Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, connectionMethodInfo.properties);
                    if (propertyValue != null) {
                        partialConnectionTask.setProperty(propertySpec.getName(), propertyValue);
                    } else {
                        partialConnectionTask.removeProperty(propertySpec.getName());
                    }
                }
            } catch (LocalizedFieldValidationException e) {
                throw new LocalizedFieldValidationException(e.getMessageSeed(), "properties."+e.getViolatingProperty(), e.getArgs());
            }
        }
    }

    /**
     * Finds the {@link ConnectionTask}s that are available for configuration on a device, that is, the PartialConnectionTasks that are not yet used in a ConnectionTask
     *
     * @param device              the Device
     * @param deviceConfiguration
     * @return the List of ConnectionTask
     */
    private List<PartialConnectionTask> findAvailablePartialConnectionTasksByDevice(Device device, DeviceConfiguration deviceConfiguration) {
        List<PartialConnectionTask> availableConnectionTasks = new ArrayList<>(deviceConfiguration.getPartialConnectionTasks());
        List<ConnectionTask<?, ?>> connectionTasks = device.getConnectionTasks();
        for (Iterator<PartialConnectionTask> iterator = availableConnectionTasks.iterator(); iterator.hasNext(); ) {
            PartialConnectionTask next = iterator.next();
            for (ConnectionTask<?, ?> connectionTask : connectionTasks) {
                if (connectionTask.getPartialConnectionTask().getId() == next.getId()) {
                    iterator.remove();
                }
            }
        }

        return availableConnectionTasks;
    }
}
