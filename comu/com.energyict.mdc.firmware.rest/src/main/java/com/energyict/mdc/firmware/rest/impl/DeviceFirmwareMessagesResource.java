package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.FirmwareComTaskExecution;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
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
import java.time.Clock;
import java.time.Instant;
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
    private final Clock clock;
    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public DeviceFirmwareMessagesResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, FirmwareService firmwareService, FirmwareMessageInfoFactory firmwareMessageInfoFactory, DeviceMessageSpecificationService deviceMessageSpecificationService, TaskService taskService, Clock clock, MdcPropertyUtils mdcPropertyUtils) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.firmwareService = firmwareService;
        this.firmwareMessageInfoFactory = firmwareMessageInfoFactory;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.taskService = taskService;
        this.clock = clock;
        this.mdcPropertyUtils = mdcPropertyUtils;
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

    /**
     * Returns the appropriate DeviceMessageId which corresponds with the uploadOption
     */
    private DeviceMessageId getFirmwareUpgradeMessageId(Device device, String uploadOption) {
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
        Instant releaseDate = info.releaseDate == null ? this.clock.instant() : Instant.ofEpochMilli(info.releaseDate);
        ProtocolSupportedFirmwareOptions requestedFUOption = ProtocolSupportedFirmwareOptions.from(info.id).orElse(null);
        Set<ProtocolSupportedFirmwareOptions> deviceTypeFUAllowedOptions = firmwareService.getAllowedFirmwareUpgradeOptionsFor(device.getDeviceType());

        // Check allowed firmware upgrade options for device type
        if (deviceTypeFUAllowedOptions.isEmpty()){ // firmware upgrade is not allowed for the device type
            throw exceptionFactory.newException(MessageSeeds.FIRMWARE_UPGRADE_OPTIONS_ARE_DISABLED_FOR_DEVICE_TYPE);
        }
        if (!deviceTypeFUAllowedOptions.contains(requestedFUOption)){ // the requested firmware upgrade option is not allowed on device type
            throw exceptionFactory.newException(MessageSeeds.FIRMWARE_UPGRADE_OPTION_ARE_DISABLED_FOR_DEVICE_TYPE);
        }

        // Check firmware upgrade task
        ComTask fuComTask = taskService.findFirmwareComTask()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.DEFAULT_FIRMWARE_MANAGEMENT_TASK_CAN_NOT_BE_FOUND));
        ComTaskEnablement fuComTaskEnablement = device.getDeviceConfiguration().getComTaskEnablementFor(fuComTask)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.DEFAULT_FIRMWARE_MANAGEMENT_TASK_IS_NOT_ACTIVE));

        // Check firmware com task execution for current device
        Optional<ComTaskExecution> fuComTaskExecutionRef = device.getComTaskExecutions().stream()
                .filter(comTaskExecution -> comTaskExecution.getComTasks().stream().filter(comTask -> comTask.getId() == fuComTask.getId()).count() > 0)
                .findFirst();
        FirmwareComTaskExecution fuComTaskExecution = null;
        if (!fuComTaskExecutionRef.isPresent()){
            fuComTaskExecution = device.newFirmwareComTaskExecution(fuComTaskEnablement).add();
            device.save();
        } else {
            // TODO cancel currently pending messages of the corresponding firmware type
            /*
            Cancelling a firmwareUpgrade can only be done when there is a pending FirmwareUpgradeComTaskExecution AND a
            pending FirmwareUpgrade message. Once the firmware has been uploaded to the Device, then it is not possible
            to cancel the upgrade anymore. The only 'workaround' is to upload a new Firmware object to the device (this
            means starting a new Firmware upload process)
            The cancel logic should first cancel the FirmwareUpgrade message, then check if there are other pending
            FirmwareUpgrade related messages, if not, then cancel the FirmwareUpgradeComTaskExecution, if there are
            other pending messages, then DON'T cancel the FirmwareUpgradeComTaskExecution
            */
            fuComTaskExecution = (FirmwareComTaskExecution) fuComTaskExecutionRef.get();

        }
        if (fuComTaskExecution.getNextExecutionTimestamp() == null ||
                fuComTaskExecution.getNextExecutionTimestamp().isAfter(releaseDate)){
            fuComTaskExecution.schedule(releaseDate);
        }

        // Create a corresponding device message
        DeviceMessageId fuMessageId = getFirmwareUpgradeMessageId(device, info.id);
        Device.DeviceMessageBuilder deviceMessageBuilder = device.newDeviceMessage(fuMessageId).setReleaseDate(releaseDate);
        DeviceMessageSpec fuMessageSpec = deviceMessageSpecificationService.findMessageSpecById(fuMessageId.dbValue())
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.SUPPORTED_FIRMWARE_UPGRADE_OPTIONS_NOT_FOUND));
        try {
            for (PropertySpec propertySpec : fuMessageSpec.getPropertySpecs()) {
                Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, info.properties);
                if (propertyValue != null) {
                    deviceMessageBuilder.addProperty(propertySpec.getName(), propertyValue);
                }
            }
        } catch (LocalizedFieldValidationException e) {
            throw new LocalizedFieldValidationException(e.getMessageSeed(), "properties."+e.getViolatingProperty());
        }
        deviceMessageBuilder.add();
        return Response.ok().build();
    }
}
