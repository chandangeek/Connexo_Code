package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import com.energyict.mdc.multisense.api.impl.utils.ResourceHelper;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 7/13/15.
 */
@Path("devices/{mrid}/connectionmethods")
public class ConnectionTaskResource {

    private final ConnectionTaskInfoFactory connectionTaskInfoFactory;
    private final ConnectionTaskService connectionTaskService;
    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ConnectionTaskResource(ConnectionTaskInfoFactory connectionTaskInfoFactory, ConnectionTaskService connectionTaskService, ResourceHelper resourceHelper, ExceptionFactory exceptionFactory) {
        this.connectionTaskInfoFactory = connectionTaskInfoFactory;
        this.connectionTaskService = connectionTaskService;
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    public PagedInfoList<ConnectionTaskInfo> getConnectionMethods(@PathParam("mrid") String mrid,
                                              @Context UriInfo uriInfo,
                                              @BeanParam FieldSelection fieldSelection,
                                              @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<ConnectionTaskInfo> infos = ListPager.
                of(device.getConnectionTasks(), (a, b) -> a.getName().compareTo(b.getName())).
                from(queryParameters).stream().
                map(ct -> connectionTaskInfoFactory.from(ct, uriInfo, fieldSelection.getFields())).
                collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(ConnectionTaskResource.class).resolveTemplate("mrid", device.getmRID());
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{connectionTaskId}")
    public ConnectionTaskInfo getConnectionTask(@PathParam("mrid") String mrid,
                                                @PathParam("connectionTaskId") long id,
                                                @Context UriInfo uriInfo,
                                                @BeanParam FieldSelection fieldSelection,
                                                @BeanParam JsonQueryParameters queryParameters) {
        ConnectionTask<?,?> connectionTask = connectionTaskService.findConnectionTask(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
        if (!connectionTask.getDevice().getmRID().equals(mrid)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode());
        }
        return connectionTaskInfoFactory.from(connectionTask, uriInfo, fieldSelection.getFields());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    public Response createConnectionTaskResource(@PathParam("mrid") String mrid, ConnectionTaskInfo connectionTaskInfo, @Context UriInfo uriInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        if (connectionTaskInfo.connectionMethod == null || connectionTaskInfo.connectionMethod.id == null) {
            throw exceptionFactory.newException(MessageSeeds.MISSING_PARTIAL_CONNECTION_METHOD);
        }
        PartialConnectionTask partialConnectionTask = findPartialConnectionTaskOrThrowException(device, connectionTaskInfo.connectionMethod.id);
        if (connectionTaskInfo.direction == null) {
            throw exceptionFactory.newException(MessageSeeds.MISSING_CONNECTION_TASK_TYPE);
        }
        ConnectionTask<?, ?> task = connectionTaskInfo.direction.createTask(connectionTaskInfo, connectionTaskInfoFactory, device, partialConnectionTask);
        if (connectionTaskInfo.isDefault) {
            connectionTaskService.setDefaultConnectionTask(task);
        }

        URI uri = uriInfo.getBaseUriBuilder().
                path(ConnectionTaskResource.class).
                path(ConnectionTaskResource.class, "getConnectionTask").
                build(device.getmRID(), task.getId());

        return Response.created(uri).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{connectionTaskId}")
    public ConnectionTaskInfo updateConnectionTaskResource(@PathParam("mrid") String mrid, @PathParam("connectionTaskId") long connectionTaskId,
                                                           ConnectionTaskInfo connectionTaskInfo, @Context UriInfo uriInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ConnectionTask<? extends ComPortPool, ? extends PartialConnectionTask> connectionTask = findConnectionTaskOrThrowException(device, connectionTaskId);
        if (connectionTaskInfo.direction == null) {
            throw exceptionFactory.newException(MessageSeeds.MISSING_CONNECTION_TASK_TYPE);
        }
        ConnectionTask<?, ?> updatedConnectionTask = connectionTaskInfo.direction.updateTask(connectionTaskId, connectionTaskInfo, connectionTaskInfoFactory, device, connectionTask);

        return connectionTaskInfoFactory.from(updatedConnectionTask, uriInfo, Collections.<String>emptyList());
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    public List<String> getFields() {
        return connectionTaskInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }


    private PartialConnectionTask findPartialConnectionTaskOrThrowException(Device device, long id) {
        return device.getDeviceConfiguration().
                getPartialConnectionTasks().stream().
                filter(pct -> pct.getId() == id).
                findFirst().
                orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_PARTIAL_CONNECTION_TASK));
    }

    public ConnectionTask<?, ?> findConnectionTaskOrThrowException(Device device, long connectionTaskId) {
        return device.getConnectionTasks()
                .stream()
                .filter(ct -> ct.getId() == connectionTaskId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CONNECTION_TASK));
    }


}
