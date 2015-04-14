package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.FirmwareComTaskExecution;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.Set;

@Path("/device/{mrid}/firmwaremessages/")
public class DeviceFirmwareMessagesResource {
    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final FirmwareService firmwareService;
    private final FirmwareMessageInfoFactory firmwareMessageInfoFactory;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final TaskService taskService;

    @Inject
    public DeviceFirmwareMessagesResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, FirmwareService firmwareService, FirmwareMessageInfoFactory firmwareMessageInfoFactory, DeviceMessageSpecificationService deviceMessageSpecificationService, TaskService taskService) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.firmwareService = firmwareService;
        this.firmwareMessageInfoFactory = firmwareMessageInfoFactory;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.taskService = taskService;
    }

    @GET
    @Path("/specs/{uploadOption}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.VIEW_DEVICE})
    public Response getMessageAttributes(@PathParam("mrid") String mrid, @PathParam("uploadOption") String uploadOption){
        Device device = resourceHelper.findDeviceByMridOrThrowException(mrid);
        DeviceMessageId firmwareMessageId = getFirmwareUpgradeMessageId(device, uploadOption);
        DeviceMessageSpec firmwareMessageSpec = deviceMessageSpecificationService.findMessageSpecById(firmwareMessageId != null ? firmwareMessageId.dbValue() : 0)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.SUPPORTED_FIRMWARE_UPGRADE_OPTIONS_NOT_FOUND));
        return Response.ok(firmwareMessageInfoFactory.from(firmwareMessageSpec, device, uploadOption)).build();
    }

    private DeviceMessageId getFirmwareUpgradeMessageId(Device device, @PathParam("uploadOption") String uploadOption) {
        ProtocolSupportedFirmwareOptions targetFirmwareOptions = ProtocolSupportedFirmwareOptions.from(uploadOption)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.SUPPORTED_FIRMWARE_UPGRADE_OPTIONS_NOT_FOUND));
        Set<DeviceMessageId> allSupportedMessageIds = device.getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getSupportedMessages();
        DeviceMessageId firmwareMessageId = null;
        for (DeviceMessageId firmwareMessageCandidate : allSupportedMessageIds) {
            Optional<ProtocolSupportedFirmwareOptions> firmwareOptionForCandidate = deviceMessageSpecificationService.getProtocolSupportedFirmwareOptionFor(firmwareMessageCandidate);
            if (firmwareOptionForCandidate.isPresent() && targetFirmwareOptions.equals(firmwareOptionForCandidate.get())){
                firmwareMessageId = firmwareMessageCandidate;
                break;
            }
        }
        return firmwareMessageId;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.OPERATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response uploadFirmwareToDevice(@PathParam("mrid") String mrid, FirmwareMessageInfo info){
        Device device = resourceHelper.findDeviceByMridOrThrowException(mrid);
        if (firmwareService.getAllowedFirmwareUpgradeOptionsFor(device.getDeviceType()).isEmpty()){
            throw exceptionFactory.newException(MessageSeeds.FIRMWARE_UPGRADE_OPTIONS_ARE_DISABLED_FOR_DEVICE_TYPE);
        }
        ComTask firmwareManagementComTask = taskService.findFirmwareComTask()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.DEFAULT_FIRMWARE_MANAGEMENT_TASK_CAN_NOT_BE_FOUND));
        ComTaskEnablement firmwareManagementComTaskEnablement = device.getDeviceConfiguration().getComTaskEnablementFor(firmwareManagementComTask)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.DEFAULT_FIRMWARE_MANAGEMENT_TASK_IS_NOT_ACTIVE));

        Optional<ComTaskExecution> firmwareComTaskExecutionRef = device.getComTaskExecutions().stream()
                .filter(comTaskExecution -> comTaskExecution instanceof FirmwareComTaskExecution)
                .findFirst();
        if (!firmwareComTaskExecutionRef.isPresent()){
            device.newFirmwareComTaskExecution(firmwareManagementComTaskEnablement).add();
        }

        DeviceMessageId firmwareUpgradeMessageId = getFirmwareUpgradeMessageId(device, info.id);
        device.newDeviceMessage(firmwareUpgradeMessageId)
                //TODO Add properties
                .add();
        return Response.ok().build();
    }
}
