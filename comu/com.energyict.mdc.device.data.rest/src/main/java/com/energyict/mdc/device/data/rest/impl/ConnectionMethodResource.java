package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
public class ConnectionMethodResource {

    private final ResourceHelper resourceHelper;
    private final ConnectionMethodInfoFactory connectionMethodInfoFactory;
    private final EngineModelService engineModelService;
    private final MdcPropertyUtils mdcPropertyUtils;
    private final ConnectionTaskService connectionTaskService;
    private final ExceptionFactory exceptionFactory;
    private final Provider<ComSessionResource> comTaskExecutionResourceProvider;

    @Inject
    public ConnectionMethodResource(ResourceHelper resourceHelper, ConnectionMethodInfoFactory connectionMethodInfoFactory, EngineModelService engineModelService, MdcPropertyUtils mdcPropertyUtils, ConnectionTaskService connectionTaskService, ExceptionFactory exceptionFactory, Provider<ComSessionResource> comTaskExecutionResourceProvider) {
        this.resourceHelper = resourceHelper;
        this.connectionMethodInfoFactory = connectionMethodInfoFactory;
        this.engineModelService = engineModelService;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.connectionTaskService = connectionTaskService;
        this.exceptionFactory = exceptionFactory;
        this.comTaskExecutionResourceProvider = comTaskExecutionResourceProvider;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public Response getConnectionMethods(@PathParam("mRID") String mRID, @Context UriInfo uriInfo, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        List<ConnectionTask<?, ?>> connectionTasks = ListPager.of(device.getConnectionTasks(), new ConnectionTaskComparator()).from(queryParameters).find();
        List<ConnectionMethodInfo<?>> connectionMethodInfos = connectionMethodInfoFactory.asInfoList(connectionTasks, uriInfo);
        return Response.ok(PagedInfoList.asJson("connectionMethods", connectionMethodInfos, queryParameters)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE)
    public Response createConnectionMethod(@PathParam("mRID") String mrid, @Context UriInfo uriInfo, ConnectionMethodInfo<?> connectionMethodInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        PartialConnectionTask partialConnectionTask = findPartialConnectionTaskOrThrowException(device, connectionMethodInfo.name);
        ConnectionTask<?, ?> task = connectionMethodInfo.createTask(engineModelService, device, mdcPropertyUtils, partialConnectionTask);
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

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public Response getConnectionMethod(@PathParam("mRID") String mrid, @PathParam("id") long connectionMethodId, @Context UriInfo uriInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ConnectionTask<?, ?> connectionTask = resourceHelper.findConnectionTaskOrThrowException(device, connectionMethodId);
        return Response.status(Response.Status.OK).entity(connectionMethodInfoFactory.asInfo(connectionTask, uriInfo)).build();
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE)
    public Response updateConnectionMethod(@PathParam("mRID") String mrid, @PathParam("id") long connectionMethodId, @Context UriInfo uriInfo, ConnectionMethodInfo<ConnectionTask<? extends ComPortPool, ? extends PartialConnectionTask>> connectionMethodInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ConnectionTask<? extends ComPortPool, ? extends PartialConnectionTask> task = resourceHelper.findConnectionTaskOrThrowException(device, connectionMethodId);
        boolean wasConnectionTaskDefault = task.isDefault();
        PartialConnectionTask partialConnectionTask = findPartialConnectionTaskOrThrowException(device, connectionMethodInfo.name);

        connectionMethodInfo.writeTo(task, partialConnectionTask, engineModelService, mdcPropertyUtils);
        task.save();
        pauseOrResumeTask(connectionMethodInfo, task);
        if (connectionMethodInfo.isDefault) {
            connectionTaskService.setDefaultConnectionTask(task);
        } else if (wasConnectionTaskDefault) {
            connectionTaskService.clearDefaultConnectionTask(device);
        }

        device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        task = resourceHelper.findConnectionTaskOrThrowException(device, connectionMethodId);
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


    @DELETE
    @Path("/{id}")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE)
    public Response deleteConnectionMethod(@PathParam("mRID") String mrid, @PathParam("id") long connectionMethodId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ConnectionTask<?,?> targetConnectionTask = resourceHelper.findConnectionTaskOrThrowException(device, connectionMethodId);
        device.removeConnectionTask(targetConnectionTask);
        return Response.ok().build();
    }

    @Path("/{connectionMethodId}/comsessions")
    public ComSessionResource getComTaskExecutionResource() {
        return comTaskExecutionResourceProvider.get();
    }

}
