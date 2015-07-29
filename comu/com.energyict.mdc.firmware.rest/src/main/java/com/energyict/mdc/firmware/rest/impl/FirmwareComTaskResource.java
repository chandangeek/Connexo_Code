package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Provides logic regarding the FirmwareComTaskExecution
 */
@Path("/devices/{mrid}/comtasks")
public class FirmwareComTaskResource {

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public FirmwareComTaskResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
    }


    @PUT
    @Path("/{comTaskId}/retry")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION})
    public Response retryFirmwareComTask(@PathParam("mrid") String mrid, @PathParam("comTaskId") Long comTaskId) {
        Device device = resourceHelper.findDeviceByMridOrThrowException(mrid);
        ComTaskExecution firmwareComTaskExecution = device.getComTaskExecutions().stream()
                .filter(comTaskExecution -> comTaskExecution.getComTasks().stream()
                        .filter(comTask -> comTask.getId() == comTaskId).findAny()
                        .isPresent())
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.COM_TASK_IS_NOT_ENABLED_FOR_THIS_DEVICE, comTaskId));
        firmwareComTaskExecution.runNow();
        return Response.ok().build();
    }
}
