/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.users.User;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.rest.DeviceStagesRestricted;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.tasks.TaskService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
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
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@DeviceStagesRestricted({EndDeviceStage.POST_OPERATIONAL})
public class DeviceScheduleResource {

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final TaskService taskService;
    private final ComTaskExecutionPrivilegeCheck comTaskExecutionPrivilegeCheck;

    @Inject
    public DeviceScheduleResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, TaskService taskService, ComTaskExecutionPrivilegeCheck comTaskExecutionPrivilegeCheck) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.taskService = taskService;
        this.comTaskExecutionPrivilegeCheck = comTaskExecutionPrivilegeCheck;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response getAllComTaskExecutions(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter queryFilter) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        List<ComTaskExecution> comTaskExecutions = device.getComTaskExecutions().stream()
                .filter(comTaskExecution -> !comTaskExecution.getComTask().isSystemComTask())
                .collect(Collectors.toList());
        List<ComTaskEnablement> comTaskEnablements = deviceConfiguration.getComTaskEnablements().stream()
                .filter(comTaskEnablement -> !comTaskEnablement.getComTask().isSystemComTask())
                .collect(Collectors.toList());
        List<DeviceSchedulesInfo> deviceSchedulesInfos = DeviceSchedulesInfo.from(comTaskExecutions, comTaskEnablements, device);
        return Response.ok(PagedInfoList.fromPagedList("schedules", deviceSchedulesInfos, queryParameters)).build();
    }


    @GET
    @Path("/{comTaskId}")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response getComTask(@PathParam("name") String name, @PathParam("comTaskId") Long comTaskId) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        List<ComTaskExecution> comTaskExecutions = device.getComTaskExecutions();
        List<ComTaskEnablement> comTaskEnablements = deviceConfiguration.getComTaskEnablements();

        Optional<ComTaskExecution> cte = comTaskExecutions.stream()
                .filter(comTaskExecution -> comTaskExecution.getComTask().getId() == comTaskId)
                .findFirst();
        DeviceSchedulesInfo info = null;
        if (cte.isPresent()) {
            ComTaskExecution comTaskExecution = cte.get();
            if (comTaskExecution.isScheduledManually() && !comTaskExecution.isAdHoc()) {
                info = DeviceSchedulesInfo.fromManual(comTaskExecution);
            } else if (comTaskExecution.usesSharedSchedule()) {
                info = DeviceSchedulesInfo.fromScheduled(comTaskExecution);
            } else if (comTaskExecution.isAdHoc()) {
                info = DeviceSchedulesInfo.fromAdHoc(comTaskExecution);
            }
        } else {
            Optional<ComTaskEnablement> comTaskEnablement = comTaskEnablements.stream()
                    .filter(c -> c.getComTask().getId() == comTaskId)
                    .findFirst();
            if (comTaskEnablement.isPresent()) {
                info = DeviceSchedulesInfo.fromEnablement(comTaskEnablement.get(), device);
            }
        }
        if (info != null) {
            return Response.ok(info).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    private void checkForNoActionsAllowedOnSystemComTaskExecutions(long comTaskExecId) {
        ComTaskExecution comTaskExecution = resourceHelper.findComTaskExecutionOrThrowException(comTaskExecId);
        if (comTaskExecution.getComTask().isSystemComTask()) {
            throw exceptionFactory.newException(MessageSeeds.CAN_NOT_PERFORM_ACTION_ON_SYSTEM_COMTASK);
        }
    }

    private void checkForNoActionsAllowedOnSystemComTask(long comTaskId) {
        Optional<ComTask> comTask = taskService.findComTask(comTaskId).filter(ComTask::isSystemComTask);
        if (comTask.isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.CAN_NOT_PERFORM_ACTION_ON_SYSTEM_COMTASK);
        }
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response createComTaskExecution(@PathParam("name") String name, DeviceSchedulesInfo schedulingInfo, @Context SecurityContext securityContext) {
        // In this method, id == id of comtask
        checkForNoActionsAllowedOnSystemComTask(schedulingInfo.id);
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        User user = (User) securityContext.getUserPrincipal();
        for (ComTaskEnablement comTaskEnablement : deviceConfiguration.getComTaskEnablements()) {
            if (comTaskEnablement.getComTask().getId() == schedulingInfo.id && !comTaskExecutionPrivilegeCheck.canExecute(comTaskEnablement.getComTask(), user)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        }
        for (ComTaskEnablement comTaskEnablement : deviceConfiguration.getComTaskEnablements()) {
            if (comTaskEnablement.getComTask().getId() == schedulingInfo.id && schedulingInfo.schedule != null) {
                boolean comTaskExecutionExists = false;
                for (ComTaskExecution comTaskExecution : device.getComTaskExecutions()) {
                    if (comTaskExecution.isAdHoc() && comTaskExecution.getComTask().getId() == comTaskEnablement.getComTask().getId()) {
                        comTaskExecution.getUpdater().createNextExecutionSpecs(schedulingInfo.schedule.asTemporalExpression()).update();
                        comTaskExecutionExists = true;
                    } else if (comTaskExecution.getComTask().getId() == comTaskEnablement.getComTask().getId()) {
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }
                }
                if (!comTaskExecutionExists) {
                    ComTaskExecutionBuilder builder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, schedulingInfo.schedule.asTemporalExpression());
                    if (comTaskEnablement.hasPartialConnectionTask()) {
                        device.getConnectionTasks()
                                .stream()
                                .filter(x -> x.getPartialConnectionTask().getId() == comTaskEnablement.getPartialConnectionTask().get().getId())
                                .forEach(builder::connectionTask);
                    }
                    builder.add();
                }
            }
        }
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response updateComTaskExecution(@PathParam("name") String name, DeviceSchedulesInfo info, @Context SecurityContext securityContext) {
        // In this method, id == id of comtaskexec
        checkForNoActionsAllowedOnSystemComTaskExecutions(info.id);
        ComTaskExecution comTaskExecution = resourceHelper.findComTaskExecutionOrThrowException(info.id);
        comTaskExecution.getConnectionTask().ifPresent(ct -> resourceHelper.getLockedConnectionTask(ct.getId(), ct.getVersion()));
        comTaskExecution = resourceHelper.lockComTaskExecutionOrThrowException(info);
        User user = (User) securityContext.getUserPrincipal();
        if (!comTaskExecutionPrivilegeCheck.canExecute(comTaskExecution.getComTask(), user)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        if (!(comTaskExecution.isScheduledManually())) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (info.schedule == null) {
            comTaskExecution.getUpdater().removeSchedule().update();
            comTaskExecution.removeSchedule();
        } else {
            comTaskExecution.getUpdater().createNextExecutionSpecs(info.schedule.asTemporalExpression()).update();
        }
        return Response.status(Response.Status.OK).build();
    }

}
