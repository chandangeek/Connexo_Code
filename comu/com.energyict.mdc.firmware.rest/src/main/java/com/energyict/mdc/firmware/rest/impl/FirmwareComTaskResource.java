/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.firmware.FirmwareService;

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
    private final FirmwareService firmwareService;
    private final CommunicationTaskService communicationTaskService;
    private final ConnectionTaskService connectionTaskService;

    @Inject
    public FirmwareComTaskResource(ResourceHelper resourceHelper,
                                   ExceptionFactory exceptionFactory,
                                   ConcurrentModificationExceptionFactory conflictFactory,
                                   Thesaurus thesaurus,
                                   DeviceService deviceService,
                                   FirmwareService firmwareService,
                                   CommunicationTaskService communicationTaskService,
                                   ConnectionTaskService connectionTaskService) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.conflictFactory = conflictFactory;
        this.thesaurus = thesaurus;
        this.deviceService = deviceService;
        this.firmwareService = firmwareService;
        this.communicationTaskService = communicationTaskService;
        this.connectionTaskService = connectionTaskService;
    }


    @PUT
    @Transactional
    @Path("/{comTaskId}/retry")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION})
    public Response retryFirmwareOrVerificationComTask(@PathParam("name") String name, @PathParam("comTaskId") Long comTaskId, DeviceFirmwareActionInfo info) {
        String actionName = thesaurus.getFormat(MessageSeeds.FIRMWARE_COMMUNICATION_TASK_NAME).format();
        Device device = resourceHelper.getLockedDevice(name, info.version)
                .orElseThrow(conflictFactory.conflict()
                        .withActualVersion(() -> deviceService.findDeviceByName(name).map(Device::getVersion).orElse(null))
                        .withMessageTitle(MessageSeeds.FIRMWARE_CHECK_TASK_CONCURRENT_FAIL_TITLE, actionName)
                        .withMessageBody(MessageSeeds.FIRMWARE_CHECK_TASK_CONCURRENT_FAIL_BODY, actionName)
                        .supplier());

        firmwareService.resumeFirmwareUploadForDevice(device);

        ComTaskExecution comTaskExecution = device.getComTaskExecutions().stream()
                .filter(cte -> cte.getComTask().getId() == comTaskId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.COM_TASK_IS_NOT_ENABLED_FOR_THIS_DEVICE, comTaskId));
        connectionTaskService.findAndLockConnectionTaskById(firmwareComTaskExecution.getConnectionTaskId());
        communicationTaskService.findAndLockComTaskExecutionById(firmwareComTaskExecution.getId()).ifPresent(ComTaskExecution::runNow);
        String taskRetried = comTaskExecution.isFirmware()
                ? thesaurus.getSimpleFormat(MessageSeeds.FIRMWARE_UPLOAD_RETRIED).format()
                : thesaurus.getSimpleFormat(MessageSeeds.VERIFICATION_RETRIED).format();
        return Response.ok(taskRetried).build();
    }
}
