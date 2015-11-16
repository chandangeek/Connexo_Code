package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.DeviceStatesRestricted;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.scheduling.SchedulingService;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@DeviceStatesRestricted({DefaultState.DECOMMISSIONED})
public class DeviceScheduleResource {

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final TaskService taskService;
    private final SchedulingService schedulingService;

    @Inject
    public DeviceScheduleResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, TaskService taskService, SchedulingService schedulingService) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.taskService = taskService;
        this.schedulingService = schedulingService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response getAllComTaskExecutions(@PathParam("mRID") String mrid, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter queryFilter) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        List<ComTaskExecution> comTaskExecutions = device.getComTaskExecutions();
        List<ComTaskEnablement> comTaskEnablements = deviceConfiguration.getComTaskEnablements();
        List<DeviceSchedulesInfo> deviceSchedulesInfos = DeviceSchedulesInfo.from(comTaskExecutions, comTaskEnablements);
        return Response.ok(PagedInfoList.fromPagedList("schedules", deviceSchedulesInfos, queryParameters)).build();
    }

    private void checkForNoActionsAllowedOnSystemComTask(@PathParam("comTaskId") Long comTaskId) {
        Optional<ComTask> comTask = taskService.findComTask(comTaskId);
        comTask.ifPresent(comTask1 -> {
            if(comTask1.isSystemComTask()){
                throw exceptionFactory.newException(MessageSeeds.CAN_NOT_PERFORM_ACTION_ON_SYSTEM_COMTASK);
            }
        });
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response createComTaskExecution(@PathParam("mRID") String mrid, DeviceSchedulesInfo schedulingInfo) {
        checkForNoActionsAllowedOnSystemComTask(schedulingInfo.id);
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        for (ComTaskEnablement comTaskEnablement : deviceConfiguration.getComTaskEnablements()) {
            if (comTaskEnablement.getComTask().getId() == schedulingInfo.id) {
                if (schedulingInfo.schedule != null) {
                    boolean comTaskExecutionExists = false;
                    for (ComTaskExecution comTaskExecution : device.getComTaskExecutions()) {
                        if (comTaskExecution.isAdHoc() && comTaskExecution.getComTasks().get(0).getId() == comTaskEnablement.getComTask().getId()) {
                            ((ManuallyScheduledComTaskExecution) comTaskExecution).getUpdater().scheduleAccordingTo(schedulingInfo.schedule.asTemporalExpression()).update();
                            comTaskExecutionExists = true;
                        }
                    }
                    if (!comTaskExecutionExists) {
                        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> builder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, schedulingInfo.schedule.asTemporalExpression());
                        if (comTaskEnablement.hasPartialConnectionTask()) {
                            for (ConnectionTask<?, ?> connectionTask : device.getConnectionTasks()) {
                                if (connectionTask.getPartialConnectionTask().getId() == comTaskEnablement.getPartialConnectionTask().get().getId()) {
                                    builder.connectionTask(connectionTask);
                                }
                            }
                        }
                        builder.add();
                    }
                } else {
                    boolean comTaskExecutionExists = false;
                    for (ComTaskExecution comTaskExecution : device.getComTaskExecutions()) {
                        if (comTaskExecution.isAdHoc() && comTaskExecution.getComTasks().get(0).getId() == comTaskEnablement.getComTask().getId()) {
                            comTaskExecution.scheduleNow();
                            comTaskExecutionExists = true;
                        }
                    }
                    if (!comTaskExecutionExists) {
                        device.newAdHocComTaskExecution(comTaskEnablement).scheduleNow().add();
                    }
                }
            }
        }
        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response updateComTaskExecution(@PathParam("mRID") String mrid, DeviceSchedulesInfo info) {
        checkForNoActionsAllowedOnSystemComTask(info.id);
        ComTaskExecution comTaskExecution = resourceHelper.lockComTaskExecutionOrThrowException(info);
        Device device = comTaskExecution.getDevice();
        if (info.schedule == null) {
            device.removeComTaskExecution(comTaskExecution);
        } else {
            ((ManuallyScheduledComTaskExecution) comTaskExecution).getUpdater().scheduleAccordingTo(info.schedule.asTemporalExpression()).update();
        }
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    @Path("/{comTaskExecutionId}")
    public Response deleteComTaskExecution(@PathParam("mRID") String mrid, @PathParam("comTaskExecutionId") long id, DeviceSchedulesInfo info) {
        info.id = id;
        checkForNoActionsAllowedOnSystemComTask(id);
        ComTaskExecution comTaskExecution = resourceHelper.lockComTaskExecutionOrThrowException(info);
        comTaskExecution.getDevice().removeComTaskExecution(comTaskExecution);
        return Response.ok().build();
    }

}
