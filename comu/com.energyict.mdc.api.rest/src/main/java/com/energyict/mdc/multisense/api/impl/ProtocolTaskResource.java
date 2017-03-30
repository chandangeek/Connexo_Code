/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
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
@Path("/commands")
public class ProtocolTaskResource {

    private final TaskService taskService;
    private final ProtocolTaskInfoFactory protocolTaskInfoFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ProtocolTaskResource(TaskService taskService, ProtocolTaskInfoFactory protocolTaskInfoFactory, ExceptionFactory exceptionFactory) {
        this.taskService = taskService;
        this.protocolTaskInfoFactory = protocolTaskInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * A protocol tasks describes an action that needs to be performed when communicating with a device.
     * A ProtocolTask can be used by only one ComTask
     *
     * @summary fetch a list of all known protocol tasks
     *
     * @param protocolTaskId ID of the protocol task
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return The identified protocol task
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{protocolTaskId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public ProtocolTaskInfo getProtocolTask(@PathParam("protocolTaskId") long protocolTaskId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
         return taskService.findProtocolTask(protocolTaskId)
                 .map(ct-> protocolTaskInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                 .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_PROTOCOL_TASK));
    }

    /**
     * A protocol tasks describes an action that needs to be performed when communicating with a device.
     * A ProtocolTask can be used by only one ComTask
     *
     * @summary fetch a list of all known protocol tasks
     *
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param queryParameters queryParameters
     * @return a sorted, pageable list of elements. Only fields mentioned in field-param will be provided, or all fields if no
     * field-param was provided. The list will be sorted according to db order.
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<ProtocolTaskInfo> getProtocolTasks(@BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        List<ProtocolTaskInfo> infos = taskService.findAllProtocolTasks().from(queryParameters).stream()
                .map(protocolTask -> protocolTaskInfoFactory.from(protocolTask, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(ProtocolTaskResource.class);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    /**
     * List the fields available on this type of entity.
     * <br>E.g.
     * <br>[
     * <br> "id",
     * <br> "name",
     * <br> "actions",
     * <br> "batch"
     * <br>]
     * <br>Fields in the list can be used as parameter on a GET request to the same resource, e.g.
     * <br> <i></i>GET ..../resource?fields=id,name,batch</i>
     * <br> The call above will return only the requested fields of the entity. In the absence of a field list, all fields
     * will be returned. If IDs are required in the URL for parent entities, then will be ignored when using the PROPFIND method.
     *
     * @summary List the fields available on this type of entity
     * @return A list of field names that can be requested as parameter in the GET method on this entity type
     */
    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return protocolTaskInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }



}
