/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Provides logic regarding the FirmwareComTaskExecution.
 */
@Path("/devices/{name}/comtasks")
public class FirmwareComTaskResource {

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final Thesaurus thesaurus;
    private final DeviceService deviceService;

    @Inject
    public FirmwareComTaskResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, ConcurrentModificationExceptionFactory conflictFactory, Thesaurus thesaurus, DeviceService deviceService) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.conflictFactory = conflictFactory;
        this.thesaurus = thesaurus;
        this.deviceService = deviceService;
    }


    @PUT @Transactional
    @Path("/{comTaskId}/retry")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION})
    public Response retryFirmwareComTask(@PathParam("name") String name, @PathParam("comTaskId") Long comTaskId, DeviceFirmwareActionInfo info) {
        String actionName = thesaurus.getFormat(MessageSeeds.FIRMWARE_COMMUNICATION_TASK_NAME).format();
        Device device = resourceHelper.getLockedDevice(name, info.version)
                .orElseThrow(conflictFactory.conflict()
                        .withActualVersion(() -> deviceService.findDeviceByName(name).map(Device::getVersion).orElse(null))
                        .withMessageTitle(MessageSeeds.FIRMWARE_CHECK_TASK_CONCURRENT_FAIL_TITLE, actionName)
                        .withMessageBody(MessageSeeds.FIRMWARE_CHECK_TASK_CONCURRENT_FAIL_BODY, actionName)
                        .supplier());

        ComTaskExecution firmwareComTaskExecution = device.getComTaskExecutions().stream()
                .filter(comTaskExecution -> comTaskExecution.getComTask().getId() == comTaskId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.COM_TASK_IS_NOT_ENABLED_FOR_THIS_DEVICE, comTaskId));
        firmwareComTaskExecution.runNow();
        return Response.ok().build();
    }
}
