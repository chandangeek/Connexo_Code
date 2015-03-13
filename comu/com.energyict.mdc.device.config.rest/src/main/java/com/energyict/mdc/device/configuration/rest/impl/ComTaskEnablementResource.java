package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ComTaskEnablementBuilder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.security.Privileges;
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
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

public class ComTaskEnablementResource {

    private final ResourceHelper resourceHelper;
    private final TaskService taskService;
    private final Thesaurus thesaurus;

    @Inject
    public ComTaskEnablementResource(ResourceHelper resourceHelper, TaskService taskService, Thesaurus thesaurus) {
        this.resourceHelper = resourceHelper;
        this.taskService = taskService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE, Privileges.VIEW_DEVICE_TYPE})
    public PagedInfoList getComTaskEnablements(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @BeanParam QueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<ComTaskEnablementInfo> comTaskEnablements = ComTaskEnablementInfo.from(ListPager.of(deviceConfiguration.getComTaskEnablements(), new ComTaskEnablementComparator()).find(), thesaurus);

        return PagedInfoList.fromPagedList("data", comTaskEnablements, queryParameters);
    }

    @GET
    @Path("/{comTaskEnablementId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE, Privileges.VIEW_DEVICE_TYPE})
    public Response getComTaskEnablement(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("comTaskEnablementId") long comTaskEnablementId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        ComTaskEnablement comTaskEnablement = findComTaskEnablementOrThrowException(deviceConfiguration, comTaskEnablementId);

        return Response.ok(ComTaskEnablementInfo.from(comTaskEnablement, thesaurus)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public Response createComTaskEnablement(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, ComTaskEnablementInfo comTaskEnablementInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);

        ComTask comTask = comTaskEnablementInfo.comTask != null ? this.findComTaskOrThrowException(comTaskEnablementInfo.comTask.id) : null;
        SecurityPropertySet securityPropertySet = comTaskEnablementInfo.securityPropertySet != null ?
                resourceHelper.findAnySecurityPropertySetByIdOrThrowException(comTaskEnablementInfo.securityPropertySet.id) : null;

        ComTaskEnablementInfo.PartialConnectionTaskInfo partialConnectionTaskInfoParameter = comTaskEnablementInfo.partialConnectionTask;

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = comTaskEnablementInfo.protocolDialectConfigurationProperties != null ?
                resourceHelper.findAnyProtocolDialectConfigurationPropertiesByIdOrThrowException(comTaskEnablementInfo.protocolDialectConfigurationProperties.id) : null;

        ComTaskEnablementBuilder comTaskEnablementBuilder = deviceConfiguration.enableComTask(comTask, securityPropertySet, protocolDialectConfigurationProperties)
                .setPriority(comTaskEnablementInfo.priority)
                .setIgnoreNextExecutionSpecsForInbound(comTaskEnablementInfo.ignoreNextExecutionSpecsForInbound);

        if (partialConnectionTaskInfoParameter != null && !comTaskEnablementInfo.partialConnectionTask.id.equals(ComTaskEnablementInfo.PartialConnectionTaskInfo.DEFAULT_PARTIAL_CONNECTION_TASK_ID)) {
            PartialConnectionTask partialConnectionTask = findPartialConnectionTaskOrThrowException(deviceConfiguration, comTaskEnablementInfo.partialConnectionTask.id);
            comTaskEnablementBuilder.setPartialConnectionTask(partialConnectionTask).useDefaultConnectionTask(Boolean.FALSE);
        }

        ComTaskEnablement comTaskEnablement = comTaskEnablementBuilder.add();
        return Response.status(Response.Status.CREATED).entity(ComTaskEnablementInfo.from(comTaskEnablement, thesaurus)).build();
    }

    @PUT
    @Path("/{comTaskEnablementId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public Response updateComTaskEnablement(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("comTaskEnablementId") long comTaskEnablementId, ComTaskEnablementInfo comTaskEnablementInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);

        ComTaskEnablementInfo.PartialConnectionTaskInfo partialConnectionTaskInfoParameter = comTaskEnablementInfo.partialConnectionTask;

        ComTaskEnablement comTaskEnablement = findComTaskEnablementOrThrowException(deviceConfiguration, comTaskEnablementId);
        SecurityPropertySet securityPropertySet = comTaskEnablementInfo.securityPropertySet != null ?
                resourceHelper.findAnySecurityPropertySetByIdOrThrowException(comTaskEnablementInfo.securityPropertySet.id) : null;
        comTaskEnablementInfo.writeTo(comTaskEnablement);
        comTaskEnablement.setSecurityPropertySet(securityPropertySet);
        if (partialConnectionTaskInfoParameter != null && !comTaskEnablementInfo.partialConnectionTask.id.equals(ComTaskEnablementInfo.PartialConnectionTaskInfo.DEFAULT_PARTIAL_CONNECTION_TASK_ID)) {
            PartialConnectionTask partialConnectionTask = findPartialConnectionTaskOrThrowException(deviceConfiguration, comTaskEnablementInfo.partialConnectionTask.id);
            comTaskEnablement.setPartialConnectionTask(partialConnectionTask);
            comTaskEnablement.useDefaultConnectionTask(Boolean.FALSE);
        } else {
            comTaskEnablement.useDefaultConnectionTask(Boolean.TRUE);
        }

        comTaskEnablement.save();

        return Response.ok(ComTaskEnablementInfo.from(comTaskEnablement, thesaurus)).build();
    }

    @PUT
    @Path("/{comTaskEnablementId}/activate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public Response activateComTaskEnablement(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("comTaskEnablementId") long comTaskEnablementId) {
        this.setComTaskEnablementActive(deviceTypeId, deviceConfigurationId, comTaskEnablementId, true);
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/{comTaskEnablementId}/deactivate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public Response deactivateComTaskEnablement(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("comTaskEnablementId") long comTaskEnablementId) {
        this.setComTaskEnablementActive(deviceTypeId, deviceConfigurationId, comTaskEnablementId, false);
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/{comTaskEnablementId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
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

        String availableParameter = uriInfo.getQueryParameters().getFirst("available");

        List<ComTaskEnablement> deviceConfigurationComTaskEnablements = deviceConfiguration.getComTaskEnablements();
        List<ComTask> deviceConfigurationComTasks = new ArrayList<>(deviceConfigurationComTaskEnablements.size());
        for (ComTaskEnablement comTaskEnablement : deviceConfigurationComTaskEnablements) {
            deviceConfigurationComTasks.add(comTaskEnablement.getComTask());
        }
        if (availableParameter != null && "true".equalsIgnoreCase(availableParameter)) {
            List<ComTask> filteredDeviceConfigurationComTasks = filterComTasks(deviceConfigurationComTasks);
            deviceConfigurationComTasks.clear();
            deviceConfigurationComTasks.addAll(filteredDeviceConfigurationComTasks);
        }
        List<ComTaskEnablementInfo.ComTaskInfo> deviceConfigurationComTaskInfos = ComTaskEnablementInfo.ComTaskInfo.from(ListPager.of(deviceConfigurationComTasks, new ComTaskComparator()).find());

        return PagedInfoList.fromPagedList("data", deviceConfigurationComTaskInfos, queryParameters);
    }

    private void setComTaskEnablementActive(long deviceTypeId, long deviceConfigurationId, long comTaskEnablementId, boolean setActive) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        ComTaskEnablement comTaskEnablement = findComTaskEnablementOrThrowException(deviceConfiguration, comTaskEnablementId);

        if (setActive) {
            if(comTaskEnablement.isSuspended()) {
                comTaskEnablement.resume();
            }
        } else {
            if(!comTaskEnablement.isSuspended()) {
                comTaskEnablement.suspend();
            }
        }
    }

    private List<ComTask> filterComTasks(List<ComTask> deviceConfigurationComTasks) {
        List<ComTask> filteredResult = new ArrayList<>();
        List<ComTask> allComTasks = taskService.findAllComTasks();

        outer:
        for (ComTask comTask : allComTasks) {
            for (ComTask dcComTask : deviceConfigurationComTasks) {
                if (comTask.getId() == dcComTask.getId()) {
                    continue outer;
                }
            }
            filteredResult.add(comTask);
        }

        return filteredResult;
    }

/*    private SecurityPropertySet findSecurityPropertySetByIdOrThrowException(DeviceConfiguration deviceConfiguration, long securityPropertySetId) {
        for (SecurityPropertySet securityPropertySet : deviceConfiguration.getSecurityPropertySets()) {
            if (securityPropertySet.getId() == securityPropertySetId) {
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
    }*/

    private PartialConnectionTask findPartialConnectionTaskOrThrowException(DeviceConfiguration deviceConfiguration, long connectionMethodId) {
        for (PartialConnectionTask partialConnectionTask : deviceConfiguration.getPartialConnectionTasks()) {
            if (partialConnectionTask.getId() == connectionMethodId) {
                return partialConnectionTask;
            }
        }

        throw new WebApplicationException("No such connection task for the device configuration", Response.status(Response.Status.NOT_FOUND).entity("No such connection task for the device configuration").build());
    }

    private ComTask findComTaskOrThrowException(long comTaskId) {
        return this.taskService
                .findComTask(comTaskId)
                .orElseThrow(() -> new WebApplicationException("No such communication task", Response.status(Response.Status.NOT_FOUND).entity("No such communication task").build()));
    }

    private ComTaskEnablement findComTaskEnablementOrThrowException(DeviceConfiguration deviceConfiguration, long comTaskEnablementId) {
        for (ComTaskEnablement comTaskEnablement : deviceConfiguration.getComTaskEnablements()) {
            if (comTaskEnablement.getId() == comTaskEnablementId) {
                return comTaskEnablement;
            }
        }

        throw new WebApplicationException("No such communication task configuration for the device configuration", Response.status(Response.Status.NOT_FOUND).entity("No such communication task configuration for the device configuration").build());
    }

}