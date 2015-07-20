package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import com.energyict.mdc.tasks.TaskService;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
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
 * Created by bvn on 7/17/15.
 */
@Path("/comtasks")
public class ComTaskResource {

    private final TaskService taskService;
    private final ComTaskInfoFactory comTaskInfoFactory;

    @Inject
    public ComTaskResource(TaskService taskService, ComTaskInfoFactory comTaskInfoFactory) {
        this.taskService = taskService;
        this.comTaskInfoFactory = comTaskInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{comTaskId}")
    public ComTaskInfo getComTask(@PathParam("comTaskId") long comTaskId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
         return taskService.findComTask(comTaskId)
                 .map(ct->comTaskInfoFactory.asHypermedia(ct, uriInfo, fieldSelection.getFields()))
                 .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    public PagedInfoList<ComTaskInfo> getComTasks(@BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        List<ComTaskInfo> infos = taskService.findAllComTasks().from(queryParameters).stream()
                .map(ct -> comTaskInfoFactory.asHypermedia(ct, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(ComTaskResource.class);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    public List<String> getFields() {
        return comTaskInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }



}
