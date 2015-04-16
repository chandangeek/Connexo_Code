package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.Counter;
import com.elster.jupiter.util.Counters;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.FirmwareComTaskExecution;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Path("/device/{mrid}")
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
    @Path("/firmwaremessagespecs/{uploadOption}")
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
    @Path("/firmwaremessages")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.OPERATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response uploadFirmwareToDevice(@PathParam("mrid") String mrid, FirmwareMessageInfo info){
        Device device = resourceHelper.findDeviceByMridOrThrowException(mrid);
        checkFirmwareUpgradeOption(device.getDeviceType(), info.id);
        ComTaskEnablement comTaskEnablement = checkFUComTaskEnablement(device.getDeviceConfiguration());

        DeviceMessageId fuMessageId = getFirmwareUpgradeMessageId(device, info.id);
        Map<String, Object> convertedProperties = getConvertedProperties(fuMessageId, info);
        Instant releaseDate = info.releaseDate == null ? this.clock.instant() : Instant.ofEpochMilli(info.releaseDate);

        prepareCommunicationTask(device, comTaskEnablement, releaseDate, convertedProperties);
        Device.DeviceMessageBuilder deviceMessageBuilder = device.newDeviceMessage(fuMessageId).setReleaseDate(releaseDate);
        for (Map.Entry<String, Object> property : convertedProperties.entrySet()) {
            deviceMessageBuilder.addProperty(property.getKey(), property.getValue());
        }
        deviceMessageBuilder.add();
        return Response.ok().build();
    }

    /** Checks that device type allows the requested firmware upgrade option */
    private void checkFirmwareUpgradeOption(DeviceType deviceType, String uploadOption) {
        ProtocolSupportedFirmwareOptions requestedFUOption = ProtocolSupportedFirmwareOptions.from(uploadOption).orElse(null);
        Set<ProtocolSupportedFirmwareOptions> deviceTypeFUAllowedOptions = firmwareService.getAllowedFirmwareUpgradeOptionsFor(deviceType);
        if (deviceTypeFUAllowedOptions.isEmpty()){ // firmware upgrade is not allowed for the device type
            throw exceptionFactory.newException(MessageSeeds.FIRMWARE_UPGRADE_OPTIONS_ARE_DISABLED_FOR_DEVICE_TYPE);
        }
        if (!deviceTypeFUAllowedOptions.contains(requestedFUOption)){ // the requested firmware upgrade option is not allowed on device type
            throw exceptionFactory.newException(MessageSeeds.FIRMWARE_UPGRADE_OPTION_ARE_DISABLED_FOR_DEVICE_TYPE);
        }
    }

    /** Checks that device configuration has firmware upgrade support */
    private ComTaskEnablement checkFUComTaskEnablement(DeviceConfiguration deviceConfiguration) {
        // Check firmware upgrade task
        ComTask fuComTask = taskService.findFirmwareComTask()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.DEFAULT_FIRMWARE_MANAGEMENT_TASK_CAN_NOT_BE_FOUND));
        return deviceConfiguration.getComTaskEnablementFor(fuComTask)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.DEFAULT_FIRMWARE_MANAGEMENT_TASK_IS_NOT_ACTIVE));
    }

    /** This method converts the incoming info object to property-value map (according to the specified message id) */
    private Map<String, Object> getConvertedProperties(DeviceMessageId messageId, FirmwareMessageInfo info) {
        DeviceMessageSpec fuMessageSpec = deviceMessageSpecificationService.findMessageSpecById(messageId.dbValue())
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.SUPPORTED_FIRMWARE_UPGRADE_OPTIONS_NOT_FOUND));
        Map<String, Object> convertedProperties = new HashMap<>();
        try {
            for (PropertySpec propertySpec : fuMessageSpec.getPropertySpecs()) {
                Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, info.properties);
                if (propertyValue != null) {
                    convertedProperties.put(propertySpec.getName(), propertyValue);
                }
            }
        } catch (LocalizedFieldValidationException e) {
            throw new LocalizedFieldValidationException(e.getMessageSeed(), "properties."+e.getViolatingProperty());
        }
        return convertedProperties;
    }

    private void prepareCommunicationTask(Device device, ComTaskEnablement comTaskEnablement, Instant releaseDate, Map<String, Object> convertedProperties) {
        // Check firmware com task execution for current device
        Optional<ComTaskExecution> fuComTaskExecutionRef = device.getComTaskExecutions().stream()
                .filter(comTaskExecution -> comTaskExecution instanceof FirmwareComTaskExecution)
                .findFirst();
        FirmwareComTaskExecution fuComTaskExecution = null;
        if (!fuComTaskExecutionRef.isPresent()){
            // Create a new comTaskExecution
            fuComTaskExecution = device.newFirmwareComTaskExecution(comTaskEnablement).add();
            device.save();
        } else {
            // Cancel the previous firmware upgrades for the same firmware type
            fuComTaskExecution = (FirmwareComTaskExecution) fuComTaskExecutionRef.get();
            cancelOldFUMessages(device, convertedProperties, fuComTaskExecution);
        }
        if (fuComTaskExecution.getNextExecutionTimestamp() == null ||
                fuComTaskExecution.getNextExecutionTimestamp().isAfter(releaseDate)){
            fuComTaskExecution.schedule(releaseDate);
        }
    }

    private void cancelOldFUMessages(Device device, Map<String, Object> convertedProperties, FirmwareComTaskExecution comTaskExecution) {
        String firmwareVersionPropertyName = DeviceMessageConstants.firmwareUpdateFileAttributeName;
        FirmwareVersion requestedFirmwareVersion = (FirmwareVersion) convertedProperties.get(firmwareVersionPropertyName);
        if (requestedFirmwareVersion != null) {
            int fuMessageCategoryId = deviceMessageSpecificationService.getFirmwareCategory().getId();
            Counter pendingMsgCounter = Counters.newStrictCounter();
            device.getMessages().stream()
                    .filter(message -> message.getStatus().equals(DeviceMessageStatus.PENDING) || message.getStatus().equals(DeviceMessageStatus.WAITING)) // only pending messages
                    .filter(message -> message.getSpecification().getCategory().getId() == fuMessageCategoryId) // only pending firmware upgrade messages
                    .forEach(candidate -> {
                        Optional<DeviceMessageAttribute> candidateFirmwareVersion = candidate.getAttributes().stream().filter(attr -> firmwareVersionPropertyName.equals(attr.getName())).findFirst();
                        if (candidateFirmwareVersion.isPresent() && requestedFirmwareVersion.getFirmwareType().equals(((FirmwareVersion) candidateFirmwareVersion.get().getValue()).getFirmwareType())) {
                            candidate.revoke();
                            candidate.save();
                        } else {
                            pendingMsgCounter.increment();
                        }
                    });
            if (pendingMsgCounter.getValue() == 0) {
                comTaskExecution.putOnHold();
            }
        }
    }
}
