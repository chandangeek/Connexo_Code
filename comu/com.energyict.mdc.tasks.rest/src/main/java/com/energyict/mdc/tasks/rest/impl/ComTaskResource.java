package com.energyict.mdc.tasks.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.TaskService;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Path("/comtasks")
public class ComTaskResource {
    private TaskService taskService;
    private MasterDataService masterDataService;

    @Inject
    public ComTaskResource(TaskService taskService, MasterDataService masterDataService) {
        this.taskService = taskService;
        this.masterDataService = masterDataService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getComTasks(@BeanParam QueryParameters queryParameters) {
        List<ComTaskInfo> comTaskInfos =
                ComTaskInfo.from(ListPager.of(taskService.findAllComTasks(), new ComTaskComparator()).from(queryParameters).find());
        return PagedInfoList.asJson("data", comTaskInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getComTask(@PathParam("id") long id) {
        return Response.status(Response.Status.OK).entity(ComTaskInfo.fullFrom(taskService.findComTask(id))).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addComTask(ComTaskInfo comTaskInfo) {
        ComTask newComTask = taskService.newComTask(comTaskInfo.name);
        for (ProtocolTaskInfo protocolTaskInfo : comTaskInfo.commands) {
            Categories category = Categories.valueOf(protocolTaskInfo.category.toUpperCase());
            category.createProtocolTask(masterDataService, newComTask, protocolTaskInfo);
        }
        newComTask.save();
        return Response.ok(ComTaskInfo.from(newComTask)).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateComTask(@PathParam("id") long id, ComTaskInfo comTaskInfo) {
        ComTask editedComTask = taskService.findComTask(id);
        if (editedComTask != null) {
            editedComTask.setName(comTaskInfo.name);
            List<ProtocolTask> currentProtocolTasks = new ArrayList<>(editedComTask.getProtocolTasks());
            Set<Long> protocolTasksIds = new HashSet<>();

            for (ProtocolTaskInfo protocolTaskInfo : comTaskInfo.commands) {
                Categories category = Categories.valueOf(protocolTaskInfo.category.toUpperCase());
                if (protocolTaskInfo.id != null) {
                    protocolTasksIds.add(protocolTaskInfo.id);
                    ProtocolTask protocolTask = taskService.findProtocolTask(protocolTaskInfo.id);
                    if (protocolTask != null) {
                        category.updateProtocolTask(masterDataService, protocolTask, protocolTaskInfo);
                    }
                } else {
                    category.createProtocolTask(masterDataService, editedComTask, protocolTaskInfo);
                }
            }

            for (ProtocolTask protocolTask : currentProtocolTasks) {
                if (!protocolTasksIds.contains(protocolTask.getId()))
                    editedComTask.removeTask(protocolTask);
            }

            editedComTask.save();
            return Response.ok(ComTaskInfo.from(editedComTask)).build();
        }
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteComTask(@PathParam("id") long id) {
        ComTask comTask = taskService.findComTask(id);
        if (comTask != null) {
            comTask.delete();
            return Response.status(Response.Status.OK).build();
        }
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    @GET
    @Path("/categories")
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getCategories(@BeanParam QueryParameters queryParameters) {
        List<CategoryInfo> categoryInfos = CategoryInfo.from(ListPager.of(Arrays.asList(Categories.values())).from(queryParameters).find());
        return PagedInfoList.asJson("data", categoryInfos, queryParameters);
    }

    @GET
    @Path("/actions")
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getActions(@Context UriInfo uriInfo, @BeanParam QueryParameters queryParameters) {
        Optional<String> categoryParameter = Optional.fromNullable(uriInfo.getQueryParameters().getFirst("category"));
        if (categoryParameter.isPresent()) {
            List<ActionInfo> actionInfos = ActionInfo.from(ListPager.of(
                    Categories.valueOf(categoryParameter.get().toUpperCase()).getActions()).from(queryParameters).find());
            return PagedInfoList.asJson("data", actionInfos, queryParameters);
        }
        throw new WebApplicationException("No \"category\" query property is present",
                Response.status(Response.Status.BAD_REQUEST).entity("No \"category\" query property is present").build());
    }
}