package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.DeviceConnectionTaskInfo;
import com.energyict.mdc.device.data.rest.DeviceConnectionTaskInfoFactory;
import com.energyict.mdc.device.data.rest.DeviceStatesRestricted;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.engine.config.ComPortPool;
import java.util.List;
import java.util.stream.Collectors;
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

@DeviceStatesRestricted(value = {DefaultState.DECOMMISSIONED}, methods = {HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE})
public class ConnectionResource {

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final DeviceConnectionTaskInfoFactory connectionTaskInfoFactory;

    @Inject
    public ConnectionResource(ResourceHelper resourceHelper, DeviceConnectionTaskInfoFactory connectionTaskInfoFactory, ExceptionFactory exceptionFactory) {
        this.resourceHelper = resourceHelper;
        this.connectionTaskInfoFactory = connectionTaskInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response getConnectionMethods(@PathParam("mRID") String mRID, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        List<ConnectionTask<?, ?>> connectionTasks = device.getConnectionTasks();
        List<DeviceConnectionTaskInfo> infos = connectionTasks.stream()
                .map((ct) -> connectionTaskInfoFactory.from(ct, ct.getLastComSession()))
                .sorted((i1, i2) -> i1.connectionMethod.name.compareTo(i2.connectionMethod.name))
                .collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromPagedList("connections", infos, queryParameters)).build();
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response activateDeactivateConnection(@PathParam("mRID") String mrid, @PathParam("id") long connectionTaskId, @Context UriInfo uriInfo, DeviceConnectionTaskInfo connectionTaskInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ConnectionTask<?, ?> task = resourceHelper.findConnectionTaskOrThrowException(device, connectionTaskId);
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
    @Path("/{id}/run")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response runConnectionTask(@PathParam("mRID") String mrid, @PathParam("id") long connectionTaskId, @Context UriInfo uriInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ConnectionTask<? extends ComPortPool, ? extends PartialConnectionTask> task = resourceHelper.findConnectionTaskOrThrowException(device, connectionTaskId);
        if (task instanceof ScheduledConnectionTask) {
            ((ScheduledConnectionTask) task).scheduleNow();
        } else {
            throw exceptionFactory.newException(MessageSeeds.RUN_CONNECTIONTASK_IMPOSSIBLE);
        }
        return Response.status(Response.Status.OK).entity(connectionTaskInfoFactory.from(task, task.getLastComSession())).build();
    }
}
