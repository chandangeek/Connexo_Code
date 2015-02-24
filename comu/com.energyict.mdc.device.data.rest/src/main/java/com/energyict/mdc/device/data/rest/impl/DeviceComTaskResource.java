package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.LogLevelAdapter;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

public class DeviceComTaskResource {

    private static final String LOG_LEVELS_FILTER_PROPERTY = "logLevels";

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final DeviceComTaskInfoFactory deviceComTaskInfoFactory;
    private final TaskService taskService;
    private final CommunicationTaskService communicationTaskService;
    private final ComTaskExecutionSessionInfoFactory comTaskExecutionSessionInfoFactory;
    private final ComSessionInfoFactory comSessionInfoFactory;
    private final JournalEntryInfoFactory journalEntryInfoFactory;


    @Inject
    public DeviceComTaskResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, DeviceComTaskInfoFactory deviceComTaskInfoFactory, TaskService taskService, CommunicationTaskService communicationTaskService, ComTaskExecutionSessionInfoFactory comTaskExecutionSessionInfoFactory, ComSessionInfoFactory comSessionInfoFactory, JournalEntryInfoFactory journalEntryInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.deviceComTaskInfoFactory = deviceComTaskInfoFactory;
        this.taskService = taskService;
        this.communicationTaskService = communicationTaskService;
        this.comTaskExecutionSessionInfoFactory = comTaskExecutionSessionInfoFactory;
        this.comSessionInfoFactory = comSessionInfoFactory;
        this.journalEntryInfoFactory = journalEntryInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response getAllComTaskExecutions(@PathParam("mRID") String mrid, @BeanParam QueryParameters queryParameters, @BeanParam JsonQueryFilter queryFilter) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        List<ComTaskExecution> comTaskExecutions = device.getComTaskExecutions();
        List<ComTaskEnablement> comTaskEnablements = deviceConfiguration.getComTaskEnablements();
        List<DeviceComTaskInfo> deviceSchedulesInfos = deviceComTaskInfoFactory.from(comTaskExecutions, comTaskEnablements, device);
        return Response.ok(PagedInfoList.fromPagedList("comTasks", deviceSchedulesInfos, queryParameters)).build();
    }

    @PUT
    @Path("/{comTaskId}/urgency")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response updateComTaskExecution(@PathParam("mRID") String mrid, @PathParam("comTaskId") Long comTaskId, ComTaskUrgencyInfo comTaskUrgencyInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutionsForDeviceAndComTask(comTaskId, device);
        if (comTaskExecutions.size() > 0) {
            comTaskExecutions.forEach(updateUrgency(comTaskUrgencyInfo, device));
        } else {
            throw exceptionFactory.newException(MessageSeeds.UPDATE_URGENCY_NOT_ALLOWED);
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/{comTaskId}/protocoldialect")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response updateProtocolDialect(@PathParam("mRID") String mrid, @PathParam("comTaskId") Long comTaskId, ComTaskProtocolDialectInfo comTaskProtocolDialectInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutionsForDeviceAndComTask(comTaskId, device);
        if (comTaskExecutions.size() > 0) {
            comTaskExecutions.forEach(updateProtocolDialect(comTaskProtocolDialectInfo, device));
        } else {
            throw exceptionFactory.newException(MessageSeeds.UPDATE_DIALECT_PROPERTIES_NOT_ALLOWED);
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/{comTaskId}/frequency")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response updateConnectionMethod(@PathParam("mRID") String mrid, @PathParam("comTaskId") Long comTaskId, ComTaskFrequencyInfo comTaskFrequencyInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutionsForDeviceAndComTask(comTaskId, device);
        if (comTaskExecutions.size() > 0) {
            comTaskExecutions.forEach(updateComTaskExecutionFrequency(comTaskFrequencyInfo, device));
        } else {
            List<ComTaskEnablement> comTaskEnablements = getComTaskEnablementsForDeviceAndComtask(comTaskId, device);
            comTaskEnablements.forEach(createManuallyScheduledComTaskExecutionForEnablement(comTaskFrequencyInfo, device));
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/{comTaskId}/connectionmethod")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response updateFrequency(@PathParam("mRID") String mrid, @PathParam("comTaskId") Long comTaskId, ComTaskConnectionMethodInfo comTaskConnectionMethodInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutionsForDeviceAndComTask(comTaskId, device);
        if (comTaskExecutions.size() > 0) {
            comTaskExecutions.forEach(updateComTaskConnectionMethod(comTaskConnectionMethodInfo, device));
        } else {
            throw exceptionFactory.newException(MessageSeeds.UPDATE_CONNECTION_METHOD_NOT_ALLOWED);
        }
        return Response.ok().build();
    }


    @PUT
    @Path("/{comTaskId}/run")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION})
    public Response run(@PathParam("mRID") String mrid, @PathParam("comTaskId") Long comTaskId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutionsForDeviceAndComTask(comTaskId, device);
        if (comTaskExecutions.size() > 0) {
            comTaskExecutions.forEach(runComTaskFromExecution(device));
        } else if (comTaskExecutions.size() == 0) {
            List<ComTaskEnablement> comTaskEnablements = getComTaskEnablementsForDeviceAndComtask(comTaskId, device);
            comTaskEnablements.forEach(runComTaskFromEnablement(device));
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/{comTaskId}/runnow")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION})
    public Response runnow(@PathParam("mRID") String mrid, @PathParam("comTaskId") Long comTaskId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutionsForDeviceAndComTask(comTaskId, device);
        if (comTaskExecutions.size() > 0) {
            comTaskExecutions.forEach(runComTaskFromExecutionNow(device));
        } else if (comTaskExecutions.size() == 0) {
            List<ComTaskEnablement> comTaskEnablements = getComTaskEnablementsForDeviceAndComtask(comTaskId, device);
            comTaskEnablements.forEach(runComTaskFromEnablementNow(device));
        }
        return Response.ok().build();
    }

    @GET
    @Path("{comTaskId}/comtaskexecutionsessions")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public PagedInfoList getComTaskExecutionSessions(@PathParam("mRID") String mrid,
                                                     @PathParam("comTaskId") long comTaskId, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ComTask comTask = this.taskService.findComTask(comTaskId).orElse(null);
        if (comTask == null || !device.getDeviceConfiguration().getComTaskEnablementFor(comTask).isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_COM_TASK);
        }
        ComTaskExecution comTaskExecution = getComTaskExecutionForDeviceAndComTaskOrThrowException(comTaskId, device);
        List<ComTaskExecutionSessionInfo> infos = new ArrayList<>();
        List<ComTaskExecutionSession> comTaskExecutionSessions = communicationTaskService.findByComTaskExecutionAndComTask(comTaskExecution, comTask).from(queryParameters).find();
        for (ComTaskExecutionSession comTaskExecutionSession : comTaskExecutionSessions) {
            ComTaskExecutionSessionInfo comTaskExecutionSessionInfo = comTaskExecutionSessionInfoFactory.from(comTaskExecutionSession);
            comTaskExecutionSessionInfo.comSession = comSessionInfoFactory.from(comTaskExecutionSession.getComSession());
            infos.add(comTaskExecutionSessionInfo);
        }
        return PagedInfoList.fromPagedList("comTaskExecutionSessions", infos, queryParameters);
    }

    @GET
    @Path("{comTaskId}/comtaskexecutionsessions/{comTaskExecutionSessionId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public ComTaskExecutionSessionInfo getComTaskExecutionSession(@PathParam("mRID") String mrid,
                                                                  @PathParam("comTaskId") long comTaskId, @PathParam("comTaskExecutionSessionId") Long comTaskExecutionSessionId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ComTask comTask = taskService.findComTask(comTaskId).orElse(null);
        if (comTask == null || !device.getDeviceConfiguration().getComTaskEnablementFor(comTask).isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_COM_TASK);
        }
        ComTaskExecution comTaskExecution = getComTaskExecutionForDeviceAndComTaskOrThrowException(comTaskId, device);
        List<ComTaskExecutionSession> comTaskExecutionSessions = communicationTaskService.findByComTaskExecution(comTaskExecution).find();
        for (ComTaskExecutionSession comTaskExecutionSession : comTaskExecutionSessions) {
            if (comTaskExecutionSession.getId() == comTaskExecutionSessionId) {
                ComTaskExecutionSessionInfo comTaskExecutionSessionInfo = comTaskExecutionSessionInfoFactory.from(comTaskExecutionSession);
                comTaskExecutionSessionInfo.comSession = comSessionInfoFactory.from(comTaskExecutionSession.getComSession());
                return comTaskExecutionSessionInfo;
            }
        }
        throw exceptionFactory.newException(MessageSeeds.NO_SUCH_COM_TASK_EXEC_SESSION);
    }


    @GET
    @Path("{comTaskId}/comtaskexecutionsessions/{sessionId}/journals")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public PagedInfoList getComTaskExecutionSessionJournals(@PathParam("mRID") String mrid,
                                                            @PathParam("comTaskId") long comTaskId,
                                                            @PathParam("sessionId") long sessionId,
                                                            @BeanParam JsonQueryFilter jsonQueryFilter,
                                                            @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ComTask comTask = taskService.findComTask(comTaskId).orElse(null);
        if (comTask == null || !device.getDeviceConfiguration().getComTaskEnablementFor(comTask).isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_COM_TASK);
        }
        ComTaskExecution comTaskExecution = getComTaskExecutionForDeviceAndComTaskOrThrowException(comTaskId, device);
        ComTaskExecutionSession comTaskExecutionSession = findComTaskExecutionSessionOrThrowException(sessionId, comTaskExecution);
        EnumSet<ComServer.LogLevel> logLevels = EnumSet.noneOf(ComServer.LogLevel.class);
        if (jsonQueryFilter.hasProperty(LOG_LEVELS_FILTER_PROPERTY)) {
            jsonQueryFilter.getPropertyList(LOG_LEVELS_FILTER_PROPERTY, new LogLevelAdapter()).stream().forEach(logLevels::add);
        } else {
            logLevels = EnumSet.allOf(ComServer.LogLevel.class);
        }
        List<JournalEntryInfo> infos = comTaskExecutionSession.findComTaskExecutionJournalEntries(logLevels).from(queryParameters).stream().map(journalEntryInfoFactory::asInfo).collect(toList());

        return PagedInfoList.fromPagedList("comTaskExecutionSessions", infos, queryParameters);

    }

    private ComTaskExecutionSession findComTaskExecutionSessionOrThrowException(long sessionId, ComTaskExecution comTaskExecution) {
        return communicationTaskService.findByComTaskExecution(comTaskExecution).stream().filter(c -> c.getId() == sessionId).findAny().orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_COM_TASK_EXEC_SESSION));
    }


    private Consumer<? super ComTaskEnablement> runComTaskFromEnablement(Device device) {
        return comTaskEnablement -> {
            ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> manuallyScheduledComTaskExecutionComTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, comTaskEnablement.getProtocolDialectConfigurationProperties().orElse(null));
            if (comTaskEnablement.hasPartialConnectionTask()) {
                device.getConnectionTasks().stream()
                        .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == comTaskEnablement.getPartialConnectionTask().get().getId())
                        .forEach(manuallyScheduledComTaskExecutionComTaskExecutionBuilder::connectionTask);
            }
            ManuallyScheduledComTaskExecution manuallyScheduledComTaskExecution = manuallyScheduledComTaskExecutionComTaskExecutionBuilder.add();
            device.save();
            manuallyScheduledComTaskExecution.scheduleNow();
        };
    }

    private Consumer<? super ComTaskEnablement> runComTaskFromEnablementNow(Device device) {
        return comTaskEnablement -> {
            ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> manuallyScheduledComTaskExecutionComTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement, comTaskEnablement.getProtocolDialectConfigurationProperties().orElse(null));
            if (comTaskEnablement.hasPartialConnectionTask()) {
                device.getConnectionTasks().stream()
                        .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == comTaskEnablement.getPartialConnectionTask().get().getId())
                        .forEach(manuallyScheduledComTaskExecutionComTaskExecutionBuilder::connectionTask);
            }
            ManuallyScheduledComTaskExecution manuallyScheduledComTaskExecution = manuallyScheduledComTaskExecutionComTaskExecutionBuilder.add();
            device.save();
            manuallyScheduledComTaskExecution.runNow();
        };
    }

    private Consumer<? super ComTaskExecution> runComTaskFromExecution(Device device) {
        return ComTaskExecution::scheduleNow;
    }

    private Consumer<? super ComTaskExecution> runComTaskFromExecutionNow(Device device) {
        return ComTaskExecution::runNow;
    }


    private List<ComTaskExecution> getComTaskExecutionsForDeviceAndComTask(Long comTaskId, Device device) {
        return device.getComTaskExecutions().stream()
                .filter(comTaskExecution -> comTaskExecution.getComTasks().stream()
                        .mapToLong(ComTask::getId)
                        .anyMatch(comTaskId::equals))
                .collect(toList());
    }

    private ComTaskExecution getComTaskExecutionForDeviceAndComTaskOrThrowException(Long comTaskId, Device device) {
        return device.getComTaskExecutions().stream()
                .filter(comTaskExecution -> comTaskExecution.getComTasks().stream()
                        .mapToLong(ComTask::getId)
                        .anyMatch(comTaskId::equals))
                .findFirst().orElseThrow(() -> exceptionFactory.newException(MessageSeeds.COM_TASK_IS_NOT_ENABLED_FOR_THIS_DEVICE, comTaskId, device.getmRID()));
    }

    private List<ComTaskEnablement> getComTaskEnablementsForDeviceAndComtask(Long comTaskId, Device device) {
        return device.getDeviceConfiguration().getComTaskEnablements().stream()
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().getId() == comTaskId)
                .collect(toList());
    }

    private Consumer<ComTaskExecution> updateUrgency(ComTaskUrgencyInfo comTaskUrgencyInfo, Device device) {
        return comTaskExecution -> {
            if (comTaskExecution.isScheduledManually()) {
                device.getComTaskExecutionUpdater((ManuallyScheduledComTaskExecution) comTaskExecution).priority(comTaskUrgencyInfo.urgency).update();
            } else if (comTaskExecution.usesSharedSchedule()) {
                device.getComTaskExecutionUpdater((ScheduledComTaskExecution) comTaskExecution).priority(comTaskUrgencyInfo.urgency).update();
            } else {
                throw exceptionFactory.newException(MessageSeeds.UPDATE_URGENCY_NOT_ALLOWED);
            }
        };
    }

    private Consumer<ComTaskExecution> updateProtocolDialect(ComTaskProtocolDialectInfo comTaskProtocolDialectInfo, Device device) {
        return comTaskExecution -> {
            List<ProtocolDialectConfigurationProperties> protocolDialectConfigurationPropertiesList = device.getDeviceConfiguration().getProtocolDialectConfigurationPropertiesList();
            Optional<ProtocolDialectConfigurationProperties> dialectConfigurationPropertiesOptional = protocolDialectConfigurationPropertiesList.stream()
                    .filter(protocolDialectConfigurationProperties -> protocolDialectConfigurationProperties.getDeviceProtocolDialect().getDisplayName().equals(comTaskProtocolDialectInfo.protocolDialect))
                    .findFirst();
            if (comTaskExecution.isScheduledManually() && dialectConfigurationPropertiesOptional.isPresent()) {
                device.getComTaskExecutionUpdater((ManuallyScheduledComTaskExecution) comTaskExecution).protocolDialectConfigurationProperties(dialectConfigurationPropertiesOptional.get()).update();
                device.save();
            } else {
                throw exceptionFactory.newException(MessageSeeds.UPDATE_DIALECT_PROPERTIES_NOT_ALLOWED);
            }
        };
    }

    private Consumer<ComTaskExecution> updateComTaskExecutionFrequency(ComTaskFrequencyInfo comTaskFrequencyInfo, Device device) {
        return comTaskExecution -> {
            if (comTaskExecution.isScheduledManually() || comTaskExecution.isAdHoc())
                device.getComTaskExecutionUpdater((ManuallyScheduledComTaskExecution) comTaskExecution).scheduleAccordingTo(comTaskFrequencyInfo.temporalExpression.asTemporalExpression()).update();
        };
    }

    private Consumer<ComTaskEnablement> createManuallyScheduledComTaskExecutionForEnablement(ComTaskFrequencyInfo comTaskFrequencyInfo, Device device) {
        return comTaskEnablement -> {
            ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> manuallyScheduledComTaskExecutionComTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, comTaskEnablement.getProtocolDialectConfigurationProperties().orElse(null), comTaskFrequencyInfo.temporalExpression.asTemporalExpression());
            if (comTaskEnablement.hasPartialConnectionTask()) {
                device.getConnectionTasks().stream()
                        .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == comTaskEnablement.getPartialConnectionTask().get().getId())
                        .forEach(manuallyScheduledComTaskExecutionComTaskExecutionBuilder::connectionTask);
            }
            manuallyScheduledComTaskExecutionComTaskExecutionBuilder.add();
            device.save();
        };
    }

    private Consumer<ComTaskExecution> updateComTaskConnectionMethod(ComTaskConnectionMethodInfo comTaskConnectionMethodInfo, Device device) {
        return comTaskExecution -> {
            Optional<ConnectionTask<?, ?>> connectionTaskOptional = device.getConnectionTasks().stream().filter(ct -> ct.getName().equals(comTaskConnectionMethodInfo.connectionMethod)).findFirst();
            if (connectionTaskOptional.isPresent()) {
                if (comTaskExecution.isScheduledManually()) {
                    device.getComTaskExecutionUpdater((ManuallyScheduledComTaskExecution) comTaskExecution).connectionTask(connectionTaskOptional.get()).update();
                } else {
                    device.getComTaskExecutionUpdater((ScheduledComTaskExecution) comTaskExecution).connectionTask(connectionTaskOptional.get()).update();
                }
            } else {
                if (comTaskExecution.isScheduledManually()) {
                    device.getComTaskExecutionUpdater((ManuallyScheduledComTaskExecution) comTaskExecution).useDefaultConnectionTask(true).update();
                } else {
                    device.getComTaskExecutionUpdater((ScheduledComTaskExecution) comTaskExecution).useDefaultConnectionTask(true).update();
                }
            }
        };
    }

}