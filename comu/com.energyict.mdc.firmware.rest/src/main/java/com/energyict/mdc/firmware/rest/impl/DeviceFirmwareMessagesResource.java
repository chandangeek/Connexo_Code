package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.FirmwareComTaskExecution;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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
    private final Thesaurus thesaurus;
    private final Provider<DeviceFirmwareVersionUtils> utilProvider;

    @Inject
    public DeviceFirmwareMessagesResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, FirmwareService firmwareService, FirmwareMessageInfoFactory firmwareMessageInfoFactory, DeviceMessageSpecificationService deviceMessageSpecificationService, TaskService taskService, Clock clock, MdcPropertyUtils mdcPropertyUtils, Thesaurus thesaurus, Provider<DeviceFirmwareVersionUtils> utilProvider) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.firmwareService = firmwareService;
        this.firmwareMessageInfoFactory = firmwareMessageInfoFactory;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.taskService = taskService;
        this.clock = clock;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.thesaurus = thesaurus;
        this.utilProvider = utilProvider;
    }

    @GET
    @Path("/firmwaremessagespecs/{uploadOption}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.VIEW_DEVICE})
    public Response getMessageAttributes(@PathParam("mrid") String mrid, @PathParam("uploadOption") String uploadOption){
        Device device = resourceHelper.findDeviceByMridOrThrowException(mrid);
        DeviceMessageId firmwareMessageId = getFirmwareUpgradeMessageId(device, uploadOption)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.SUPPORTED_FIRMWARE_UPGRADE_OPTIONS_NOT_FOUND));
        DeviceMessageSpec firmwareMessageSpec = deviceMessageSpecificationService.findMessageSpecById(firmwareMessageId.dbValue())
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.SUPPORTED_FIRMWARE_UPGRADE_OPTIONS_NOT_FOUND));
        return Response.ok(firmwareMessageInfoFactory.from(firmwareMessageSpec, device, uploadOption)).build();
    }

    @POST
    @Path("/firmwaremessages")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.OPERATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response uploadFirmwareToDevice(@PathParam("mrid") String mrid, FirmwareMessageInfo info){
        Device device = resourceHelper.findDeviceByMridOrThrowException(mrid);
        checkFirmwareUpgradeOption(device.getDeviceType(), info.uploadOption);

        DeviceMessageId firmwareMessageId = getFirmwareUpgradeMessageId(device, info.uploadOption)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.SUPPORTED_FIRMWARE_UPGRADE_OPTIONS_NOT_FOUND));
        Map<String, Object> convertedProperties = getConvertedProperties(firmwareMessageId, info);
        Instant releaseDate = info.releaseDate == null ? this.clock.instant() : Instant.ofEpochMilli(info.releaseDate);

        prepareCommunicationTask(device, convertedProperties);
        Device.DeviceMessageBuilder deviceMessageBuilder = device.newDeviceMessage(firmwareMessageId).setReleaseDate(releaseDate);
        for (Map.Entry<String, Object> property : convertedProperties.entrySet()) {
            deviceMessageBuilder.addProperty(property.getKey(), property.getValue());
        }
        deviceMessageBuilder.add();
        rescheduleFirmwareUpgradeTask(device);
        return Response.ok().build();
    }

    @PUT
    @Path("/firmwaremessages/{messageId}/activate")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.OPERATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response activateFirmwareOnDevice(@PathParam("mrid") String mrid, @PathParam("messageId") Long messageId) {
        Device device = resourceHelper.findDeviceByMridOrThrowException(mrid);

        DeviceMessage<Device> upgradeMessage = device.getMessages().stream()
                .filter(message -> message.getId() == messageId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.FIRMWARE_UPLOAD_NOT_FOUND, messageId));


        DeviceFirmwareVersionUtils firmwareVersionUtils = new DeviceFirmwareVersionUtils(thesaurus, deviceMessageSpecificationService).onDevice(device);
        Optional<ProtocolSupportedFirmwareOptions> protocolSupportedFirmwareOptions = firmwareVersionUtils.getUploadOptionFromMessage(upgradeMessage);
        if (!protocolSupportedFirmwareOptions.isPresent() || !ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.equals(protocolSupportedFirmwareOptions.get())) {
            exceptionFactory.newExceptionSupplier(MessageSeeds.FIRMWARE_CANNOT_BE_ACTIVATED);
        }
        Device.DeviceMessageBuilder deviceMessageBuilder = device.newDeviceMessage(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE);
        deviceMessageBuilder.addProperty(DeviceMessageConstants.firmwareUpdateActivationDateAttributeName, new Date(Instant.now().toEpochMilli()));
        deviceMessageBuilder.add();
        rescheduleFirmwareUpgradeTask(device);
        return Response.ok().build();
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

    private void
    prepareCommunicationTask(Device device, Map<String, Object> convertedProperties) {
        Optional<ComTaskExecution> fuComTaskExecutionRef = findFirmwareComTaskExecution(device);
        if (!fuComTaskExecutionRef.isPresent()){
            createFirmwareComTaskExecution(device);
        } else {
            cancelOldFirmwareUpgrades(device, convertedProperties);
        }
    }

    private void cancelOldFirmwareUpgrades(Device device, Map<String, Object> convertedProperties) {
        String firmwareVersionPropertyName = DeviceMessageConstants.firmwareUpdateFileAttributeName;
        FirmwareVersion requestedFirmwareVersion = (FirmwareVersion) convertedProperties.get(firmwareVersionPropertyName);
        DeviceFirmwareVersionUtils versionUtils = utilProvider.get().onDevice(device);
        if (requestedFirmwareVersion != null && versionUtils.getComTaskExecution() != null) {
            versionUtils.getFirmwareMessages().stream()
                    .filter(message -> DeviceFirmwareVersionUtils.PENDING_STATUSES.contains(message.getStatus())) // only pending firmware upgrade messages
                    .filter(candidate -> { // only messages which have the same firmware type
                        Optional<FirmwareVersion> candidateFirmwareVersion = versionUtils.getFirmwareVersionFromMessage(candidate);
                        return candidateFirmwareVersion.isPresent() && requestedFirmwareVersion.getFirmwareType().equals(candidateFirmwareVersion.get().getFirmwareType());
                    })
                    .forEach(message -> {
                        message.revoke();
                        message.save();
                    });
        }
    }

    @DELETE
    @Path("/firmwaremessages/{msgId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.OPERATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response cancelFirmwareUpload(@PathParam("mrid") String mrid, @PathParam("msgId") long msgId) {
        Device device = resourceHelper.findDeviceByMridOrThrowException(mrid);
        DeviceMessage<Device> upgradeMessage = device.getMessages().stream()
                .filter(message -> message.getId() == msgId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.FIRMWARE_UPLOAD_NOT_FOUND, msgId));
        if (!DeviceFirmwareVersionUtils.PENDING_STATUSES.contains(upgradeMessage.getStatus())){
            throw exceptionFactory.newException(MessageSeeds.FIRMWARE_UPLOAD_HAS_BEEN_STARTED_CANNOT_BE_CANCELED);
        }
        upgradeMessage.revoke();
        upgradeMessage.save();
        // if we have the pending message that means we need to reschedule comTaskExecution for firmware upgrade
        rescheduleFirmwareUpgradeTask(device);
        return Response.ok().build();
    }

    @GET
    @Path("/firmwaresactions")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.VIEW_DEVICE})
    public Response getDynamicActions(@PathParam("mrid") String mrid, @BeanParam QueryParameters queryParameters){
        Device device = resourceHelper.findDeviceByMridOrThrowException(mrid);
        List<UpgradeOptionInfo> upgradeOptionActions = new ArrayList<>();
        DeviceFirmwareVersionUtils versionUtils = utilProvider.get().onDevice(device);
        boolean hasActiveUpgradeMessages = versionUtils.getFirmwareMessages().stream()
                .filter(message -> DeviceFirmwareVersionUtils.PENDING_STATUSES.contains(message.getStatus()))
                .count() != 0;
        if (!hasActiveUpgradeMessages) {
            upgradeOptionActions = firmwareService.getAllowedFirmwareUpgradeOptionsFor(device.getDeviceType())
                    .stream()
                    .map(option -> new UpgradeOptionInfo(option.getId(), thesaurus.getString(option.getId(), option.getId())))
                    .collect(Collectors.toList());
        }
        // TODO add actions for basic checks here
        return Response.ok(PagedInfoList.fromPagedList("firmwareactions", upgradeOptionActions, queryParameters)).build();
    }

    /** Returns the appropriate DeviceMessageId which corresponds with the uploadOption */
    private Optional<DeviceMessageId> getFirmwareUpgradeMessageId(Device device, String uploadOption) {
        ProtocolSupportedFirmwareOptions targetFirmwareOptions = ProtocolSupportedFirmwareOptions.from(uploadOption)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.SUPPORTED_FIRMWARE_UPGRADE_OPTIONS_NOT_FOUND));
        return device.getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getSupportedMessages()
                .stream()
                .filter(firmwareMessageCandidate -> {
                    Optional<ProtocolSupportedFirmwareOptions> firmwareOptionForCandidate = deviceMessageSpecificationService.getProtocolSupportedFirmwareOptionFor(firmwareMessageCandidate);
                    return firmwareOptionForCandidate.isPresent() && targetFirmwareOptions.equals(firmwareOptionForCandidate.get());
                })
                .findFirst();
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

    private Optional<ComTaskExecution> findFirmwareComTaskExecution(Device device) {
        return device.getComTaskExecutions().stream()
                .filter(comTaskExecution -> comTaskExecution instanceof FirmwareComTaskExecution)
                .findFirst();
    }

    private FirmwareComTaskExecution createFirmwareComTaskExecution(Device device) {
        // Check firmware upgrade task
        ComTask comTask = taskService.findFirmwareComTask()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.DEFAULT_FIRMWARE_MANAGEMENT_TASK_CAN_NOT_BE_FOUND));
        ComTaskEnablement comTaskEnablement = device.getDeviceConfiguration().getComTaskEnablementFor(comTask)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.DEFAULT_FIRMWARE_MANAGEMENT_TASK_IS_NOT_ACTIVE));
        FirmwareComTaskExecution firmwareComTaskExecution = device.newFirmwareComTaskExecution(comTaskEnablement).add();
        device.save();
        return firmwareComTaskExecution;
    }

    private void rescheduleFirmwareUpgradeTask(Device device) {
        DeviceFirmwareVersionUtils versionUtils = utilProvider.get().onDevice(device);
        Optional<Instant> earliestReleaseDate = versionUtils.getFirmwareMessages().stream()
                .filter(message -> DeviceFirmwareVersionUtils.PENDING_STATUSES.contains(message.getStatus()))
                .map(DeviceMessage::getReleaseDate)
                .sorted(Instant::compareTo)
                .findFirst();
        rescheduleFirmwareUpgradeTask(device, earliestReleaseDate.orElse(null));
    }

    private void rescheduleFirmwareUpgradeTask(Device device, Instant earliestReleaseDate){
        Optional<ComTaskExecution> firmwareComTaskExecution = findFirmwareComTaskExecution(device);
        firmwareComTaskExecution.ifPresent(comTaskExecution -> {
            if (earliestReleaseDate != null) {
                if (comTaskExecution.getNextExecutionTimestamp() == null ||
                        comTaskExecution.getNextExecutionTimestamp().isAfter(earliestReleaseDate)) {
                    comTaskExecution.schedule(earliestReleaseDate);
                }
            } else {
                comTaskExecution.putOnHold();
            }
        });
    }
}
