/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.scheduling.ComSchedule;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.common.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.scheduling.SchedulingService;
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
import java.util.logging.Logger;

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
    private final Provider<ConnectionFunctionInfoFactory> connectionFunctionInfoFactoryProvider;

    private static final Logger LOGGER = Logger.getLogger(ComTaskExecutionInfoFactory.class.getName());

    @Inject
    public ComTaskExecutionInfoFactory(ExceptionFactory exceptionFactory, ConnectionTaskService connectionTaskService,
                                       SchedulingService schedulingService, TaskService taskService,
                                       Provider<DeviceInfoFactory> deviceInfoFactoryProvider,
                                       Provider<ConnectionTaskInfoFactory> connectionTaskInfoFactoryProvider,
                                       Provider<ComTaskInfoFactory> comTaskInfoFactoryProvider,
                                       Provider<ComScheduleInfoFactory> comScheduleInfoFactoryProvider,
                                       Provider<ConnectionFunctionInfoFactory> connectionFunctionInfoFactoryProvider) {
        this.exceptionFactory = exceptionFactory;
        this.connectionTaskService = connectionTaskService;
        this.schedulingService = schedulingService;
        this.taskService = taskService;
        this.deviceInfoFactoryProvider = deviceInfoFactoryProvider;
        this.connectionTaskInfoFactoryProvider = connectionTaskInfoFactoryProvider;
        this.comTaskInfoFactoryProvider = comTaskInfoFactoryProvider;
        this.comScheduleInfoFactoryProvider = comScheduleInfoFactoryProvider;
        this.connectionFunctionInfoFactoryProvider = connectionFunctionInfoFactoryProvider;
    }

    public LinkInfo asLink(ComTaskExecution comTaskExecution, Relation relation, UriInfo uriInfo) {
        ComTaskExecutionInfo info = new ComTaskExecutionInfo();
        copySelectedFields(info, comTaskExecution, uriInfo, Arrays.asList("id", "version"));
        info.link = link(comTaskExecution, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<ComTaskExecution> comTaskExecutions, Relation relation, UriInfo uriInfo) {
        return comTaskExecutions.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
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
        map.put("id", (comTaskExecutionInfo, comTaskExecution, uriInfo) -> {
            comTaskExecutionInfo.id = comTaskExecution.getId();
            if (comTaskExecutionInfo.device == null) {
                comTaskExecutionInfo.device = new LinkInfo();
            }
            comTaskExecutionInfo.device.id = comTaskExecution.getDevice().getId();
        });
        map.put("version", (comTaskExecutionInfo, comTaskExecution, uriInfo) -> {
            comTaskExecutionInfo.version = comTaskExecution.getVersion();
            if (comTaskExecutionInfo.device == null) {
                comTaskExecutionInfo.device = new LinkInfo();
            }
            comTaskExecutionInfo.device.version = comTaskExecution.getDevice().getVersion();
        });
        map.put("link", ((comTaskExecutionInfo, comTaskExecution, uriInfo) ->
                comTaskExecutionInfo.link = link(comTaskExecution, Relation.REF_SELF, uriInfo)));
        map.put("device", ((comTaskExecutionInfo, comTaskExecution, uriInfo) ->
                comTaskExecutionInfo.device = deviceInfoFactoryProvider.get().asLink(comTaskExecution.getDevice(), Relation.REF_PARENT, uriInfo)));
        map.put("connectionTask", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> {
            if (comTaskExecution.getConnectionTask().isPresent()) {
                comTaskExecutionInfo.connectionTask = connectionTaskInfoFactoryProvider.get().asLink(comTaskExecution.getConnectionTask().get(), Relation.REF_RELATION, uriInfo);
            }
        }));
        map.put("useDefaultConnectionTask", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> {
            if (comTaskExecution.usesDefaultConnectionTask()) {
                comTaskExecutionInfo.useDefaultConnectionTask = comTaskExecution.usesDefaultConnectionTask();
            }
        }));
        map.put("useConnectionTaskWithConnectionFunction", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> {
            Optional<DeviceProtocolPluggableClass> protocolPluggableClassOptional = comTaskExecution.getDevice().getDeviceType().getDeviceProtocolPluggableClass();
            if (comTaskExecution.getConnectionFunction().isPresent() && protocolPluggableClassOptional.isPresent()) {
                comTaskExecutionInfo.useConnectionTaskWithConnectionFunction = connectionFunctionInfoFactoryProvider.get().asLink(
                        protocolPluggableClassOptional.get(),
                        comTaskExecution.getConnectionFunction().get(),
                        Relation.REF_RELATION,
                        uriInfo);
            }
        }));
        map.put("comTask", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> {
            if (ComTaskExecution.class.isAssignableFrom(comTaskExecution.getClass())) {
                comTaskExecutionInfo.comTask = comTaskInfoFactoryProvider.get().asLink(comTaskExecution.getComTask(), Relation.REF_RELATION, uriInfo);
            }
        }));
        map.put("nextExecution", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.nextExecution = comTaskExecution.getNextExecutionTimestamp()));
        map.put("plannedNextExecution", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.plannedNextExecution = comTaskExecution.getPlannedNextExecutionTimestamp()));
        map.put("priority", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.priority = comTaskExecution.getPlannedPriority()));
        map.put("ignoreNextExecutionSpecForInbound", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.ignoreNextExecutionSpecForInbound = comTaskExecution.isIgnoreNextExecutionSpecsForInbound()));
        map.put("schedule", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> {
            if (comTaskExecution.usesSharedSchedule()) {
                comTaskExecutionInfo.schedule = comScheduleInfoFactoryProvider.get().asLink(comTaskExecution.getComSchedule().get(), Relation.REF_RELATION, uriInfo);
            }
        }));
        map.put("type", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> {
            comTaskExecutionInfo.type = comTaskExecution.isScheduledManually() ?
                    (comTaskExecution.isAdHoc() ?
                            ComTaskExecutionType.AdHoc :
                            ComTaskExecutionType.ManualSchedule) :
                    ComTaskExecutionType.SharedSchedule;
        }));
        map.put("lastCommunicationStart", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.lastCommunicationStart = comTaskExecution.getLastExecutionStartTimestamp()));
        map.put("lastSuccessfulCompletion", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.lastSuccessfulCompletion = comTaskExecution.getLastSuccessfulCompletionTimestamp()));
        map.put("status", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.status = comTaskExecution.getStatus()));

        return map;
    }

    public ComTaskExecution createManuallyScheduledComTaskExecution(ComTaskExecutionInfo comTaskExecutionInfo, Device device) {
        if (comTaskExecutionInfo.comTask == null || comTaskExecutionInfo.comTask.id == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.COM_TASK_EXPECTED);
        }
        if (comTaskExecutionInfo.schedulingSpec == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.SCHEDULE_SPEC_EXPECTED);
        }
        ComTask comTask = taskService.findComTask(comTaskExecutionInfo.comTask.id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_COM_TASK));

        ComTaskEnablement comTaskEnablement = device.getDeviceConfiguration().getComTaskEnablementFor(comTask)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.COM_TASK_NOT_ENABLED));

        Optional<ConnectionTask<?, ?>> connectionTask = getConnectionTaskOptionallyOrThrowException(comTaskExecutionInfo);
        Optional<ConnectionFunction> connectionFunction = getConnectionFunctionOptionallyOrThrowException(comTaskExecutionInfo, device.getDeviceType());
        boolean useDefaultConnectionTask = comTaskExecutionInfo.useDefaultConnectionTask != null && comTaskExecutionInfo.useDefaultConnectionTask;
        if (atLeastTwo(connectionTask.isPresent(), connectionFunction.isPresent(), useDefaultConnectionTask)) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.NOT_POSSIBLE_TO_SUPPLY_MULTIPLE_OR_NONE);
        }

        ComTaskExecutionBuilder builder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, comTaskExecutionInfo.schedulingSpec.asTemporalExpression());
        if (useDefaultConnectionTask) {
            builder.useDefaultConnectionTask(true);
        } else if (connectionFunction.isPresent()) {
            builder.setConnectionFunction(connectionFunction.get());
        } else {
            connectionTask.ifPresent(builder::connectionTask);
        }
        if (comTaskExecutionInfo.priority != null) {
            builder.priority(comTaskExecutionInfo.priority);
        }
        if (comTaskExecutionInfo.ignoreNextExecutionSpecForInbound != null) {
            builder.ignoreNextExecutionSpecForInbound(comTaskExecutionInfo.ignoreNextExecutionSpecForInbound);
        }
        ComTaskExecution manuallyScheduledComTaskExecution = builder.add();
        device.save();
        return manuallyScheduledComTaskExecution;
    }

    public ComTaskExecution createSharedScheduledComtaskExecution(ComTaskExecutionInfo comTaskExecutionInfo, Device device) {
        if (comTaskExecutionInfo.comTask != null && comTaskExecutionInfo.comTask.id != null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.TYPE_DOES_NOT_SUPPORT_COM_TASK);
        }
        if (comTaskExecutionInfo.schedulingSpec != null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.TYPE_DOES_NOT_SUPPORT_SCHEDULE_SPEC);
        }
        if (comTaskExecutionInfo.schedule == null || comTaskExecutionInfo.schedule.id == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.SCHEDULE_EXPECTED);
        }
        ComSchedule comSchedule = schedulingService.findSchedule(comTaskExecutionInfo.schedule.id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_COM_SCHEDULE));

        Optional<ConnectionTask<?, ?>> connectionTask = getConnectionTaskOptionallyOrThrowException(comTaskExecutionInfo);
        Optional<ConnectionFunction> connectionFunction = getConnectionFunctionOptionallyOrThrowException(comTaskExecutionInfo, device.getDeviceType());
        boolean useDefaultConnectionTask = comTaskExecutionInfo.useDefaultConnectionTask != null && comTaskExecutionInfo.useDefaultConnectionTask;
        if (atLeastTwo(connectionTask.isPresent(), connectionFunction.isPresent(), useDefaultConnectionTask)) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.NOT_POSSIBLE_TO_SUPPLY_MULTIPLE_OR_NONE);
        }

        ComTaskExecutionBuilder builder = device.newScheduledComTaskExecution(comSchedule);
        if (useDefaultConnectionTask) {
            builder.useDefaultConnectionTask(true);
        } else if (connectionFunction.isPresent()) {
            builder.setConnectionFunction(connectionFunction.get());
        } else {
            connectionTask.ifPresent(builder::connectionTask);
        }

        if (connectionTask.isPresent()) {
            builder.connectionTask(connectionTask.get());
        }
        if (comTaskExecutionInfo.priority != null) {
            builder.priority(comTaskExecutionInfo.priority);
        }
        if (comTaskExecutionInfo.ignoreNextExecutionSpecForInbound != null) {
            builder.ignoreNextExecutionSpecForInbound(comTaskExecutionInfo.ignoreNextExecutionSpecForInbound);
        }
        ComTaskExecution scheduledComTaskExecution = builder.add();
        device.save();
        return scheduledComTaskExecution;
    }

    private boolean atLeastTwo(boolean a, boolean b, boolean c) {
        return a && (b || c) || (b && c);
    }

    public ComTaskExecution createAdHocComtaskExecution(ComTaskExecutionInfo comTaskExecutionInfo, Device device) {
        if (comTaskExecutionInfo.comTask == null || comTaskExecutionInfo.comTask.id == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.COM_TASK_EXPECTED);
        }
        if (comTaskExecutionInfo.schedulingSpec != null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.TYPE_DOES_NOT_SUPPORT_SCHEDULE_SPEC);
        }
        Optional<ConnectionTask<?, ?>> connectionTask = getConnectionTaskOptionallyOrThrowException(comTaskExecutionInfo);
        ComTask comTask = taskService.findComTask(comTaskExecutionInfo.comTask.id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_COM_TASK));

        ComTaskEnablement comTaskEnablement = device.getDeviceConfiguration().getComTaskEnablementFor(comTask)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.COM_TASK_NOT_ENABLED));

        ComTaskExecutionBuilder builder = device.newAdHocComTaskExecution(comTaskEnablement);
        if (connectionTask.isPresent()) {
            builder.connectionTask(connectionTask.get());
        }
        if (comTaskExecutionInfo.priority != null) {
            builder.priority(comTaskExecutionInfo.priority);
        }
        if (comTaskExecutionInfo.ignoreNextExecutionSpecForInbound != null) {
            builder.ignoreNextExecutionSpecForInbound(comTaskExecutionInfo.ignoreNextExecutionSpecForInbound);
        }
        ComTaskExecution manuallyScheduledComTaskExecution = builder.add();
        device.save();
        return manuallyScheduledComTaskExecution;
    }

    private Optional<ConnectionTask<?, ?>> getConnectionTaskOptionallyOrThrowException(ComTaskExecutionInfo comTaskExecutionInfo) {
        if (comTaskExecutionInfo.connectionTask != null && comTaskExecutionInfo.connectionTask.id != null) {
            return Optional.of(connectionTaskService.findConnectionTask(comTaskExecutionInfo.connectionTask.id)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_CONNECTION_TASK)));
        }
        return Optional.empty();
    }

    private Optional<ConnectionFunction> getConnectionFunctionOptionallyOrThrowException(ComTaskExecutionInfo comTaskExecutionInfo, DeviceType deviceType) {
        Optional<DeviceProtocolPluggableClass> protocolPluggableClass = deviceType.getDeviceProtocolPluggableClass();
        if (comTaskExecutionInfo.useConnectionTaskWithConnectionFunction != null && comTaskExecutionInfo.useConnectionTaskWithConnectionFunction.id != null && protocolPluggableClass.isPresent()) {
           return Optional.of( getAllProvidedOrConsumableConnectionFunctions(protocolPluggableClass.get())
                    .stream()
                    .filter(cf -> cf.getId() == comTaskExecutionInfo.useConnectionTaskWithConnectionFunction.id)
                    .findFirst()
                    .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_CONNECTION_FUNCTION)));
        }
        return Optional.empty();
    }

    private List<ConnectionFunction> getAllProvidedOrConsumableConnectionFunctions(DeviceProtocolPluggableClass pluggableClass) {
        List<ConnectionFunction> connectionFunctions = pluggableClass.getProvidedConnectionFunctions();
        pluggableClass.getConsumableConnectionFunctions()
                .stream()
                .filter(cf -> notAlreadyPresent(connectionFunctions, cf))
                .forEach(connectionFunctions::add);
        return connectionFunctions;
    }

    private boolean notAlreadyPresent(List<ConnectionFunction> connectionFunctions, ConnectionFunction connectionFunctionToTest) {
         return connectionFunctions.stream()
                 .noneMatch(cf -> cf.getId() == connectionFunctionToTest.getId());
     }

    public ComTaskExecution updateSharedScheduledComtaskExecution(ComTaskExecutionInfo comTaskExecutionInfo, ComTaskExecution comTaskExecution) {
        ComTaskExecutionUpdater updater = comTaskExecution.getUpdater();
        updateCommonFields(comTaskExecutionInfo, updater);
        LOGGER.info("CXO-11731: Update comtask execution from updateSharedScheduledComtaskExecution.");
        return updater.update();
    }

    public ComTaskExecution updateManuallyScheduledComTaskExecution(ComTaskExecutionInfo comTaskExecutionInfo, ComTaskExecution comTaskExecution) {
        ComTaskExecutionUpdater updater = comTaskExecution.getUpdater();
        updateCommonFields(comTaskExecutionInfo, updater);
        if (comTaskExecutionInfo.schedulingSpec != null) {
            updater.createNextExecutionSpecs(comTaskExecutionInfo.schedulingSpec.asTemporalExpression());
        } else {
            updater.removeSchedule();
        }
        LOGGER.info("CXO-11731: Update comtask execution from updateManuallyScheduledComTaskExecution.");
        return updater.update();
    }

    public ComTaskExecution updateAdHocComTaskExecution(ComTaskExecutionInfo comTaskExecutionInfo, ComTaskExecution comTaskExecution) {
        ComTaskExecutionUpdater updater = comTaskExecution.getUpdater();
        updateCommonFields(comTaskExecutionInfo, updater);
        if (comTaskExecutionInfo.schedulingSpec != null) {
            updater.createNextExecutionSpecs(comTaskExecutionInfo.schedulingSpec.asTemporalExpression());
        } else {
            updater.removeSchedule();
        }
        LOGGER.info("CXO-11731: Update comtask execution from updateAdHocComTaskExecution.");
        return updater.update();
    }

    protected void updateCommonFields(ComTaskExecutionInfo comTaskExecutionInfo, ComTaskExecutionUpdater updater) {
        Optional<ConnectionTask<?, ?>> connectionTask = getConnectionTaskOptionallyOrThrowException(comTaskExecutionInfo);
        Optional<ConnectionFunction> connectionFunction = getConnectionFunctionOptionallyOrThrowException(comTaskExecutionInfo, updater.getComTaskExecution().getDevice().getDeviceType());
        boolean useDefaultConnectionTask = comTaskExecutionInfo.useDefaultConnectionTask != null && comTaskExecutionInfo.useDefaultConnectionTask;
        if (atLeastTwo(connectionTask.isPresent(), connectionFunction.isPresent(), useDefaultConnectionTask)) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.NOT_POSSIBLE_TO_SUPPLY_MULTIPLE_OR_NONE);
        }

        if (useDefaultConnectionTask) {
            updater.useDefaultConnectionTask(true);
        } else if (connectionFunction.isPresent()) {
            updater.setConnectionFunction(connectionFunction.get());
        } else {
            connectionTask.ifPresent(updater::connectionTask);
        }
        if (comTaskExecutionInfo.priority != null) {
            updater.priority(comTaskExecutionInfo.priority);
        }
        if (comTaskExecutionInfo.ignoreNextExecutionSpecForInbound != null) {
            updater.ignoreNextExecutionSpecForInbound(comTaskExecutionInfo.ignoreNextExecutionSpecForInbound);
        }
        if (comTaskExecutionInfo.ignoreNextExecutionSpecForInbound != null) {
            updater.ignoreNextExecutionSpecForInbound(comTaskExecutionInfo.ignoreNextExecutionSpecForInbound);
        }
    }
}
