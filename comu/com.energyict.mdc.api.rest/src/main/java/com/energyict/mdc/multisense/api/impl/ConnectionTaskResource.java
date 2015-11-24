package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import com.energyict.mdc.multisense.api.impl.utils.ResourceHelper;
import com.energyict.mdc.multisense.api.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Validator;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 7/13/15.
 */
@Path("devices/{mrid}/connectiontasks")
public class ConnectionTaskResource {

    private final ConnectionTaskInfoFactory connectionTaskInfoFactory;
    private final ConnectionTaskService connectionTaskService;
    private final DeviceService deviceService;
    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final Validator validator;

    @Inject
    public ConnectionTaskResource(ConnectionTaskInfoFactory connectionTaskInfoFactory, ConnectionTaskService connectionTaskService, DeviceService deviceService, ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, Validator validator) {
        this.connectionTaskInfoFactory = connectionTaskInfoFactory;
        this.connectionTaskService = connectionTaskService;
        this.deviceService = deviceService;
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.validator = validator;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<ConnectionTaskInfo> getConnectionTasks(@PathParam("mrid") String mrid,
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

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{connectionTaskId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public ConnectionTaskInfo getConnectionTask(@PathParam("mrid") String mrid,
                                                @PathParam("connectionTaskId") long id,
                                                @Context UriInfo uriInfo,
                                                @BeanParam FieldSelection fieldSelection,
                                                @BeanParam JsonQueryParameters queryParameters) {
        ConnectionTask<?,?> connectionTask = connectionTaskService.findConnectionTask(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_CONNECTION_TASK));
        if (!connectionTask.getDevice().getmRID().equals(mrid)) {
            throw exceptionFactory.newException(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_CONNECTION_TASK);
        }
        return connectionTaskInfoFactory.from(connectionTask, uriInfo, fieldSelection.getFields());
    }

    @POST @Transactional
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public Response createConnectionTask(@PathParam("mrid") String mrid, ConnectionTaskInfo connectionTaskInfo, @Context UriInfo uriInfo) {
        if (connectionTaskInfo.device==null || connectionTaskInfo.device.version==null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.VERSION_MISSING, "Device");
        }
        if (connectionTaskInfo.connectionMethod==null || connectionTaskInfo.connectionMethod.id==null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.EXPECTED_METHOD_ID);
        }
        Device device = deviceService.findAndLockDeviceBymRIDAndVersion(mrid, connectionTaskInfo.device.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.NO_SUCH_DEVICE));
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

    @PUT @Transactional
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{connectionTaskId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public ConnectionTaskInfo updateConnectionTask(@PathParam("mrid") String mrid, @PathParam("connectionTaskId") long connectionTaskId,
                                                           ConnectionTaskInfo connectionTaskInfo, @Context UriInfo uriInfo) {
        if (connectionTaskInfo.version == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.VERSION_MISSING);
        }
        if (connectionTaskInfo.device==null || connectionTaskInfo.device.version==null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.VERSION_MISSING, "Device");
        }
        Device device = deviceService.findAndLockDeviceBymRIDAndVersion(mrid, connectionTaskInfo.device.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.NO_SUCH_DEVICE));
        ConnectionTask<? extends ComPortPool, ? extends PartialConnectionTask> connectionTask = connectionTaskService.findAndLockConnectionTaskByIdAndVersion(connectionTaskId, connectionTaskInfo.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.NO_SUCH_CONNECTION_TASK));
        if (!connectionTask.getDevice().getmRID().equals(mrid)) {
            throw exceptionFactory.newException(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_CONNECTION_TASK);
        }
        if (connectionTaskInfo.direction == null) {
            throw exceptionFactory.newException(MessageSeeds.MISSING_CONNECTION_TASK_TYPE);
        }
        ConnectionTask<?, ?> updatedConnectionTask = connectionTaskInfo.direction.updateTask(connectionTaskId, connectionTaskInfo, connectionTaskInfoFactory, device, connectionTask);

        return connectionTaskInfoFactory.from(updatedConnectionTask, uriInfo, Collections.<String>emptyList());
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return connectionTaskInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }


    private PartialConnectionTask findPartialConnectionTaskOrThrowException(Device device, long id) {
        return device.getDeviceConfiguration().
                getPartialConnectionTasks().stream().
                filter(pct -> pct.getId() == id).
                findFirst().
                orElseThrow(() -> exceptionFactory.newException(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_PARTIAL_CONNECTION_TASK));
    }

}
