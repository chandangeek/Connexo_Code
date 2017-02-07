/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ComTaskEnablementBuilder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.firmware.FirmwareService;
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
import java.util.List;
import java.util.stream.Collectors;

public class ComTaskEnablementResource {

    private final ResourceHelper resourceHelper;
    private final TaskService taskService;
    private final Thesaurus thesaurus;
    private final FirmwareService firmwareService;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public ComTaskEnablementResource(ResourceHelper resourceHelper, TaskService taskService, Thesaurus thesaurus, FirmwareService firmwareService, ConcurrentModificationExceptionFactory conflictFactory) {
        this.resourceHelper = resourceHelper;
        this.taskService = taskService;
        this.thesaurus = thesaurus;
        this.firmwareService = firmwareService;
        this.conflictFactory = conflictFactory;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList getComTaskEnablements(@PathParam("deviceConfigurationId") long deviceConfigurationId, @BeanParam JsonQueryParameters queryParameters) {
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);
        List<ComTaskEnablementInfo> comTaskEnablements = ComTaskEnablementInfo.
                                                            from(ListPager.of(deviceConfiguration.getComTaskEnablements(), new ComTaskEnablementComparator()).
                                                                    from(queryParameters).
                                                                    find(), thesaurus);
        return PagedInfoList.fromPagedList("data", comTaskEnablements, queryParameters);
    }

    @GET @Transactional
    @Path("/{comTaskEnablementId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public Response getComTaskEnablement(@PathParam("comTaskEnablementId") long comTaskEnablementId) {
        ComTaskEnablement comTaskEnablement = resourceHelper.findComTaskEnablementByIdOrThrowException(comTaskEnablementId);
        return Response.ok(ComTaskEnablementInfo.from(comTaskEnablement, thesaurus)).build();
    }

    @POST @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response createComTaskEnablement(@PathParam("deviceConfigurationId") long deviceConfigurationId, ComTaskEnablementInfo info) {
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);

        ComTask comTask = info.comTask != null && info.comTask.id != null ?
                findComTaskOrThrowException(info.comTask.id) : null;
        SecurityPropertySet securityPropertySet = info.securityPropertySet != null && info.securityPropertySet.id != null ?
                resourceHelper.findSecurityPropertySetByIdOrThrowException(info.securityPropertySet.id) : null;

        ComTaskEnablementInfo.PartialConnectionTaskInfo partialConnectionTaskInfoParameter = info.partialConnectionTask;

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = info.protocolDialectConfigurationProperties != null
                && info.protocolDialectConfigurationProperties.id != null ?
                resourceHelper.findProtocolDialectConfigurationPropertiesByIdOrThrowException(info.protocolDialectConfigurationProperties.id) : null;

        ComTaskEnablementBuilder comTaskEnablementBuilder = deviceConfiguration.enableComTask(comTask, securityPropertySet, protocolDialectConfigurationProperties)
                .setPriority(info.priority)
                .setIgnoreNextExecutionSpecsForInbound(info.ignoreNextExecutionSpecsForInbound);

        if (partialConnectionTaskInfoParameter != null && !info.partialConnectionTask.id.equals(ComTaskEnablementInfo.PartialConnectionTaskInfo.DEFAULT_PARTIAL_CONNECTION_TASK_ID)) {
            PartialConnectionTask partialConnectionTask = resourceHelper.findPartialConnectionTaskByIdOrThrowException(info.partialConnectionTask.id);
            comTaskEnablementBuilder.setPartialConnectionTask(partialConnectionTask).useDefaultConnectionTask(Boolean.FALSE);
        }

        ComTaskEnablement comTaskEnablement = comTaskEnablementBuilder.add();
        return Response.status(Response.Status.CREATED).entity(ComTaskEnablementInfo.from(comTaskEnablement, thesaurus)).build();
    }

    @PUT @Transactional
    @Path("/{comTaskEnablementId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response updateComTaskEnablement(@PathParam("comTaskEnablementId") long comTaskEnablementId, ComTaskEnablementInfo info) {
        info.id = comTaskEnablementId;
        ComTaskEnablement comTaskEnablement = resourceHelper.lockComTaskEnablementOrThrowException(info);

        ComTaskEnablementInfo.PartialConnectionTaskInfo partialConnectionTaskInfoParameter = info.partialConnectionTask;

        SecurityPropertySet securityPropertySet = info.securityPropertySet != null ?
                resourceHelper.findSecurityPropertySetByIdOrThrowException(info.securityPropertySet.id) : null;
        info.writeTo(comTaskEnablement);
        comTaskEnablement.setSecurityPropertySet(securityPropertySet);
        if (partialConnectionTaskInfoParameter != null && !info.partialConnectionTask.id.equals(ComTaskEnablementInfo.PartialConnectionTaskInfo.DEFAULT_PARTIAL_CONNECTION_TASK_ID)) {
            PartialConnectionTask partialConnectionTask = resourceHelper.findPartialConnectionTaskByIdOrThrowException(info.partialConnectionTask.id);
            comTaskEnablement.setPartialConnectionTask(partialConnectionTask);
            comTaskEnablement.useDefaultConnectionTask(Boolean.FALSE);
        } else {
            comTaskEnablement.useDefaultConnectionTask(Boolean.TRUE);
        }

        comTaskEnablement.save();

        return Response.ok(ComTaskEnablementInfo.from(comTaskEnablement, thesaurus)).build();
    }

    @PUT @Transactional
    @Path("/{comTaskEnablementId}/activate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response activateComTaskEnablement(@PathParam("comTaskEnablementId") long comTaskEnablementId, ComTaskEnablementInfo info) {
        info.id = comTaskEnablementId;
        ComTaskEnablement comTaskEnablement = resourceHelper.getLockedComTaskEnablement(info.id, info.version)
                .orElseThrow(conflictFactory.conflict()
                        .withMessageTitle(MessageSeeds.CONCURRENT_FAIL_ACTIVATE_TITLE, info.comTask.name)
                        .withMessageBody(MessageSeeds.CONCURRENT_FAIL_ACTIVATE_BODY, info.comTask.name)
                        .withActualVersion(() -> resourceHelper.getCurrentComTaskEnablementVersion(info.id))
                        .supplier());
        this.setComTaskEnablementActive(comTaskEnablement, true);
        return Response.status(Response.Status.OK).build();
    }

    @PUT @Transactional
    @Path("/{comTaskEnablementId}/deactivate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response deactivateComTaskEnablement(@PathParam("comTaskEnablementId") long comTaskEnablementId, ComTaskEnablementInfo info) {
        info.id = comTaskEnablementId;
        ComTaskEnablement comTaskEnablement = resourceHelper.getLockedComTaskEnablement(info.id, info.version)
                .orElseThrow(conflictFactory.conflict()
                        .withMessageTitle(MessageSeeds.CONCURRENT_FAIL_DEACTIVATE_TITLE, info.comTask.name)
                        .withMessageBody(MessageSeeds.CONCURRENT_FAIL_DEACTIVATE_BODY, info.comTask.name)
                        .withActualVersion(() -> resourceHelper.getCurrentComTaskEnablementVersion(info.id))
                        .supplier());
        this.setComTaskEnablementActive(comTaskEnablement, false);
        return Response.status(Response.Status.OK).build();
    }

    @DELETE @Transactional
    @Path("/{comTaskEnablementId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response deleteComTaskEnablement(@PathParam("comTaskEnablementId") long comTaskEnablementId, ComTaskEnablementInfo info) {
        ComTaskEnablement comTaskEnablement = resourceHelper.lockComTaskEnablementOrThrowException(info);
        comTaskEnablement.getDeviceConfiguration().disableComTask(comTaskEnablement.getComTask());

        return Response.status(Response.Status.OK).build();
    }

    /**
     * @return A list of ComTasks which are allowed for the given DeviceType. If the DeviceType doesn't support firmwareUpgrades,
     * then the 'Firmware Management' ComTask is not displayed.
     */
    public PagedInfoList getAllowedComTasksWhichAreNotDefinedYetFor(long deviceTypeId, long deviceConfigurationId, JsonQueryParameters queryParameters, UriInfo uriInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);
        List<ComTaskEnablementInfo.ComTaskInfo> deviceConfigurationComTaskInfos = getAllowedComTaskInfos(deviceType, deviceConfiguration);
        return PagedInfoList.fromPagedList("data", deviceConfigurationComTaskInfos, queryParameters);
    }

    private List<ComTaskEnablementInfo.ComTaskInfo> getAllowedComTaskInfos(DeviceType deviceType, DeviceConfiguration deviceConfiguration) {
        List<ComTask> allowedComTasks = taskService.findAllComTasks().stream()
                .filter(comTask ->
                        comTaskIsNotAlreadyDefinedOnDeviceConfig(deviceConfiguration.getComTaskEnablements(), comTask) // filter all which are not enabled yet
                                && comTaskIsAllowedOnDeviceType(comTask, deviceType))   // filter FirmwareTask if DeviceType doesn't allow
                .collect(Collectors.toList());

        return ComTaskEnablementInfo.ComTaskInfo.from(ListPager.of(allowedComTasks, new ComTaskComparator()).find());
    }

    private boolean comTaskIsNotAlreadyDefinedOnDeviceConfig(List<ComTaskEnablement> deviceConfigurationComTaskEnablements, ComTask comTask) {
        return !deviceConfigurationComTaskEnablements.stream().filter(comTaskEnablement -> comTaskEnablement.getComTask().getId() == comTask.getId()).findAny().isPresent();
    }

    /**
     * Will only check if the DeviceType allows the firmwareUpgrade task and if the given ComTask is a firmwareUpgradeTask
     */
    private boolean comTaskIsAllowedOnDeviceType(ComTask comTask, DeviceType deviceType) {
        return deviceTypeAllowsFirmwareManagement(deviceType) || !isFirmwareManagementComTask(comTask);
    }

    private boolean isFirmwareManagementComTask(ComTask comTask) {
        return taskService.findFirmwareComTask().map(firmwareComTask -> firmwareComTask.getId() == comTask.getId()).orElse(false);
    }

    private boolean deviceTypeAllowsFirmwareManagement(DeviceType deviceType) {
        return this.firmwareService.findFirmwareManagementOptions(deviceType).isPresent();
    }

    private void setComTaskEnablementActive(ComTaskEnablement comTaskEnablement, boolean active) {
        if (active) {
            if(comTaskEnablement.isSuspended()) {
                comTaskEnablement.resume();
            }
        } else {
            if(!comTaskEnablement.isSuspended()) {
                comTaskEnablement.suspend();
            }
        }
    }

    private ComTask findComTaskOrThrowException(long comTaskId) {
        return this.taskService
                .findComTask(comTaskId)
                .orElseThrow(() -> new WebApplicationException("No such communication task", Response.status(Response.Status.NOT_FOUND).entity("No such communication task").build()));
    }
}