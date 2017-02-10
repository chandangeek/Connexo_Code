/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTask;
import com.energyict.mdc.device.data.rest.DeviceConnectionTaskInfo;
import com.energyict.mdc.device.data.rest.DeviceStatesRestricted;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@DeviceStatesRestricted(value = {DefaultState.DECOMMISSIONED}, methods = {HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE})
public class ConnectionResource {

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final DeviceConnectionTaskInfoFactory connectionTaskInfoFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public ConnectionResource(ResourceHelper resourceHelper, DeviceConnectionTaskInfoFactory connectionTaskInfoFactory, ExceptionFactory exceptionFactory, ConcurrentModificationExceptionFactory conflictFactory) {
        this.resourceHelper = resourceHelper;
        this.connectionTaskInfoFactory = connectionTaskInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.conflictFactory = conflictFactory;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response getConnectionMethods(@PathParam("name") String name, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<ConnectionTask<?, ?>> connectionTasks = device.getConnectionTasks();
        List<DeviceConnectionTaskInfo> infos = connectionTasks.stream()
                .map((ct) -> connectionTaskInfoFactory.from(ct, ct.getLastComSession()))
                .sorted((i1, i2) -> i1.connectionMethod.name.compareTo(i2.connectionMethod.name))
                .collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromPagedList("connections", infos, queryParameters)).build();
    }

    @PUT @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response activateDeactivateConnection(@PathParam("name") String name, @PathParam("id") long connectionTaskId, @Context UriInfo uriInfo, DeviceConnectionTaskInfo connectionTaskInfo) {
        connectionTaskInfo.id = connectionTaskId;
        ConnectionTask<?, ?> task = resourceHelper.lockConnectionTaskOrThrowException(connectionTaskInfo);
        switch (connectionTaskInfo.connectionMethod.status) {
            case ACTIVE:
                task.activate();
                break;
            case INACTIVE:
                task.deactivate();
                break;
            default:
                break;
        }
        return Response.status(Response.Status.OK).entity(connectionTaskInfoFactory.from(task, task.getLastComSession())).build();
    }

    @PUT
    @Transactional
    @Path("/{id}/protocoldialect")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response updateProtocolDialect(@PathParam("id") Long connectionTaskId, DeviceConnectionTaskInfo connectionTaskInfo) {
        ConnectionTask task = resourceHelper.getLockedConnectionTask(connectionTaskId, connectionTaskInfo.version)
                .orElseThrow(conflictFactory.conflict()
                        .withActualVersion(() -> resourceHelper.getCurrentConnectionTaskVersion(connectionTaskId))
                        .withMessageTitle(MessageSeeds.CONCURRENT_RUN_TITLE, connectionTaskInfo.name)
                        .withMessageBody(MessageSeeds.CONCURRENT_RUN_BODY, connectionTaskInfo.name)
                        .supplier());
        if (connectionTaskInfo.protocolDialect != null && !connectionTaskInfo.protocolDialect.isEmpty()){
            List<ProtocolDialectConfigurationProperties> protocolDialectConfigurationPropertiesList = task.getDevice().getDeviceConfiguration().getProtocolDialectConfigurationPropertiesList();
            Optional<ProtocolDialectConfigurationProperties> dialectConfigurationProperties = protocolDialectConfigurationPropertiesList.stream()
                    .filter(protocolDialectConfigurationProperties -> protocolDialectConfigurationProperties.getDeviceProtocolDialect()
                            .getDisplayName()
                            .equals(connectionTaskInfo.protocolDialect))
                    .findFirst();
            if (!dialectConfigurationProperties.isPresent()){
                throw exceptionFactory.newException(MessageSeeds.NO_SUCH_PROTOCOL_PROPERTIES, connectionTaskInfo.protocolDialect);
            }
            if (task instanceof ServerConnectionTask) {
                ((ServerConnectionTask) task).setProtocolDialectConfigurationProperties(dialectConfigurationProperties.get());
                task.save();
            } else {
                throw exceptionFactory.newException(MessageSeeds.UPDATE_DIALECT_PROPERTIES_NOT_ALLOWED);
            }
        }
        return Response.status(Response.Status.OK).build();
    }

    @PUT @Transactional
    @Path("/{id}/run")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    @SuppressWarnings("unchecked")
    public Response runConnectionTask(@PathParam("name") String name, @PathParam("id") long connectionTaskId, @Context UriInfo uriInfo, DeviceConnectionTaskInfo connectionTaskInfo) {
        ConnectionTask task = resourceHelper.getLockedConnectionTask(connectionTaskId, connectionTaskInfo.version)
                .orElseThrow(conflictFactory.conflict()
                        .withActualVersion(() -> resourceHelper.getCurrentConnectionTaskVersion(connectionTaskId))
                        .withMessageTitle(MessageSeeds.CONCURRENT_RUN_TITLE, connectionTaskInfo.name)
                        .withMessageBody(MessageSeeds.CONCURRENT_RUN_BODY, connectionTaskInfo.name)
                        .supplier());
        if (task instanceof ScheduledConnectionTask) {
            ((ScheduledConnectionTask) task).scheduleNow();
        } else {
            throw exceptionFactory.newException(MessageSeeds.RUN_CONNECTIONTASK_IMPOSSIBLE);
        }
        task.save();
        return Response.status(Response.Status.OK).entity(connectionTaskInfoFactory.from(task, task.getLastComSession())).build();
    }
}
