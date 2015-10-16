package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.SingleComTaskComTaskExecution;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

public class ComTaskExecutionInfoFactory extends SelectableFieldFactory<ComTaskExecutionInfo, ComTaskExecution> {

    private final ExceptionFactory exceptionFactory;
    private final ConnectionTaskService connectionTaskService;
    private final SchedulingService schedulingService;
    private final TaskService taskService;

    @Inject
    public ComTaskExecutionInfoFactory(ExceptionFactory exceptionFactory, ConnectionTaskService connectionTaskService, SchedulingService schedulingService, TaskService taskService) {
        this.exceptionFactory = exceptionFactory;
        this.connectionTaskService = connectionTaskService;
        this.schedulingService = schedulingService;
        this.taskService = taskService;
    }

    public ComTaskExecutionInfo from(ComTaskExecution comTaskExecution, UriInfo uriInfo, Collection<String> fields) {
        ComTaskExecutionInfo info = new ComTaskExecutionInfo();
        copySelectedFields(info, comTaskExecution, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<ComTaskExecutionInfo, ComTaskExecution>> buildFieldMap() {
        Map<String, PropertyCopier<ComTaskExecutionInfo, ComTaskExecution>> map = new HashMap<>();
        map.put("id", (comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.id = comTaskExecution.getId());
        map.put("link", ((comTaskExecutionInfo, comTaskExecution, uriInfo) ->
            comTaskExecutionInfo.link = Link.fromUriBuilder(uriInfo.
                    getBaseUriBuilder().
                    path(ComTaskExecutionResource.class).
                    path(ComTaskExecutionResource.class, "getComTaskExecution")).
                    rel(LinkInfo.REF_SELF).
                    title("Communication task execution").
                    build(comTaskExecution.getDevice().getmRID(), comTaskExecution.getId())
        ));
        map.put("device", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> {
            comTaskExecutionInfo.device = new LinkInfo();
            comTaskExecutionInfo.device.id = comTaskExecution.getDevice().getId();
            comTaskExecutionInfo.device.link = Link.fromUriBuilder(uriInfo.
                    getBaseUriBuilder().
                    path(DeviceResource.class).
                    path(DeviceResource.class, "getDevice")).
                    rel(LinkInfo.REF_PARENT).
                    title("Device").
                    build(comTaskExecution.getDevice().getmRID());
        }));
        map.put("connectionTask", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> {
            if (comTaskExecution.getConnectionTask().isPresent()) {
                comTaskExecutionInfo.connectionTask = new LinkInfo();
                comTaskExecutionInfo.connectionTask.id = comTaskExecution.getConnectionTask().get().getId();
                comTaskExecutionInfo.connectionTask.link =
                        Link.fromUriBuilder(uriInfo.
                            getBaseUriBuilder().
                            path(ConnectionTaskResource.class).
                            path(ConnectionTaskResource.class, "getConnectionTask").
                            resolveTemplate("mrid", comTaskExecution.getDevice().getmRID()).
                            resolveTemplate("connectionTaskId", comTaskExecution.getConnectionTask().get().getId())).
                        rel(LinkInfo.REF_RELATION).
                        title("Connection Task").
                        build();
            }
        }));
        map.put("comTask", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> {
            if (SingleComTaskComTaskExecution.class.isAssignableFrom(comTaskExecution.getClass())) {
                UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                        .path(ComTaskResource.class)
                        .path(ComTaskResource.class, "getComTask");
                comTaskExecutionInfo.comTask = new LinkInfo();
                ComTask comTask1 = ((SingleComTaskComTaskExecution) comTaskExecution).getComTask();
                comTaskExecutionInfo.comTask.id = comTask1.getId();
                comTaskExecutionInfo.comTask.link = Link.fromUriBuilder(uriBuilder).rel(LinkInfo.REF_RELATION).title("Communication task").build(comTask1.getId());
            }
        }));
        map.put("nextExecution", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.nextExecution = comTaskExecution.getNextExecutionTimestamp()));
        map.put("plannedNextExecution", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.plannedNextExecution = comTaskExecution.getPlannedNextExecutionTimestamp()));
        map.put("priority", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.priority = comTaskExecution.getPlannedPriority()));
        map.put("ignoreNextExecutionSpecForInbound", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.ignoreNextExecutionSpecForInbound = comTaskExecution.isIgnoreNextExecutionSpecsForInbound()));
        map.put("schedule", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> {
            if (comTaskExecution.usesSharedSchedule()) {
                UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                        .path(ComScheduleResource.class)
                        .path(ComScheduleResource.class, "getComSchedule");
                comTaskExecutionInfo.schedule = new LinkInfo();
                comTaskExecutionInfo.schedule.link = Link.fromUriBuilder(uriBuilder)
                        .rel(LinkInfo.REF_RELATION)
                        .title("Shared communication schedule")
                        .build(((ScheduledComTaskExecution) comTaskExecution).getComSchedule().getId());
                comTaskExecutionInfo.schedule.id = ((ScheduledComTaskExecution) comTaskExecution).getComSchedule().getId();
            }
        }));
        map.put("type", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> {
            comTaskExecutionInfo.type = comTaskExecution.isScheduledManually()?
                    (comTaskExecution.isAdHoc()?
                            ComTaskExecutionType.AdHoc:
                            ComTaskExecutionType.ManualSchedule):
                        ComTaskExecutionType.SharedSchedule;
        }));
        map.put("lastCommunicationStart", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.lastCommunicationStart = comTaskExecution.getLastExecutionStartTimestamp()));
        map.put("lastSuccessfulCompletion", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.lastSuccessfulCompletion = comTaskExecution.getLastSuccessfulCompletionTimestamp()));
        map.put("status", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.status = comTaskExecution.getStatus()));

        return map;
    }

    public ManuallyScheduledComTaskExecution createManuallyScheduledComTaskExecution(ComTaskExecutionInfo comTaskExecutionInfo, Device device) {
        if (comTaskExecutionInfo.comTask==null || comTaskExecutionInfo.comTask.id==null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.COM_TASK_EXPECTED);
        }
        if (comTaskExecutionInfo.schedulingSpec==null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.SCHEDULE_SPEC_EXPECTED);
        }
        Optional<ConnectionTask> connectionTask = getConnectionTaskOrThrowException(comTaskExecutionInfo);

        ComTask comTask = taskService.findComTask(comTaskExecutionInfo.comTask.id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_COM_TASK));

        ComTaskEnablement comTaskEnablement = device.getDeviceConfiguration().getComTaskEnablementFor(comTask)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.COM_TASK_NOT_ENABLED));

        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> builder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, comTaskExecutionInfo.schedulingSpec.asTemporalExpression());
        if (connectionTask.isPresent()) {
            builder.connectionTask(connectionTask.get());
        }
        if (comTaskExecutionInfo.priority!=null) {
            builder.priority(comTaskExecutionInfo.priority);
        }
        if (comTaskExecutionInfo.ignoreNextExecutionSpecForInbound!=null) {
            builder.ignoreNextExecutionSpecForInbound(comTaskExecutionInfo.ignoreNextExecutionSpecForInbound);
        }
        ManuallyScheduledComTaskExecution manuallyScheduledComTaskExecution = builder.add();
        device.save();
        return manuallyScheduledComTaskExecution;
    }


    public ScheduledComTaskExecution createSharedScheduledComtaskExecution(ComTaskExecutionInfo comTaskExecutionInfo, Device device) {
        if (comTaskExecutionInfo.comTask!=null && comTaskExecutionInfo.comTask.id!=null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.TYPE_DOES_NOT_SUPPORT_COM_TASK);
        }
        if (comTaskExecutionInfo.schedulingSpec!=null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.TYPE_DOES_NOT_SUPPORT_SCHEDULE_SPEC);
        }
        if (comTaskExecutionInfo.schedule==null || comTaskExecutionInfo.schedule.id==null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.SCHEDULE_EXPECTED);
        }
        Optional<ConnectionTask> connectionTask = getConnectionTaskOrThrowException(comTaskExecutionInfo);
        ComSchedule comSchedule = schedulingService.findSchedule(comTaskExecutionInfo.schedule.id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_COM_SCHEDULE));

        ComTaskExecutionBuilder<ScheduledComTaskExecution> builder = device.newScheduledComTaskExecution(comSchedule);
        if (connectionTask.isPresent()) {
            builder.connectionTask(connectionTask.get());
        }
        if (comTaskExecutionInfo.priority!=null) {
            builder.priority(comTaskExecutionInfo.priority);
        }
        if (comTaskExecutionInfo.ignoreNextExecutionSpecForInbound!=null) {
            builder.ignoreNextExecutionSpecForInbound(comTaskExecutionInfo.ignoreNextExecutionSpecForInbound);
        }
        ScheduledComTaskExecution scheduledComTaskExecution = builder.add();
        device.save();
        return scheduledComTaskExecution;
    }

    public SingleComTaskComTaskExecution createAdHocComtaskExecution(ComTaskExecutionInfo comTaskExecutionInfo, Device device) {
        if (comTaskExecutionInfo.comTask ==null || comTaskExecutionInfo.comTask.id==null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.COM_TASK_EXPECTED);
        }
        if (comTaskExecutionInfo.schedulingSpec!=null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.TYPE_DOES_NOT_SUPPORT_SCHEDULE_SPEC);
        }
        Optional<ConnectionTask> connectionTask = getConnectionTaskOrThrowException(comTaskExecutionInfo);
        ComTask comTask = taskService.findComTask(comTaskExecutionInfo.comTask.id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_COM_TASK));

        ComTaskEnablement comTaskEnablement = device.getDeviceConfiguration().getComTaskEnablementFor(comTask)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.COM_TASK_NOT_ENABLED));

        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> builder = device.newAdHocComTaskExecution(comTaskEnablement);
        if (connectionTask.isPresent()) {
            builder.connectionTask(connectionTask.get());
        }
        if (comTaskExecutionInfo.priority!=null) {
            builder.priority(comTaskExecutionInfo.priority);
        }
        if (comTaskExecutionInfo.ignoreNextExecutionSpecForInbound!=null) {
            builder.ignoreNextExecutionSpecForInbound(comTaskExecutionInfo.ignoreNextExecutionSpecForInbound);
        }
        ManuallyScheduledComTaskExecution manuallyScheduledComTaskExecution = builder.add();
        device.save();
        return manuallyScheduledComTaskExecution;
    }

    protected Optional<ConnectionTask> getConnectionTaskOrThrowException(ComTaskExecutionInfo comTaskExecutionInfo) {
        if (comTaskExecutionInfo.connectionTask!=null && comTaskExecutionInfo.connectionTask.id!=null) {
            return Optional.of(connectionTaskService.findConnectionTask(comTaskExecutionInfo.connectionTask.id)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_CONNECTION_TASK)));
        }
        return Optional.empty();
    }

}
