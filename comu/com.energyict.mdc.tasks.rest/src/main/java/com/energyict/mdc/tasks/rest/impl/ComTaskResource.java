/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.MessagesTask;
import com.energyict.mdc.common.tasks.ProtocolTask;
import com.energyict.mdc.engine.config.security.Privileges;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.rest.Categories;
import com.energyict.mdc.tasks.rest.impl.util.ResourceHelper;

import javax.annotation.security.RolesAllowed;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/comtasks")
public class ComTaskResource {
    private TaskService taskService;
    private MasterDataService masterDataService;
    private DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final ResourceHelper resourceHelper;
    private final Thesaurus thesaurus;

    @Inject
    public ComTaskResource(TaskService taskService, MasterDataService masterDataService, DeviceMessageSpecificationService deviceMessageSpecificationService, Thesaurus thesaurus, ResourceHelper resourceHelper) {
        this.taskService = taskService;
        this.masterDataService = masterDataService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.thesaurus = thesaurus;
        this.resourceHelper = resourceHelper;
    }

    @GET @Transactional

    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION, Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION})
    public PagedInfoList getComTasks(@BeanParam JsonQueryParameters queryParameters) {
        List<ComTaskInfo> comTaskInfos =
                ComTaskInfo.from(ListPager.of(taskService.findAllUserComTasks(), new ComTaskComparator()).from(queryParameters).find());
        return PagedInfoList.fromPagedList("data", comTaskInfos, queryParameters);
    }

    @GET @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION, Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION})
    public Response getComTask(@PathParam("id") long id) {
        ComTask comTask = taskService.findComTask(id).orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));
        return Response.status(Response.Status.OK).entity(ComTaskInfo.fullFrom(comTask, thesaurus)).build();
    }

    @POST @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public Response addComTask(ComTaskInfo comTaskInfo) {
        ComTask newComTask = taskService.newComTask(comTaskInfo.name);
        newComTask.setManualSystemTask(comTaskInfo.systemTask);

        for (ProtocolTaskInfo protocolTaskInfo : comTaskInfo.commands) {
            Categories category = Categories.valueOf(protocolTaskInfo.categoryId.toUpperCase());
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
                .map(category -> deviceMessageSpecificationService.findCategoryById(category.id))
                .flatMap(Functions.asStream())
                .collect(Collectors.toList());
    }

    @PUT @Transactional
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public Response updateComTask(@PathParam("id") long id, ComTaskInfo comTaskInfo) {

        ComTask comTask = resourceHelper.lockComTaskOrThrowException(comTaskInfo);
        comTask.setName(comTaskInfo.name);
        comTask.setManualSystemTask(comTaskInfo.systemTask);
        comTask.setMaxNrOfTries(comTaskInfo.maxNrOfTries);
        List<ProtocolTask> currentProtocolTasks = new ArrayList<>(comTask.getProtocolTasks());

        currentProtocolTasks.stream()
                .filter(protocolTask -> infoNoLongerContainsTask(comTaskInfo, protocolTask))
                .forEach(comTask::removeTask);

        for (ProtocolTaskInfo protocolTaskInfo : comTaskInfo.commands) {
            Categories category = Categories.valueOf(protocolTaskInfo.categoryId.toUpperCase());
            if (protocolTaskInfo.id != null) {
                comTask.getProtocolTasks().stream().filter(protocolTask -> protocolTask.getId()==protocolTaskInfo.id).findAny().ifPresent(t -> category.updateProtocolTask(masterDataService, t, protocolTaskInfo));
            } else {
                category.createProtocolTask(masterDataService, comTask, protocolTaskInfo);
            }
        }

        addMessageCategoriesToComTask(comTaskInfo, comTask);
        comTask.save();
        return Response.ok(ComTaskInfo.from(comTask)).build();
    }

    private boolean infoNoLongerContainsTask(ComTaskInfo comTaskInfo, ProtocolTask protocolTask) {
        return comTaskInfo.commands.stream()
                .noneMatch(info -> Objects.equals(info.id, protocolTask.getId()));
    }

    @DELETE @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION)
    public Response deleteComTask(@PathParam("id") long id, ComTaskInfo info) {
        info.id = id;
        resourceHelper.lockComTaskOrThrowException(info).delete();
        return Response.status(Response.Status.OK).build();
    }

    @GET @Transactional
    @Path("/categories")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION, Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION})
    public PagedInfoList getCategories(@BeanParam JsonQueryParameters queryParameters) {
        List<CategoryInfo> categoryInfos = CategoryInfo.from(ListPager.of(Arrays.asList(Categories.values())).from(queryParameters).find(), thesaurus);
        return PagedInfoList.fromPagedList("data", categoryInfos, queryParameters);
    }

    @GET @Transactional
    @Path("/actions")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION, Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION})
    public PagedInfoList getActions(@QueryParam("category") String categoryParameter, @BeanParam JsonQueryParameters queryParameters) {
        if (categoryParameter != null) {
            Categories categories;
            try {
                categories = Categories.valueOf(categoryParameter.toUpperCase());
            } catch (IllegalArgumentException x) {
                String errorMsg = "The category '" + categoryParameter.toUpperCase() + "' is unknown";
                throw new WebApplicationException(errorMsg, Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).build());
            }
            List<ActionInfo> actionInfos = ActionInfo.from(ListPager.of(categories.getActions()).from(queryParameters).find(), thesaurus);
            return PagedInfoList.fromPagedList("data", actionInfos, queryParameters);
        }
        throw new WebApplicationException("No \"category\" query property is present",
                Response.status(Response.Status.BAD_REQUEST).entity("No \"category\" query property is present").build());
    }

    @GET @Transactional
    @Path("/messages")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION, Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION})
    public Response getMessageCategories(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        Stream<DeviceMessageCategory> messageCategoriesStream = deviceMessageSpecificationService.filteredCategoriesForComTaskDefinition().stream();
        String availableFor = uriInfo.getQueryParameters().getFirst("availableFor");
        if (availableFor != null) {
            ComTask comTask = taskService.findComTask(Long.parseLong(availableFor)).orElse(null);
            if (comTask != null) {
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
        return comTask
                .getProtocolTasks()
                .stream()
                .filter(protocolTask -> protocolTask instanceof MessagesTask)
                .map(MessagesTask.class::cast)
                .map(MessagesTask::getDeviceMessageCategories)
                .flatMap(Collection::stream)
                .map(DeviceMessageCategory::getId)
                .collect(Collectors.toList());
    }

}