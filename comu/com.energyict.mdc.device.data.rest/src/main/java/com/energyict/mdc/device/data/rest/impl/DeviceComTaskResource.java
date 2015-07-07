package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
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
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
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

@DeviceStatesRestricted(value = {DefaultState.DECOMMISSIONED}, methods = {HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE})
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
    public Response getAllComTaskExecutions(@PathParam("mRID") String mrid, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter queryFilter) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        List<DeviceComTaskInfo> deviceSchedulesInfos = deviceComTaskInfoFactory.from(device.getComTaskExecutions(), deviceConfiguration.getComTaskEnablements(), device);
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
        if(comTaskExecutions.isEmpty()){
            List<ComTaskEnablement> comTaskEnablements = getComTaskEnablementsForDeviceAndComtask(comTaskId, device);
            for (ComTaskEnablement comTaskEnablement : comTaskEnablements) {
                comTaskExecutions.add(createManuallyScheduledComTaskExecutionWithoutFrequency(device, comTaskEnablement));
            }
        }
        if (!comTaskExecutions.isEmpty()) {
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
        if(comTaskExecutions.isEmpty()){
            List<ComTaskEnablement> comTaskEnablements = getComTaskEnablementsForDeviceAndComtask(comTaskId, device);
            for (ComTaskEnablement comTaskEnablement : comTaskEnablements) {
                comTaskExecutions.add(createManuallyScheduledComTaskExecutionWithoutFrequency(device, comTaskEnablement));
            }
        }
        if (!comTaskExecutions.isEmpty()) {
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
    public Response updateFrequency(@PathParam("mRID") String mrid, @PathParam("comTaskId") Long comTaskId, ComTaskFrequencyInfo comTaskFrequencyInfo) {
        checkForNoActionsAllowedOnSystemComTask(comTaskId);
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutionsForDeviceAndComTask(comTaskId, device);
        if(comTaskExecutions.isEmpty()){
            List<ComTaskEnablement> comTaskEnablements = getComTaskEnablementsForDeviceAndComtask(comTaskId, device);
            for (ComTaskEnablement comTaskEnablement : comTaskEnablements) {
                comTaskExecutions.add(createManuallyScheduledComTaskExecutionWithoutFrequency(device, comTaskEnablement));
            }
        }
        if (!comTaskExecutions.isEmpty()) {
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
    public Response updateConnectionMethod(@PathParam("mRID") String mrid, @PathParam("comTaskId") Long comTaskId, ComTaskConnectionMethodInfo comTaskConnectionMethodInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutionsForDeviceAndComTask(comTaskId, device);
        if(comTaskExecutions.isEmpty()){
            List<ComTaskEnablement> comTaskEnablements = getComTaskEnablementsForDeviceAndComtask(comTaskId, device);
            for (ComTaskEnablement comTaskEnablement : comTaskEnablements) {
                comTaskExecutions.add(createManuallyScheduledComTaskExecutionWithoutFrequency(device, comTaskEnablement));
            }
        }
        if (!comTaskExecutions.isEmpty()) {
            comTaskExecutions.forEach(updateComTaskConnectionMethod(comTaskConnectionMethodInfo, device));
        } else {
            throw exceptionFactory.newException(MessageSeeds.UPDATE_CONNECTION_METHOD_NOT_ALLOWED);
        }
        return Response.ok().build();
    }


    private void checkForNoActionsAllowedOnSystemComTask(@PathParam("comTaskId") Long comTaskId) {
        ComTask comTask = taskService.findComTask(comTaskId).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_COM_TASK, comTaskId));
        if (comTask.isSystemComTask()) {
            throw exceptionFactory.newException(MessageSeeds.CAN_NOT_PERFORM_ACTION_ON_SYSTEM_COMTASK);
        }
    }

    @PUT
    @Path("/{comTaskId}/run")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION})
    public Response run(@PathParam("mRID") String mrid, @PathParam("comTaskId") Long comTaskId) {
        checkForNoActionsAllowedOnSystemComTask(comTaskId);
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutionsForDeviceAndComTask(comTaskId, device);
        if (!comTaskExecutions.isEmpty()) {
            comTaskExecutions.forEach(runComTaskFromExecution());
        } else {
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
        checkForNoActionsAllowedOnSystemComTask(comTaskId);
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutionsForDeviceAndComTask(comTaskId, device);
        if (!comTaskExecutions.isEmpty()) {
            comTaskExecutions.forEach(runComTaskFromExecutionNow());
        } else {
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
                                                     @PathParam("comTaskId") long comTaskId, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ComTask comTask = this.taskService.findComTask(comTaskId).orElse(null);
        if (comTask == null || !device.getDeviceConfiguration().getComTaskEnablementFor(comTask).isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_COM_TASK);
        }
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutionsForDeviceAndComTask(comTaskId, device);
        if(comTaskExecutions.isEmpty()){
            List<ComTaskEnablement> comTaskEnablements = getComTaskEnablementsForDeviceAndComtask(comTaskId, device);
            for (ComTaskEnablement comTaskEnablement : comTaskEnablements) {
                comTaskExecutions.add(createManuallyScheduledComTaskExecutionWithoutFrequency(device, comTaskEnablement));
            }
        }
        ComTaskExecution comTaskExecution = getComTaskExecutionForDeviceAndComTaskOrThrowException(comTaskId, device);
        List<ComTaskExecutionSessionInfo> infos = new ArrayList<>();
        List<ComTaskExecutionSession> comTaskExecutionSessions = communicationTaskService.findSessionsByComTaskExecutionAndComTask(comTaskExecution, comTask).from(queryParameters).find();
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
        List<ComTaskExecutionSession> comTaskExecutionSessions = communicationTaskService.findSessionsByComTaskExecution(comTaskExecution).find();
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
                                                            @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ComTask comTask = taskService.findComTask(comTaskId).orElse(null);
        if (comTask == null || !device.getDeviceConfiguration().getComTaskEnablementFor(comTask).isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_COM_TASK);
        }
        ComTaskExecutionSession comTaskExecutionSession = findComTaskExecutionSessionOrThrowException(sessionId, comTask);
        EnumSet<ComServer.LogLevel> logLevels = EnumSet.noneOf(ComServer.LogLevel.class);
        if (jsonQueryFilter.hasProperty(LOG_LEVELS_FILTER_PROPERTY)) {
            jsonQueryFilter.getPropertyList(LOG_LEVELS_FILTER_PROPERTY, new LogLevelAdapter()).stream().forEach(logLevels::add);
        } else {
            logLevels = EnumSet.allOf(ComServer.LogLevel.class);
        }
        List<JournalEntryInfo> infos = comTaskExecutionSession.findComTaskExecutionJournalEntries(logLevels).from(queryParameters).stream().map(journalEntryInfoFactory::asInfo).collect(toList());

        return PagedInfoList.fromPagedList("comTaskExecutionSessions", infos, queryParameters);

    }

    @PUT
    @Path("{comTaskId}/activate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response activateComTask(@PathParam("mRID") String mrid, @PathParam("comTaskId") long comTaskId) {
        checkForNoActionsAllowedOnSystemComTask(comTaskId);
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        activateComTaskOnDevice(device, comTaskId);
        return Response.ok().build();
    }

    private void activateComTaskOnDevice(Device device, long comTaskId) {
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutionsForDeviceAndComTask(comTaskId, device);
        if (comTaskExecutions.isEmpty()) {
            List<ComTaskEnablement> comTaskEnablements = getComTaskEnablementsForDeviceAndComtask(comTaskId, device);
            for (ComTaskEnablement comTaskEnablement : comTaskEnablements) {
                comTaskExecutions.add(createManuallyScheduledComTaskExecutionWithoutFrequency(device, comTaskEnablement));
            }
        }
        comTaskExecutions.stream().filter(cte -> cte.isOnHold()).forEach(cte -> cte.updateNextExecutionTimestamp());
    }

    @PUT
    @Path("{comTaskId}/deactivate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response deactivateComTask(@PathParam("mRID") String mrid, @PathParam("comTaskId") long comTaskId) {
        checkForNoActionsAllowedOnSystemComTask(comTaskId);
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        deactivateComTaskOnDevice(device, comTaskId);
        return Response.ok().build();
    }

    private void deactivateComTaskOnDevice(Device device, long comTaskId) {
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutionsForDeviceAndComTask(comTaskId, device);
        if (comTaskExecutions.isEmpty()) {
            List<ComTaskEnablement> comTaskEnablements = getComTaskEnablementsForDeviceAndComtask(comTaskId, device);
            for (ComTaskEnablement comTaskEnablement : comTaskEnablements) {
                comTaskExecutions.add(createManuallyScheduledComTaskExecutionWithoutFrequency(device, comTaskEnablement));
            }
        }
        comTaskExecutions.stream().filter(cte -> !cte.isOnHold()).forEach(cte -> cte.putOnHold());
    }

    @PUT
    @Path("/activate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response activateAllComTasks(@PathParam("mRID") String mrid) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        device.getDeviceConfiguration().getComTaskEnablements().stream()
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().isUserComTask())
                .map(comTaskEnablement -> comTaskEnablement.getComTask().getId())
                .forEach(comTaskId -> activateComTaskOnDevice(device, comTaskId));
        return Response.ok().build();
    }

    @PUT
    @Path("/deactivate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response deactivateAllComTasks(@PathParam("mRID") String mrid) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        device.getDeviceConfiguration().getComTaskEnablements().stream()
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().isUserComTask())
                .map(comTaskEnablement -> comTaskEnablement.getComTask().getId())
                .forEach(comTaskId -> deactivateComTaskOnDevice(device, comTaskId));
        return Response.ok().build();
    }

    private ComTaskExecutionSession findComTaskExecutionSessionOrThrowException(long sessionId, ComTask comTask) {
        Optional<ComTaskExecutionSession> session = this.communicationTaskService.findSession(sessionId);
        if (session.isPresent() && session.get().getComTask().getId() == comTask.getId()) {
            return session.get();
        }
        else {
            throw this.exceptionFactory.newException(MessageSeeds.NO_SUCH_COM_TASK_EXEC_SESSION);
        }
    }


    private Consumer<? super ComTaskEnablement> runComTaskFromEnablement(Device device) {
        return comTaskEnablement -> {
            ManuallyScheduledComTaskExecution manuallyScheduledComTaskExecution = createManuallyScheduledComTaskExecutionWithoutFrequency(device, comTaskEnablement);
            manuallyScheduledComTaskExecution.scheduleNow();
        };
    }

    private Consumer<? super ComTaskEnablement> runComTaskFromEnablementNow(Device device) {
        return comTaskEnablement -> {
            ManuallyScheduledComTaskExecution manuallyScheduledComTaskExecution = createManuallyScheduledComTaskExecutionWithoutFrequency(device, comTaskEnablement);
            manuallyScheduledComTaskExecution.runNow();
        };
    }

    private ManuallyScheduledComTaskExecution createManuallyScheduledComTaskExecutionWithoutFrequency(Device device, ComTaskEnablement comTaskEnablement) {
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> manuallyScheduledComTaskExecutionComTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        if (comTaskEnablement.hasPartialConnectionTask()) {
            device.getConnectionTasks().stream()
                    .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == comTaskEnablement.getPartialConnectionTask().get().getId())
                    .forEach(manuallyScheduledComTaskExecutionComTaskExecutionBuilder::connectionTask);
        }
        ManuallyScheduledComTaskExecution manuallyScheduledComTaskExecution = manuallyScheduledComTaskExecutionComTaskExecutionBuilder.add();
        device.save();
        return manuallyScheduledComTaskExecution;
    }

    private Consumer<? super ComTaskExecution> runComTaskFromExecution() {
        return ComTaskExecution::scheduleNow;
    }

    private Consumer<? super ComTaskExecution> runComTaskFromExecutionNow() {
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
            if (comTaskExecution.isScheduledManually() || comTaskExecution.isAdHoc()) {
                device.getComTaskExecutionUpdater((ManuallyScheduledComTaskExecution) comTaskExecution).scheduleAccordingTo(comTaskFrequencyInfo.temporalExpression.asTemporalExpression()).update();
            }
        };
    }

    private Consumer<ComTaskEnablement> createManuallyScheduledComTaskExecutionForEnablement(ComTaskFrequencyInfo comTaskFrequencyInfo, Device device) {
        return comTaskEnablement -> {
            ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> manuallyScheduledComTaskExecutionComTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, comTaskFrequencyInfo.temporalExpression.asTemporalExpression());
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