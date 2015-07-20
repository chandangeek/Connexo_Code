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
@Path("/commands")
public class ProtocolTaskResource {

    private final TaskService taskService;
    private final ProtocolTaskInfoFactory protocolTaskInfoFactory;

    @Inject
    public ProtocolTaskResource(TaskService taskService, ProtocolTaskInfoFactory protocolTaskInfoFactory) {
        this.taskService = taskService;
        this.protocolTaskInfoFactory = protocolTaskInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{protocolTaskId}")
    public ProtocolTaskInfo getProtocolTask(@PathParam("protocolTaskId") long protocolTaskId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
         return taskService.findProtocolTask(protocolTaskId)
                 .map(ct-> protocolTaskInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                 .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    public PagedInfoList<ProtocolTaskInfo> getProtocolTasks(@BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        List<ProtocolTaskInfo> infos = taskService.findAllProtocolTasks().from(queryParameters).stream()
                .map(protocolTask -> protocolTaskInfoFactory.from(protocolTask, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(ProtocolTaskResource.class);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    public List<String> getFields() {
        return protocolTaskInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }



}
