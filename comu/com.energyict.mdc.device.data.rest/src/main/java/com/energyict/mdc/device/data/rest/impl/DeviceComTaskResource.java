package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.*;
import com.energyict.mdc.tasks.ComTask;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DeviceComTaskResource {
    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final Thesaurus thesaurus;
    private final DeviceComTaskInfoFactory deviceComTaskInfoFactory;
    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public DeviceComTaskResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, Thesaurus thesaurus, DeviceComTaskInfoFactory deviceComTaskInfoFactory, DeviceConfigurationService deviceConfigurationService) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.thesaurus = thesaurus;
        this.deviceComTaskInfoFactory = deviceComTaskInfoFactory;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllComTaskExecutions(@PathParam("mRID") String mrid, @BeanParam QueryParameters queryParameters, @BeanParam JsonQueryFilter queryFilter){
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        List<ComTaskExecution> comTaskExecutions = device.getComTaskExecutions();
        List<ComTaskEnablement> comTaskEnablements = deviceConfiguration.getComTaskEnablements();
        List<DeviceComTaskInfo> deviceSchedulesInfos = deviceComTaskInfoFactory.from(comTaskExecutions,comTaskEnablements);
        return Response.ok(PagedInfoList.asJson("comTasks", deviceSchedulesInfos, queryParameters)).build();
    }

    @PUT
    @Path("/{comTaskId}/urgency")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateComTaskExecution(@PathParam("mRID") String mrid,@PathParam("comTaskId") Long comTaskId, ComTaskUrgencyInfo comTaskUrgencyInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutionForDeviceAndComTask(comTaskId, device);
        if(comTaskExecutions.size()>0){
            comTaskExecutions.forEach(updateUrgency(comTaskUrgencyInfo, device));
        } else {
            throw exceptionFactory.newException(MessageSeeds.UPDATE_URGENCY_NOT_ALLOWED);
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/{comTaskId}/protocoldialect")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateProtocolDialect(@PathParam("mRID") String mrid,@PathParam("comTaskId") Long comTaskId, ComTaskProtocolDialectInfo comTaskProtocolDialectInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutionForDeviceAndComTask(comTaskId, device);
        if(comTaskExecutions.size()>0){
            comTaskExecutions.forEach(updateProtocolDialect(comTaskProtocolDialectInfo, device));
        } else {
            throw exceptionFactory.newException(MessageSeeds.UPDATE_DIALECT_PROPERTIES_NOT_ALLOWED);
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/{comTaskId}/frequency")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateConnectionMethod(@PathParam("mRID") String mrid,@PathParam("comTaskId") Long comTaskId, ComTaskFrequencyInfo comTaskFrequencyInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutionForDeviceAndComTask(comTaskId, device);
        if(comTaskExecutions.size()>0){
            comTaskExecutions.forEach(updateComTaskExecutionFrequency(comTaskFrequencyInfo, device));
        } else {
            List<ComTaskEnablement> comTaskEnablements = getComTaskEnablementsForDeviceAndComtask(comTaskId, device);
            comTaskEnablements.forEach(createManuallyScheduledComTaskExecutionForEnablement(comTaskFrequencyInfo, device));
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/{comTaskId}/connectionmethod")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateFrequency(@PathParam("mRID") String mrid,@PathParam("comTaskId") Long comTaskId, ComTaskConnectionMethodInfo comTaskConnectionMethodInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutionForDeviceAndComTask(comTaskId, device);
        if(comTaskExecutions.size()>0){
            comTaskExecutions.forEach(updateComTaskConnectionMethod(comTaskConnectionMethodInfo, device));
        } else {
            throw exceptionFactory.newException(MessageSeeds.UPDATE_CONNECTION_METHOD_NOT_ALLOWED);
        }
        return Response.ok().build();
    }


    @PUT
    @Path("/{comTaskId}/run")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response run(@PathParam("mRID") String mrid,@PathParam("comTaskId") Long comTaskId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutionForDeviceAndComTask(comTaskId, device);
        if(comTaskExecutions.size()>0){
            comTaskExecutions.forEach(runComTaskFromExecution(device));
        } else if(comTaskExecutions.size()==0){
            List<ComTaskEnablement> comTaskEnablements = getComTaskEnablementsForDeviceAndComtask(comTaskId, device);
            comTaskEnablements.forEach(runComTaskFromEnablement(device));
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/{comTaskId}/runnow")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response runnow(@PathParam("mRID") String mrid,@PathParam("comTaskId") Long comTaskId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutionForDeviceAndComTask(comTaskId, device);
        if(comTaskExecutions.size()>0){
            comTaskExecutions.forEach(runComTaskFromExecutionNow(device));
        } else if(comTaskExecutions.size()==0){
            List<ComTaskEnablement> comTaskEnablements = getComTaskEnablementsForDeviceAndComtask(comTaskId, device);
            comTaskEnablements.forEach(runComTaskFromEnablementNow(device));
        }
        return Response.ok().build();
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


    private List<ComTaskExecution> getComTaskExecutionForDeviceAndComTask(Long comTaskId, Device device) {
        return device.getComTaskExecutions().stream()
                    .filter(comTaskExecution -> comTaskExecution.getComTasks().stream()
                            .mapToLong(ComTask::getId)
                            .anyMatch(comTaskId::equals))
                   .collect(Collectors.toList());
    }

    private List<ComTaskEnablement> getComTaskEnablementsForDeviceAndComtask(Long comTaskId, Device device){
        return device.getDeviceConfiguration().getComTaskEnablements().stream()
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().getId()== comTaskId)
                .collect(Collectors.toList());
    }

    private Consumer<ComTaskExecution> updateUrgency(ComTaskUrgencyInfo comTaskUrgencyInfo, Device device) {
        return comTaskExecution -> {
            if(comTaskExecution.isScheduledManually()){
                device.getComTaskExecutionUpdater((ManuallyScheduledComTaskExecution)comTaskExecution).priority(comTaskUrgencyInfo.urgency).update();
            } else if (comTaskExecution.usesSharedSchedule()){
                device.getComTaskExecutionUpdater((ScheduledComTaskExecution)comTaskExecution).priority(comTaskUrgencyInfo.urgency).update();
            } else {
               throw exceptionFactory.newException(MessageSeeds.UPDATE_URGENCY_NOT_ALLOWED);
            }
        };
    }

    private Consumer<ComTaskExecution> updateProtocolDialect(ComTaskProtocolDialectInfo comTaskProtocolDialectInfo, Device device){
        return comTaskExecution -> {
            List<ProtocolDialectConfigurationProperties> protocolDialectConfigurationPropertiesList = device.getDeviceConfiguration().getProtocolDialectConfigurationPropertiesList();
            Optional<ProtocolDialectConfigurationProperties> dialectConfigurationPropertiesOptional = protocolDialectConfigurationPropertiesList.stream()
                    .filter(protocolDialectConfigurationProperties -> protocolDialectConfigurationProperties.getDeviceProtocolDialect().getDisplayName().equals(comTaskProtocolDialectInfo.protocolDialect))
                    .findFirst();
            if(comTaskExecution.isScheduledManually() && dialectConfigurationPropertiesOptional.isPresent()){
                device.getComTaskExecutionUpdater((ManuallyScheduledComTaskExecution)comTaskExecution).protocolDialectConfigurationProperties(dialectConfigurationPropertiesOptional.get()).update();
                device.save();
            } else {
                throw exceptionFactory.newException(MessageSeeds.UPDATE_DIALECT_PROPERTIES_NOT_ALLOWED);
            }
        };
    }

    private Consumer<ComTaskExecution> updateComTaskExecutionFrequency(ComTaskFrequencyInfo comTaskFrequencyInfo, Device device){
        return comTaskExecution -> {
            if(comTaskExecution.isScheduledManually() || comTaskExecution.isAdHoc())
           device.getComTaskExecutionUpdater((ManuallyScheduledComTaskExecution)comTaskExecution).scheduleAccordingTo(comTaskFrequencyInfo.temporalExpression.asTemporalExpression()).update();
        };
    }

    private Consumer<ComTaskEnablement> createManuallyScheduledComTaskExecutionForEnablement(ComTaskFrequencyInfo comTaskFrequencyInfo, Device device){
        return comTaskEnablement -> {
            ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> manuallyScheduledComTaskExecutionComTaskExecutionBuilder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, comTaskEnablement.getProtocolDialectConfigurationProperties().orElse(null), comTaskFrequencyInfo.temporalExpression.asTemporalExpression());
            if(comTaskEnablement.hasPartialConnectionTask()){
                device.getConnectionTasks().stream()
                        .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == comTaskEnablement.getPartialConnectionTask().get().getId())
                        .forEach(manuallyScheduledComTaskExecutionComTaskExecutionBuilder::connectionTask);
            }
            manuallyScheduledComTaskExecutionComTaskExecutionBuilder.add();
            device.save();
        };
    }

    private Consumer<ComTaskExecution> updateComTaskConnectionMethod(ComTaskConnectionMethodInfo comTaskConnectionMethodInfo, Device device){
        return comTaskExecution -> {
                Optional<ConnectionTask<?, ?>> connectionTaskOptional = device.getConnectionTasks().stream().filter(ct -> ct.getName().equals(comTaskConnectionMethodInfo.connectionMethod)).findFirst();
                if(connectionTaskOptional.isPresent()){
                    if(comTaskExecution.isScheduledManually()){
                        device.getComTaskExecutionUpdater((ManuallyScheduledComTaskExecution)comTaskExecution).connectionTask(connectionTaskOptional.get()).update();
                    } else {
                        device.getComTaskExecutionUpdater((ScheduledComTaskExecution)comTaskExecution).connectionTask(connectionTaskOptional.get()).update();
                    }
                } else {
                    if(comTaskExecution.isScheduledManually()) {
                        device.getComTaskExecutionUpdater((ManuallyScheduledComTaskExecution)comTaskExecution).useDefaultConnectionTask(true).update();
                    } else {
                        device.getComTaskExecutionUpdater((ScheduledComTaskExecution)comTaskExecution).useDefaultConnectionTask(true).update();
                    }
                }
        };
    }

}
