package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.SingleComTaskComTaskExecution;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class ComTaskExecutionInfoFactory extends SelectableFieldFactory<ComTaskExecutionInfo, ComTaskExecution> {

    private final ExceptionFactory exceptionFactory;
    private final ConnectionTaskService connectionTaskService;
    private final SchedulingService schedulingService;
    private final TaskService taskService;
    private final Provider<DeviceInfoFactory> deviceInfoFactoryProvider;
    private final Provider<ConnectionTaskInfoFactory> connectionTaskInfoFactoryProvider;
    private final Provider<ComTaskInfoFactory> comTaskInfoFactoryProvider;
    private final Provider<ComScheduleInfoFactory> comScheduleInfoFactoryProvider;

    @Inject
    public ComTaskExecutionInfoFactory(ExceptionFactory exceptionFactory, ConnectionTaskService connectionTaskService,
                                       SchedulingService schedulingService, TaskService taskService,
                                       Provider<DeviceInfoFactory> deviceInfoFactoryProvider,
                                       Provider<ConnectionTaskInfoFactory> connectionTaskInfoFactoryProvider,
                                       Provider<ComTaskInfoFactory> comTaskInfoFactoryProvider,
                                       Provider<ComScheduleInfoFactory> comScheduleInfoFactoryProvider) {
        this.exceptionFactory = exceptionFactory;
        this.connectionTaskService = connectionTaskService;
        this.schedulingService = schedulingService;
        this.taskService = taskService;
        this.deviceInfoFactoryProvider = deviceInfoFactoryProvider;
        this.connectionTaskInfoFactoryProvider = connectionTaskInfoFactoryProvider;
        this.comTaskInfoFactoryProvider = comTaskInfoFactoryProvider;
        this.comScheduleInfoFactoryProvider = comScheduleInfoFactoryProvider;
    }

    public LinkInfo asLink(ComTaskExecution comTaskExecution, Relation relation, UriInfo uriInfo) {
        ComTaskExecutionInfo info = new ComTaskExecutionInfo();
        copySelectedFields(info,comTaskExecution,uriInfo, Arrays.asList("id","version"));
        info.link = link(comTaskExecution,relation,uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<ComTaskExecution> comTaskExecutions, Relation relation, UriInfo uriInfo) {
        return comTaskExecutions.stream().map(i-> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(ComTaskExecution comTaskExecution, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("ComTask execution")
                .build(comTaskExecution.getDevice().getmRID(), comTaskExecution.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(ComTaskExecutionResource.class)
                .path(ComTaskExecutionResource.class, "getComTaskExecution");
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
        map.put("version", (comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.version = comTaskExecution.getVersion());
        map.put("link", ((comTaskExecutionInfo, comTaskExecution, uriInfo) ->
            comTaskExecutionInfo.link = link(comTaskExecution, Relation.REF_SELF, uriInfo)));
        map.put("device", ((comTaskExecutionInfo, comTaskExecution, uriInfo) ->
            comTaskExecutionInfo.device = deviceInfoFactoryProvider.get().asLink(comTaskExecution.getDevice(), Relation.REF_PARENT, uriInfo)));
        map.put("connectionTask", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> {
            if (comTaskExecution.getConnectionTask().isPresent()) {
                comTaskExecutionInfo.connectionTask = connectionTaskInfoFactoryProvider.get().asLink(comTaskExecution.getConnectionTask().get(), Relation.REF_RELATION, uriInfo);
            }
        }));
        map.put("comTask", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> {
            if (SingleComTaskComTaskExecution.class.isAssignableFrom(comTaskExecution.getClass())) {
                comTaskExecutionInfo.comTask = comTaskInfoFactoryProvider.get().asLink(((SingleComTaskComTaskExecution) comTaskExecution).getComTask(), Relation.REF_RELATION, uriInfo);
            }
        }));
        map.put("nextExecution", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.nextExecution = comTaskExecution.getNextExecutionTimestamp()));
        map.put("plannedNextExecution", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.plannedNextExecution = comTaskExecution.getPlannedNextExecutionTimestamp()));
        map.put("priority", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.priority = comTaskExecution.getPlannedPriority()));
        map.put("ignoreNextExecutionSpecForInbound", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.ignoreNextExecutionSpecForInbound = comTaskExecution.isIgnoreNextExecutionSpecsForInbound()));
        map.put("schedule", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> {
            if (comTaskExecution.usesSharedSchedule()) {
                comTaskExecutionInfo.schedule = comScheduleInfoFactoryProvider.get().asLink(((ScheduledComTaskExecution) comTaskExecution).getComSchedule(), Relation.REF_RELATION, uriInfo);
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
        Optional<ConnectionTask<?, ?>> connectionTask = getConnectionTaskOptionallyOrThrowException(comTaskExecutionInfo);
        if (connectionTask.isPresent() && comTaskExecutionInfo.useDefaultConnectionTask!=null && comTaskExecutionInfo.useDefaultConnectionTask) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.NOT_POSSIBLE_TO_SUPPLY_BOTH_OR_NONE);
        }
        if (!connectionTask.isPresent() && (comTaskExecutionInfo.useDefaultConnectionTask==null || !comTaskExecutionInfo.useDefaultConnectionTask)) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.NOT_POSSIBLE_TO_SUPPLY_BOTH_OR_NONE);
        }

        ComTask comTask = taskService.findComTask(comTaskExecutionInfo.comTask.id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_COM_TASK));

        ComTaskEnablement comTaskEnablement = device.getDeviceConfiguration().getComTaskEnablementFor(comTask)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.COM_TASK_NOT_ENABLED));

        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> builder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, comTaskExecutionInfo.schedulingSpec.asTemporalExpression());
        if (comTaskExecutionInfo.useDefaultConnectionTask!=null && comTaskExecutionInfo.useDefaultConnectionTask) {
            builder.useDefaultConnectionTask(true);
        } else {
            connectionTask.ifPresent(task -> builder.connectionTask(task));
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
        ComSchedule comSchedule = schedulingService.findSchedule(comTaskExecutionInfo.schedule.id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_COM_SCHEDULE));
        Optional<ConnectionTask<?,?>> connectionTask = getConnectionTaskOptionallyOrThrowException(comTaskExecutionInfo);
        if (connectionTask.isPresent() && comTaskExecutionInfo.useDefaultConnectionTask!=null && comTaskExecutionInfo.useDefaultConnectionTask) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.NOT_POSSIBLE_TO_SUPPLY_BOTH_OR_NONE);
        }
        if (!connectionTask.isPresent() && (comTaskExecutionInfo.useDefaultConnectionTask==null || !comTaskExecutionInfo.useDefaultConnectionTask)) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.NOT_POSSIBLE_TO_SUPPLY_BOTH_OR_NONE);
        }

        ComTaskExecutionBuilder<ScheduledComTaskExecution> builder = device.newScheduledComTaskExecution(comSchedule);

        if (comTaskExecutionInfo.useDefaultConnectionTask!=null && comTaskExecutionInfo.useDefaultConnectionTask) {
            builder.useDefaultConnectionTask(true);
        } else {
            connectionTask.ifPresent(task->builder.connectionTask(task));
        }

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
        Optional<ConnectionTask<?, ?>> connectionTask = getConnectionTaskOptionallyOrThrowException(comTaskExecutionInfo);
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

    protected Optional<ConnectionTask<?,?>> getConnectionTaskOptionallyOrThrowException(ComTaskExecutionInfo comTaskExecutionInfo) {
        if (comTaskExecutionInfo.connectionTask!=null && comTaskExecutionInfo.connectionTask.id!=null) {
            return Optional.of(connectionTaskService.findConnectionTask(comTaskExecutionInfo.connectionTask.id)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_CONNECTION_TASK)));
        }
        return Optional.empty();
    }

    public ScheduledComTaskExecution updateSharedScheduledComtaskExecution(ComTaskExecutionInfo comTaskExecutionInfo, ScheduledComTaskExecution comTaskExecution) {
        ScheduledComTaskExecutionUpdater updater = comTaskExecution.getUpdater();
        updateCommonFields(comTaskExecutionInfo, updater);
        return updater.update();
    }

    public ManuallyScheduledComTaskExecution updateManuallyScheduledComTaskExecution(ComTaskExecutionInfo comTaskExecutionInfo, ManuallyScheduledComTaskExecution comTaskExecution) {
        ManuallyScheduledComTaskExecutionUpdater updater = comTaskExecution.getUpdater();
        updateCommonFields(comTaskExecutionInfo, updater);
        if (comTaskExecutionInfo.schedulingSpec!=null) {
            updater.scheduleAccordingTo(comTaskExecutionInfo.schedulingSpec.asTemporalExpression());
        } else {
            updater.removeSchedule();
        }
        return updater.update();
    }

    public ManuallyScheduledComTaskExecution updateAdHocComTaskExecution(ComTaskExecutionInfo comTaskExecutionInfo, ManuallyScheduledComTaskExecution comTaskExecution) {
        ManuallyScheduledComTaskExecutionUpdater updater = comTaskExecution.getUpdater();
        updateCommonFields(comTaskExecutionInfo, updater);
        if (comTaskExecutionInfo.schedulingSpec!=null) {
            updater.scheduleAccordingTo(comTaskExecutionInfo.schedulingSpec.asTemporalExpression());
        } else {
            updater.removeSchedule();
        }
        return updater.update();
    }

    protected void updateCommonFields(ComTaskExecutionInfo comTaskExecutionInfo, ComTaskExecutionUpdater<? extends ComTaskExecutionUpdater<?, ?>, ? extends ComTaskExecution> updater) {
        Optional<ConnectionTask<?,?>> connectionTask = getConnectionTaskOptionallyOrThrowException(comTaskExecutionInfo);
        if (!connectionTask.isPresent() && (comTaskExecutionInfo.useDefaultConnectionTask==null || !comTaskExecutionInfo.useDefaultConnectionTask)) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.NOT_POSSIBLE_TO_SUPPLY_BOTH_OR_NONE);
        }
        if (comTaskExecutionInfo.useDefaultConnectionTask!=null && comTaskExecutionInfo.useDefaultConnectionTask) {
            updater.useDefaultConnectionTask(true);
        } else {
            connectionTask.ifPresent(task->updater.connectionTask(task));
        }
        if (comTaskExecutionInfo.priority!=null) {
            updater.priority(comTaskExecutionInfo.priority);
        }
        if (comTaskExecutionInfo.ignoreNextExecutionSpecForInbound!=null) {
            updater.ignoreNextExecutionSpecForInbound(comTaskExecutionInfo.ignoreNextExecutionSpecForInbound);
        }
        if (comTaskExecutionInfo.ignoreNextExecutionSpecForInbound!=null) {
            updater.ignoreNextExecutionSpecForInbound(comTaskExecutionInfo.ignoreNextExecutionSpecForInbound);
        }
    }
}
