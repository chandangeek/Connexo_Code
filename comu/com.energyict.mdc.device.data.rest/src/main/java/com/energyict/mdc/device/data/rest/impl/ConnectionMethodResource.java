/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.DeviceStagesRestricted;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Created by bvn on 10/3/14.
 */
@DeviceStagesRestricted(value = {EndDeviceStage.POST_OPERATIONAL}, methods = {HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE})
public class ConnectionMethodResource {

    private final ResourceHelper resourceHelper;
    private final ConnectionMethodInfoFactory connectionMethodInfoFactory;
    private final EngineConfigurationService engineConfigurationService;
    private final MdcPropertyUtils mdcPropertyUtils;
    private final ConnectionTaskService connectionTaskService;
    private final ExceptionFactory exceptionFactory;
    private final Provider<ComSessionResource> comTaskExecutionResourceProvider;

    @Inject
    public ConnectionMethodResource(ResourceHelper resourceHelper, ConnectionMethodInfoFactory connectionMethodInfoFactory, EngineConfigurationService engineConfigurationService, MdcPropertyUtils mdcPropertyUtils, ConnectionTaskService connectionTaskService, ExceptionFactory exceptionFactory, Provider<ComSessionResource> comTaskExecutionResourceProvider) {
        this.resourceHelper = resourceHelper;
        this.connectionMethodInfoFactory = connectionMethodInfoFactory;
        this.engineConfigurationService = engineConfigurationService;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.connectionTaskService = connectionTaskService;
        this.exceptionFactory = exceptionFactory;
        this.comTaskExecutionResourceProvider = comTaskExecutionResourceProvider;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response getConnectionMethods(@PathParam("name") String name, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<ConnectionTask<?, ?>> connectionTasks = ListPager.of(device.getConnectionTasks(), new ConnectionTaskComparator()).from(queryParameters).find();
        List<ConnectionMethodInfo<?>> connectionMethodInfos = connectionMethodInfoFactory.asInfoList(connectionTasks, uriInfo);
        return Response.ok(PagedInfoList.fromPagedList("connectionMethods", connectionMethodInfos, queryParameters)).build();
    }

    @POST @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION)
    public Response createConnectionMethod(@PathParam("name") String name, @Context UriInfo uriInfo, ConnectionMethodInfo<?> connectionMethodInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        PartialConnectionTask partialConnectionTask = findPartialConnectionTaskOrThrowException(device, connectionMethodInfo.name);
        ConnectionTask<?, ?> task = connectionMethodInfo.createTask(engineConfigurationService, device, mdcPropertyUtils, partialConnectionTask);
        if (connectionMethodInfo.isDefault) {
            connectionTaskService.setDefaultConnectionTask(task);
        }

        return Response.status(Response.Status.CREATED).entity(connectionMethodInfoFactory.asInfo(task, uriInfo)).build();
    }

    private void pauseOrResumeTask(ConnectionMethodInfo<?> connectionMethodInfo, ConnectionTask<?, ?> task) {
        switch (connectionMethodInfo.status) {
            case ACTIVE:
                task.activate();
                break;
            case INACTIVE:
                task.deactivate();
                break;
        }
    }

    @GET @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response getConnectionMethod(@PathParam("name") String name, @PathParam("id") long connectionMethodId, @Context UriInfo uriInfo) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        ConnectionTask<?, ?> connectionTask = resourceHelper.findConnectionTaskOrThrowException(device, connectionMethodId);
        return Response.status(Response.Status.OK).entity(connectionMethodInfoFactory.asInfo(connectionTask, uriInfo)).build();
    }

    @PUT @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response updateConnectionMethod(@PathParam("name") String name, @PathParam("id") long connectionMethodId,
                                           @Context UriInfo uriInfo,
                                           ConnectionMethodInfo<ConnectionTask<? extends ComPortPool, ? extends PartialConnectionTask>> info) {
        info.id = connectionMethodId;
        ConnectionTask task = resourceHelper.lockConnectionTaskOrThrowException(info);
        Device device = task.getDevice();
        boolean wasConnectionTaskDefault = task.isDefault();
        PartialConnectionTask partialConnectionTask = findPartialConnectionTaskOrThrowException(device, info.name);

        info.writeTo(task, partialConnectionTask, engineConfigurationService, mdcPropertyUtils);
        task.saveAllProperties();
        pauseOrResumeTask(info, task);
        if (info.isDefault && !wasConnectionTaskDefault) {
            connectionTaskService.setDefaultConnectionTask(task);
        } else if (wasConnectionTaskDefault) {
            connectionTaskService.clearDefaultConnectionTask(device);
        }
        return Response.status(Response.Status.OK).entity(connectionMethodInfoFactory.asInfo(task, uriInfo)).build();
    }

    private PartialConnectionTask findPartialConnectionTaskOrThrowException(Device device, String name) {
        for (PartialConnectionTask partialConnectionTask : device.getDeviceConfiguration().getPartialConnectionTasks()) {
            if (partialConnectionTask.getName().equals(name)) {
                return partialConnectionTask;
            }
        }
        throw exceptionFactory.newException(MessageSeeds.NO_SUCH_PARTIAL_CONNECTION_TASK);
    }


    @DELETE @Transactional
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response deleteConnectionMethod(@PathParam("name") String name, @PathParam("id") long connectionMethodId, ConnectionMethodInfo<ConnectionTask<?, ?>> info) {
        ConnectionTask connectionTask = resourceHelper.lockConnectionTaskOrThrowException(info);
        connectionTask.getDevice().removeConnectionTask(connectionTask);
        return Response.ok().build();
    }

    @Path("/{connectionMethodId}/comsessions")
    public ComSessionResource getComTaskExecutionResource() {
        return comTaskExecutionResourceProvider.get();
    }

}
