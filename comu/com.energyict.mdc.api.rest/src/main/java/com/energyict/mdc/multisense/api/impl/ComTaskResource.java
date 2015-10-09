package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import com.energyict.mdc.multisense.api.security.Privileges;
import com.energyict.mdc.tasks.TaskService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 7/17/15.
 */
@Path("/comtasks")
public class ComTaskResource {

    private final TaskService taskService;
    private final ComTaskInfoFactory comTaskInfoFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ComTaskResource(TaskService taskService, ComTaskInfoFactory comTaskInfoFactory, ExceptionFactory exceptionFactory) {
        this.taskService = taskService;
        this.comTaskInfoFactory = comTaskInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{comTaskId}")
    @RolesAllowed({Privileges.PUBLIC_REST_API})
    public ComTaskInfo getComTask(@PathParam("comTaskId") long comTaskId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
         return taskService.findComTask(comTaskId)
                 .map(ct -> comTaskInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                 .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_COM_TASK));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.PUBLIC_REST_API})
    public PagedInfoList<ComTaskInfo> getComTasks(@BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        List<ComTaskInfo> infos = taskService.findAllComTasks().from(queryParameters).stream()
                .map(ct -> comTaskInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(ComTaskResource.class);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.PUBLIC_REST_API})
    public List<String> getFields() {
        return comTaskInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }



}
