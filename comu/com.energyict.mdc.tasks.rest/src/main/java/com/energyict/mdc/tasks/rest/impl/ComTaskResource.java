package com.energyict.mdc.tasks.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.engine.model.security.Privileges;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.TaskService;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/comtasks")
public class ComTaskResource {
    private TaskService taskService;
    private MasterDataService masterDataService;
    private DeviceMessageSpecificationService deviceMessageSpecificationService;

    @Inject
    public ComTaskResource(TaskService taskService, MasterDataService masterDataService, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.taskService = taskService;
        this.masterDataService = masterDataService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_COMMUNICATION_INFRASTRUCTURE)
    public PagedInfoList getComTasks(@BeanParam QueryParameters queryParameters) {
        List<ComTaskInfo> comTaskInfos =
                ComTaskInfo.from(ListPager.of(taskService.findAllComTasks(), new ComTaskComparator()).from(queryParameters).find());
        return PagedInfoList.asJson("data", comTaskInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_COMMUNICATION_INFRASTRUCTURE)
    public Response getComTask(@PathParam("id") long id) {
        ComTask comTask = taskService.findComTask(id).orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));
        return Response.status(Response.Status.OK).entity(ComTaskInfo.fullFrom(comTask)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_COMMUNICATION_INFRASTRUCTURE)
    public Response addComTask(ComTaskInfo comTaskInfo) {
        ComTask newComTask = taskService.newComTask(comTaskInfo.name);
        for (ProtocolTaskInfo protocolTaskInfo : comTaskInfo.commands) {
            Categories category = Categories.valueOf(protocolTaskInfo.category.toUpperCase());
            category.createProtocolTask(masterDataService, newComTask, protocolTaskInfo);
        }
        addMessageCategoriesToComTask(comTaskInfo, newComTask);
        newComTask.save();
        return Response.ok(ComTaskInfo.from(newComTask)).build();
    }

    private void addMessageCategoriesToComTask(ComTaskInfo request, ComTask comTask) {
        if (request.messages != null && !request.messages.isEmpty()) {
            List<DeviceMessageCategory> categories = getMessageCategories(request);
            comTask.createMessagesTask().deviceMessageCategories(categories).add();
        }
    }

    private List<DeviceMessageCategory> getMessageCategories(ComTaskInfo comTaskInfo) {
        return comTaskInfo.messages.stream()
                .map(category -> {
                    java.util.Optional<DeviceMessageCategory> categoryRef = deviceMessageSpecificationService.findCategoryById(category.id);
                    return categoryRef.isPresent() ? categoryRef.get() : null;
                })
                .collect(Collectors.toList());
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_COMMUNICATION_INFRASTRUCTURE)
    public Response updateComTask(@PathParam("id") long id, ComTaskInfo comTaskInfo) {
        ComTask comTask = taskService.findComTask(id).orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));
        comTask.setName(comTaskInfo.name);
        List<ProtocolTask> currentProtocolTasks = new ArrayList<>(comTask.getProtocolTasks());
        Set<Long> protocolTasksIds = new HashSet<>();

        for (ProtocolTaskInfo protocolTaskInfo : comTaskInfo.commands) {
            Categories category = Categories.valueOf(protocolTaskInfo.category.toUpperCase());
            if (protocolTaskInfo.id != null) {
                protocolTasksIds.add(protocolTaskInfo.id);
                taskService.findProtocolTask(protocolTaskInfo.id).ifPresent(t -> category.updateProtocolTask(masterDataService, t, protocolTaskInfo));
            } else {
                category.createProtocolTask(masterDataService, comTask, protocolTaskInfo);
            }
        }

        for (ProtocolTask protocolTask : currentProtocolTasks) {
            if (!protocolTasksIds.contains(protocolTask.getId())) {
                comTask.removeTask(protocolTask);
            }
        }
        addMessageCategoriesToComTask(comTaskInfo, comTask);
        comTask.save();
        return Response.ok(ComTaskInfo.from(comTask)).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_COMMUNICATION_INFRASTRUCTURE)
    public Response deleteComTask(@PathParam("id") long id) {
        ComTask comTask = taskService.findComTask(id).orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));
        comTask.delete();
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/categories")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_COMMUNICATION_INFRASTRUCTURE)
    public PagedInfoList getCategories(@BeanParam QueryParameters queryParameters) {
        List<CategoryInfo> categoryInfos = CategoryInfo.from(ListPager.of(Arrays.asList(Categories.values())).from(queryParameters).find());
        return PagedInfoList.asJson("data", categoryInfos, queryParameters);
    }

    @GET
    @Path("/actions")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_COMMUNICATION_INFRASTRUCTURE)
    public PagedInfoList getActions(@Context UriInfo uriInfo, @BeanParam QueryParameters queryParameters) {
        Optional<String> categoryParameter = Optional.ofNullable(uriInfo.getQueryParameters().getFirst("category"));
        if (categoryParameter.isPresent()) {
            List<ActionInfo> actionInfos = ActionInfo.from(ListPager.of(
                    Categories.valueOf(categoryParameter.get().toUpperCase()).getActions()).from(queryParameters).find());
            return PagedInfoList.asJson("data", actionInfos, queryParameters);
        }
        throw new WebApplicationException("No \"category\" query property is present",
                Response.status(Response.Status.BAD_REQUEST).entity("No \"category\" query property is present").build());
    }

    @GET
    @Path("/messages")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_COMMUNICATION_INFRASTRUCTURE)
    public Response getMessageCategories(@Context UriInfo uriInfo, @BeanParam QueryParameters queryParameters) {
        Stream<DeviceMessageCategory> messageCategoriesStream = deviceMessageSpecificationService.allCategories().stream();
        String availableFor = uriInfo.getQueryParameters().getFirst("availableFor");
        if (availableFor != null) {
            ComTask comTask = taskService.findComTask(Long.parseLong(availableFor)).orElse(null);
            if (comTask != null){
                List<Integer> categoriesInComTask = getMessageCategoriesIdsInComTask(comTask);
                messageCategoriesStream = messageCategoriesStream.filter(obj -> !categoriesInComTask.contains(obj.getId()));
            }
        }
        List<MessageCategoryInfo> infos = messageCategoriesStream
                .map(MessageCategoryInfo::from)
                .sorted((c1, c2) -> c1.name.compareToIgnoreCase(c2.name))
                .collect(Collectors.toList());
        return Response.ok(infos).build();
    }

    private List<Integer> getMessageCategoriesIdsInComTask(ComTask comTask) {
        List<Integer> categoriesInComTask = new ArrayList<>();
        for (ProtocolTask protocolTask : comTask.getProtocolTasks()) {
            if (protocolTask instanceof MessagesTask){
                MessagesTask task = (MessagesTask) protocolTask;
                categoriesInComTask.addAll(task.getDeviceMessageCategories().stream().map(cat -> cat.getId()).collect(Collectors.toList()));
            }
        }
        return categoriesInComTask;
    }
}