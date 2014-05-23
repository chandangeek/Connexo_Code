package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

public class ComTaskEnablementResource {

    private final ResourceHelper resourceHelper;
    private final DeviceConfigurationService deviceConfigurationService;
    private final TaskService taskService;
    private final Thesaurus thesaurus;

    @Inject
    public ComTaskEnablementResource(ResourceHelper resourceHelper, DeviceConfigurationService deviceConfigurationService, TaskService taskService, Thesaurus thesaurus) {
        this.resourceHelper = resourceHelper;
        this.deviceConfigurationService = deviceConfigurationService;
        this.taskService = taskService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getComTaskEnablements(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @BeanParam QueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<ComTaskEnablementInfo> comTaskEnablements = ComTaskEnablementInfo.from(ListPager.of(deviceConfiguration.getComTaskEnablements(), new ComTaskEnablementComparator()).find(), thesaurus);

        return PagedInfoList.asJson("data", comTaskEnablements, queryParameters);
    }

    @GET
    @Path("/{comTaskEnablementId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getComTaskEnablement(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("comTaskEnablementId") long comTaskEnablementId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        ComTaskEnablement comTaskEnablement = findComTaskEnablementOrThrowException(deviceConfiguration, comTaskEnablementId);

        return Response.status(Response.Status.OK).entity(ComTaskEnablementInfo.from(comTaskEnablement, thesaurus)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createComTaskEnablement(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, ComTaskEnablementInfo comTaskEnablementInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);

        ComTask comTask = taskService.findComTask(comTaskEnablementInfo.comTask.id);
        SecurityPropertySet securityPropertySet = findSecurityPropertySetByIdOrThrowException(deviceConfiguration, comTaskEnablementInfo.securityPropertySet.id);

        Optional<ComTaskEnablementInfo.PartialConnectionTaskInfo> partialConnectionTaskInfoParameter = Optional.fromNullable(comTaskEnablementInfo.partialConnectionTask);
        Optional<ComTaskEnablementInfo.ProtocolDialectConfigurationPropertiesInfo> protocolDialectConfigurationPropertiesInfoParameter = Optional.fromNullable(comTaskEnablementInfo.protocolDialectConfigurationProperties);
        Optional<TemporalExpressionInfo> nextExecutionSpecsParameter = Optional.fromNullable(comTaskEnablementInfo.nextExecutionSpecs);

        ComTaskEnablementBuilder comTaskEnablementBuilder = deviceConfiguration.enableComTask(comTask, securityPropertySet)
                .setPriority(comTaskEnablementInfo.priority)
                .setIgnoreNextExecutionSpecsForInbound(comTaskEnablementInfo.ignoreNextExecutionSpecsForInbound);

        if(partialConnectionTaskInfoParameter.isPresent() && comTaskEnablementInfo.partialConnectionTask.id != ComTaskEnablementInfo.PartialConnectionTaskInfo.DEFAULT_PARTIAL_CONNECTION_TASK_ID) {
            PartialConnectionTask partialConnectionTask = findPartialConnectionTaskOrThrowException(deviceConfiguration, comTaskEnablementInfo.partialConnectionTask.id);
            comTaskEnablementBuilder.setPartialConnectionTask(partialConnectionTask).useDefaultConnectionTask(Boolean.FALSE);
        }

        if(protocolDialectConfigurationPropertiesInfoParameter.isPresent() && comTaskEnablementInfo.protocolDialectConfigurationProperties.id != ComTaskEnablementInfo.ProtocolDialectConfigurationPropertiesInfo.DEFAULT_PROTOCOL_DIALECT_ID) {
            ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = findProtocolDialectOrThrowException(deviceConfiguration, comTaskEnablementInfo.protocolDialectConfigurationProperties.id);
            comTaskEnablementBuilder.setProtocolDialectConfigurationProperties(protocolDialectConfigurationProperties);
        }

        if(nextExecutionSpecsParameter.isPresent()) {
            comTaskEnablementBuilder.setNextExecutionSpecsFrom(comTaskEnablementInfo.nextExecutionSpecs.asTemporalExpression());
        }

        comTaskEnablementBuilder.add();

        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/{comTaskEnablementId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateComTaskEnablement(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("comTaskEnablementId") long comTaskEnablementId, ComTaskEnablementInfo comTaskEnablementInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);

        Optional<ComTaskEnablementInfo.PartialConnectionTaskInfo> partialConnectionTaskInfoParameter = Optional.fromNullable(comTaskEnablementInfo.partialConnectionTask);

        ComTaskEnablement comTaskEnablement = findComTaskEnablementOrThrowException(deviceConfiguration, comTaskEnablementId);
        SecurityPropertySet securityPropertySet = findSecurityPropertySetByIdOrThrowException(deviceConfiguration, comTaskEnablementInfo.securityPropertySet.id);
        comTaskEnablementInfo.writeTo(comTaskEnablement);
        comTaskEnablement.setSecurityPropertySet(securityPropertySet);
        if(partialConnectionTaskInfoParameter.isPresent() && comTaskEnablementInfo.partialConnectionTask.id != ComTaskEnablementInfo.PartialConnectionTaskInfo.DEFAULT_PARTIAL_CONNECTION_TASK_ID) {
            PartialConnectionTask partialConnectionTask = findPartialConnectionTaskOrThrowException(deviceConfiguration, comTaskEnablementInfo.partialConnectionTask.id);
            comTaskEnablement.setPartialConnectionTask(partialConnectionTask);
            comTaskEnablement.useDefaultConnectionTask(Boolean.FALSE);
        } else {
            comTaskEnablement.useDefaultConnectionTask(Boolean.TRUE);
        }

        comTaskEnablement.save();

        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/{comTaskEnablementId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteComTaskEnablement(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("comTaskEnablementId") long comTaskEnablementId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        ComTaskEnablement comTaskEnablement = findComTaskEnablementOrThrowException(deviceConfiguration, comTaskEnablementId);
        deviceConfiguration.disableComTask(comTaskEnablement.getComTask());

        return Response.status(Response.Status.OK).build();
    }

    public PagedInfoList getComTasks(long deviceTypeId, long deviceConfigurationId, QueryParameters queryParameters, UriInfo uriInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);

        Optional<String> availableParameter = Optional.fromNullable(uriInfo.getQueryParameters().getFirst("available"));

        List<ComTaskEnablement> deviceConfigurationComTaskEnablements = deviceConfiguration.getComTaskEnablements();
        List<ComTask> deviceConfigurationComTasks = new ArrayList<>(deviceConfigurationComTaskEnablements.size());
        for(ComTaskEnablement comTaskEnablement : deviceConfigurationComTaskEnablements) {
            deviceConfigurationComTasks.add(comTaskEnablement.getComTask());
        }
        if(availableParameter.isPresent() && availableParameter.get().equalsIgnoreCase("true")) {
            List<ComTask> filteredDeviceConfigurationComTasks = filterComTasks(deviceConfigurationComTasks);
            deviceConfigurationComTasks.clear();
            deviceConfigurationComTasks.addAll(filteredDeviceConfigurationComTasks);
        }
        List<ComTaskEnablementInfo.ComTaskInfo> deviceConfigurationComTaskInfos = ComTaskEnablementInfo.ComTaskInfo.from(ListPager.of(deviceConfigurationComTasks, new ComTaskComparator()).find());

        return PagedInfoList.asJson("data", deviceConfigurationComTaskInfos, queryParameters);
    }

    private List<ComTask> filterComTasks(List<ComTask> deviceConfigurationComTasks) {
        List<ComTask> filteredResult = new ArrayList<>();
        List<ComTask> allComTasks = taskService.findAllComTasks();

        outer:
        for(ComTask comTask : allComTasks) {
            for(ComTask dcComTask : deviceConfigurationComTasks) {
                if(comTask.getId() == dcComTask.getId()) {
                    continue outer;
                }
            }
            filteredResult.add(comTask);
        }

        return filteredResult;
    }

    private SecurityPropertySet findSecurityPropertySetByIdOrThrowException(DeviceConfiguration deviceConfiguration, long securityPropertySetId) {
        for(SecurityPropertySet securityPropertySet : deviceConfiguration.getSecurityPropertySets()) {
            if(securityPropertySet.getId() == securityPropertySetId) {
                return securityPropertySet;
            }
        }

        throw new WebApplicationException("No such security property set for the device configuration", Response.status(Response.Status.NOT_FOUND).entity("No such security property set for the device configuration").build());
    }

    private ProtocolDialectConfigurationProperties findProtocolDialectOrThrowException(DeviceConfiguration deviceConfiguration, long protocolDialectId) {
        for (ProtocolDialectConfigurationProperties protocolDialectConfigurationProperty : deviceConfiguration.getProtocolDialectConfigurationPropertiesList()) {
            if (protocolDialectConfigurationProperty.getId() == protocolDialectId) {
                return protocolDialectConfigurationProperty;
            }
        }

        throw new WebApplicationException("No such protocol dialect for the device configuration", Response.status(Response.Status.NOT_FOUND).entity("No such protocol dialect for the device configuration").build());
    }

    private PartialConnectionTask findPartialConnectionTaskOrThrowException(DeviceConfiguration deviceConfiguration, long connectionMethodId) {
        for (PartialConnectionTask partialConnectionTask : deviceConfiguration.getPartialConnectionTasks()) {
            if (partialConnectionTask.getId() == connectionMethodId) {
                return partialConnectionTask;
            }
        }

        throw new WebApplicationException("No such connection task for the device configuration", Response.status(Response.Status.NOT_FOUND).entity("No such connection task for the device configuration").build());
    }

    private ComTaskEnablement findComTaskEnablementOrThrowException(DeviceConfiguration deviceConfiguration, long comTaskEnablementId) {
        for(ComTaskEnablement comTaskEnablement : deviceConfiguration.getComTaskEnablements()) {
            if(comTaskEnablement.getId() == comTaskEnablementId) {
                return comTaskEnablement;
            }
        }

        throw new WebApplicationException("No such communication task configuration for the device configuration", Response.status(Response.Status.NOT_FOUND).entity("No such communication task configuration for the device configuration").build());
    }
}
