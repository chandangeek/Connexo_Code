/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.time.TemporalExpression;
import com.energyict.mdc.common.rest.IdListBuilder;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.ComScheduleBuilder;
import com.energyict.mdc.scheduling.rest.ComTaskInfo;
import com.energyict.mdc.scheduling.security.Privileges;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/schedules")
public class SchedulingResource {

    private final SchedulingService schedulingService;
    private final DeviceService deviceService;
    private final Clock clock;
    private final DeviceConfigurationService deviceConfigurationService;
    private final TaskService taskService;
    private final ResourceHelper resourceHelper;

    @Inject
    public SchedulingResource(SchedulingService schedulingService, DeviceService deviceService, Clock clock, DeviceConfigurationService deviceConfigurationService, TaskService taskService, ResourceHelper resourceHelper) {
        this.schedulingService = schedulingService;
        this.deviceService = deviceService;
        this.clock = clock;
        this.deviceConfigurationService = deviceConfigurationService;
        this.taskService = taskService;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_SHARED_COMMUNICATION_SCHEDULE, Privileges.Constants.VIEW_SHARED_COMMUNICATION_SCHEDULE})
    public PagedInfoList getSchedules(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter queryFilter) {
        String deviceName = queryFilter.hasProperty("deviceName") ? queryFilter.getString("deviceName") : null;
        boolean available = queryFilter.hasProperty("available") ? queryFilter.getBoolean("available") : false;
        List<ComSchedule> comSchedules = schedulingService.getAllSchedules();
        Collections.sort(comSchedules, (o1, o2) -> o1.getName().compareTo(o2.getName()));
        if (deviceName != null && available) {
            filterAvailableSchedulesOnly(deviceName, comSchedules);
        }
        comSchedules = ListPager.of(comSchedules).from(queryParameters).find();

        List<ComScheduleInfo> comScheduleInfos = new ArrayList<>();
        for (ComSchedule comSchedule : comSchedules) {
            comScheduleInfos.add(ComScheduleInfo.from(comSchedule, isInUse(comSchedule), clock.instant()));
        }
        return PagedInfoList.fromPagedList("schedules", comScheduleInfos, queryParameters);
    }

    @GET
    @Path("/used")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_SHARED_COMMUNICATION_SCHEDULE, Privileges.Constants.VIEW_SHARED_COMMUNICATION_SCHEDULE})
    public PagedInfoList getUsedSchedules(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter queryFilter) {
        String deviceName = queryFilter.hasProperty("deviceId") ? queryFilter.getString("deviceId") : null;
        Device device = deviceService.findDeviceByName(deviceName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid name"));

        List<ComSchedule> usedSchedules = device.getComTaskExecutions()
                .stream()
                .filter(ComTaskExecution::usesSharedSchedule)
                .map(comTaskExecution -> comTaskExecution.getComSchedule().get())
                .distinct()
                .sorted((o1, o2) -> o1.getName().compareTo(o2.getName()))
                .collect(Collectors.toList());
        usedSchedules = ListPager.of(usedSchedules).from(queryParameters).find();

        List<ComScheduleInfo> comScheduleInfos = usedSchedules.stream()
                .map(comSchedule -> ComScheduleInfo.from(comSchedule, isInUse(comSchedule), clock.instant()))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("schedules", comScheduleInfos, queryParameters);
    }

    private void filterAvailableSchedulesOnly(String deviceName, List<ComSchedule> comSchedules) {
        deviceService.findDeviceByName(deviceName).ifPresent(device -> {
                List<ComTaskExecution> comTaskExecutions = device.getComTaskExecutions();
                List<ComTaskEnablement> comTaskEnablements = device.getDeviceConfiguration().getComTaskEnablements();
                Iterator<ComSchedule> iterator = comSchedules.iterator();
                while(iterator.hasNext()) {
                    ComSchedule comSchedule = iterator.next();
                    if (!isValidComSchedule(comSchedule, comTaskEnablements, comTaskExecutions)) {
                        iterator.remove();
                    }
                }
        });
    }

    private boolean isValidComSchedule(ComSchedule comSchedule, List<ComTaskEnablement> comTaskEnablements, List<ComTaskExecution> comTaskExecutions) {
        boolean alreadyScheduledExecutionForTaskInSchedule = comTaskExecutions.stream()
                .filter(ComTaskExecution::usesSharedSchedule)
                .map(ComTaskExecution::getComTask)
                .filter(comSchedule.getComTasks()::contains)
                .findFirst()
                .isPresent();
        return !alreadyScheduledExecutionForTaskInSchedule && comTaskEnablements.stream()
                .filter(comTaskEnablement -> comSchedule.getComTasks().contains(comTaskEnablement.getComTask()))
                .findFirst()
                .isPresent();
    }

    @GET
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_SHARED_COMMUNICATION_SCHEDULE, Privileges.Constants.VIEW_SHARED_COMMUNICATION_SCHEDULE})
    public ComScheduleInfo getSchedules(@PathParam("id") long id) {
        ComSchedule comSchedule = resourceHelper.findComScheduleOrThrowException(id);
        return ComScheduleInfo.from(comSchedule, isInUse(comSchedule), clock.instant());
    }


    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_SHARED_COMMUNICATION_SCHEDULE)
    public Response createSchedule(ComScheduleInfo comScheduleInfo) {
        ComScheduleBuilder comScheduleBuilder = schedulingService.newComSchedule(comScheduleInfo.name, comScheduleInfo.temporalExpression.asTemporalExpression(),
                comScheduleInfo.startDate == null ? null : comScheduleInfo.startDate);
        comScheduleBuilder.mrid(comScheduleInfo.mRID);
        if (comScheduleInfo.comTaskUsages != null) {
            getComTasks(comScheduleInfo.comTaskUsages).stream().forEach(comScheduleBuilder::addComTask);
        }
        ComSchedule comSchedule = comScheduleBuilder.build();
        return Response.status(Response.Status.CREATED).entity(ComScheduleInfo.from(comSchedule, false, clock.instant())).build();
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_SHARED_COMMUNICATION_SCHEDULE)
    public Response deleteSchedules(@PathParam("id") long id, ComScheduleInfo info) {
        info.id = id;
        ComSchedule comSchedule = resourceHelper.lockComScheduleOrThrowException(info);
        if (this.isInUse(comSchedule)) {
            comSchedule.makeObsolete();
        } else {
            comSchedule.delete();
        }
        return Response.noContent().build();
    }

    @PUT
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_SHARED_COMMUNICATION_SCHEDULE)
    public ComScheduleInfo updateSchedules(@PathParam("id") long id, ComScheduleInfo comScheduleInfo) {
        ComSchedule comSchedule = resourceHelper.lockComScheduleOrThrowException(comScheduleInfo);
        comSchedule.setName(comScheduleInfo.name);
        comSchedule.setTemporalExpression(comScheduleInfo.temporalExpression == null ? null : comScheduleInfo.temporalExpression.asTemporalExpression());
        comSchedule.setStartDate(comScheduleInfo.startDate == null ? null : comScheduleInfo.startDate);
        comSchedule.setmRID(comScheduleInfo.mRID);
        if (comScheduleInfo.comTaskUsages != null) {
            updateTasks(comSchedule, comScheduleInfo.comTaskUsages);
        }
        comSchedule.update();
        return ComScheduleInfo.from(comSchedule, isInUse(comSchedule), clock.instant());
    }

    private List<ComTask> getComTasks(Collection<ComTaskInfo> comTaskUsages) {
        List<ComTask> comTasks = new ArrayList<>();
        for (ComTaskInfo comTaskInfo : comTaskUsages) {
            ComTask comTask = taskService.findComTask(comTaskInfo.id).orElseThrow(() -> new WebApplicationException("No ComTask with id " + comTaskInfo.id, Response.Status.BAD_REQUEST));
            comTasks.add(comTask);
        }
        return comTasks;
    }

    private void updateTasks(ComSchedule comSchedule, List<ComTaskInfo> comTaskUsages) {
        Map<Long, ComTaskInfo> newComTaskIdMap = asIdz(comTaskUsages);
        for (ComTask comTask : comSchedule.getComTasks()) {
            if (newComTaskIdMap.containsKey(comTask.getId())) {
                // Updating ComTasks not allowed here
                newComTaskIdMap.remove(comTask.getId());
            } else {
                comSchedule.removeComTask(comTask);
            }
        }
        getComTasks(newComTaskIdMap.values()).stream().forEach(comSchedule::addComTask);
    }

    private Map<Long, ComTaskInfo> asIdz(Collection<ComTaskInfo> comTaskInfos) {
        Map<Long, ComTaskInfo> comTaskIdMap = new HashMap<>();
        for (ComTaskInfo comTaskInfo : comTaskInfos) {
            comTaskIdMap.put(comTaskInfo.id, comTaskInfo);
        }
        return comTaskIdMap;
    }

    @GET
    @Transactional
    @Path("/{id}/comTasks")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_SHARED_COMMUNICATION_SCHEDULE, Privileges.Constants.VIEW_SHARED_COMMUNICATION_SCHEDULE})
    public Response getComTasks(@PathParam("id") long id, @BeanParam JsonQueryFilter queryFilter) {
        ComSchedule comSchedule = resourceHelper.findComScheduleOrThrowException(id);
        if (queryFilter.hasProperty("available") && queryFilter.getBoolean("available")) {
            return Response.ok().entity(ComTaskInfo.from(getAvailableComTasksExcludingAlreadyAssigned(comSchedule))).build();
        } else {
            return Response.ok().entity(ComTaskInfo.from(comSchedule.getComTasks())).build();
        }
    }

    @PUT
    @Transactional
    @Path("/preview")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_SHARED_COMMUNICATION_SCHEDULE)
    public Response generatePreviewForSchedule(PreviewInfo previewInfo) {
        if (previewInfo.temporalExpression == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.CAN_NOT_BE_EMPTY, "temporalExpression");
        }
        if (previewInfo.temporalExpression.every == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.CAN_NOT_BE_EMPTY, "temporalExpression.every");
        }
        previewInfo.nextOccurrences = calculateNextOccurrences(previewInfo);
        return Response.ok().entity(previewInfo).build();
    }

    private List<Date> calculateNextOccurrences(PreviewInfo previewInfo) {
        TemporalExpression temporalExpression = previewInfo.temporalExpression.asTemporalExpression();
        List<Date> nextOccurrences = new ArrayList<>();
        Date occurrence = previewInfo.startDate == null ? Date.from(this.clock.instant()) : previewInfo.startDate;
        Calendar latestOccurrence = Calendar.getInstance();
        for (int i = 0; i < 5; i++) {
            latestOccurrence.setTime(occurrence);
            occurrence = temporalExpression.nextOccurrence(latestOccurrence);
            nextOccurrences.add(occurrence);
        }
        return nextOccurrences;
    }

    private List<ComTask> getAvailableComTasksExcludingAlreadyAssigned(ComSchedule comSchedule) {
        List<ComTask> remainingComTasks = new ArrayList<>();
        Map<Long, ComTask> existingComTasks = IdListBuilder.asIdMap(comSchedule.getComTasks());
        for (ComTask availableComTask : taskService.findAllUserComTasks()) {
            if (!existingComTasks.containsKey(availableComTask.getId())) {
                remainingComTasks.add(availableComTask);
            }
        }

        return remainingComTasks;
    }

    private boolean isInUse(ComSchedule comSchedule) {
        return this.deviceService.isLinkedToDevices(comSchedule);
    }

}
