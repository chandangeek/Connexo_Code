/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.servicecall.DefaultState;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.NoStatusInformationTaskException;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCheck;
import com.energyict.mdc.firmware.FirmwareCheckManagementOptions;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.firmware.BaseFirmwareVersion;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/devices/{name}")
public class DeviceFirmwareMessagesResource {
    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final FirmwareService firmwareService;
    private final FirmwareMessageInfoFactory firmwareMessageInfoFactory;
    private final TaskService taskService;
    private final Clock clock;
    private final MdcPropertyUtils mdcPropertyUtils;
    private final Thesaurus thesaurus;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final DeviceService deviceService;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final DeviceMessageService deviceMessageService;

    @Inject
    public DeviceFirmwareMessagesResource(ResourceHelper resourceHelper,
                                          ExceptionFactory exceptionFactory,
                                          FirmwareService firmwareService,
                                          FirmwareMessageInfoFactory firmwareMessageInfoFactory,
                                          TaskService taskService,
                                          Clock clock,
                                          MdcPropertyUtils mdcPropertyUtils,
                                          Thesaurus thesaurus,
                                          ConcurrentModificationExceptionFactory conflictFactory,
                                          DeviceService deviceService,
                                          DeviceMessageSpecificationService deviceMessageSpecificationService,
                                          DeviceMessageService deviceMessageService) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.firmwareService = firmwareService;
        this.firmwareMessageInfoFactory = firmwareMessageInfoFactory;
        this.taskService = taskService;
        this.clock = clock;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.thesaurus = thesaurus;
        this.conflictFactory = conflictFactory;
        this.deviceService = deviceService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.deviceMessageService = deviceMessageService;
    }

    @GET
    @Transactional
    @Path("/firmwaremessagespecs/{uploadOption}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE})
    public Response getMessageAttributes(@PathParam("name") String name, @PathParam("uploadOption") String uploadOption, @QueryParam("firmwareType") String firmwareType) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        DeviceMessageSpec firmwareMessageSpec = resourceHelper.findFirmwareMessageSpecOrThrowException(device.getDeviceType(), uploadOption);
        return Response.ok(firmwareMessageInfoFactory.from(firmwareMessageSpec, device, uploadOption, firmwareType)).build();
    }

    @POST
    @Transactional
    @Path("/firmwaremessages")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response uploadFirmwareToDevice(@PathParam("name") String name, FirmwareMessageInfo info, @QueryParam("force") boolean force) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        checkFirmwareUpgradeOption(device.getDeviceType(), info.uploadOption);

        Long firmwareVersionId = info.getPropertyInfo(FirmwareMessageInfoFactory.PROPERTY_KEY_FIRMWARE_VERSION)
                .flatMap(resourceHelper::getPropertyInfoValueLong)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.FIRMWARE_VERSION_MISSING));

        FirmwareVersion firmwareVersion = resourceHelper.findFirmwareVersionByIdOrThrowException(firmwareVersionId);
        DeviceMessageId firmwareMessageId = resourceHelper.findFirmwareMessageIdOrThrowException(device.getDeviceType(), info.uploadOption, firmwareVersion);
        DeviceMessageSpec firmwareMessageSpec = resourceHelper.findFirmwareMessageSpecOrThrowException(firmwareMessageId);
        if (!force) {
            Optional<ConfirmationInfo> confirmationInfoOptional = performFirmwareRankingChecks(device, firmwareVersion);
            if (confirmationInfoOptional.isPresent()) {
                return Response.status(Response.Status.BAD_REQUEST).entity(confirmationInfoOptional.get()).build();
            }
        }
        if (deviceMessageSpecificationService.needsImageIdentifierAtFirmwareUpload(firmwareMessageId) && firmwareVersion.getImageIdentifier() != null) {
            firmwareMessageInfoFactory.initImageIdentifier(info, firmwareVersion.getImageIdentifier());
        }
        firmwareMessageInfoFactory.initResumeProperty(info, false);
        Map<String, Object> convertedProperties = getConvertedProperties(firmwareMessageSpec, info);
        Instant releaseDate = info.releaseDate == null ? clock.instant() : info.releaseDate;

        prepareCommunicationTask(device, convertedProperties, firmwareMessageSpec);
        Device.DeviceMessageBuilder deviceMessageBuilder = device.newDeviceMessage(firmwareMessageId).setReleaseDate(releaseDate);
        for (Map.Entry<String, Object> property : convertedProperties.entrySet()) {
            deviceMessageBuilder.addProperty(property.getKey(), property.getValue());
        }
        deviceMessageBuilder.add();
        rescheduleFirmwareUpgradeTask(device);
        return Response.status(Response.Status.CREATED).build();
    }

    private Optional<ConfirmationInfo> performFirmwareRankingChecks(Device device, FirmwareVersion firmwareVersion) {
        ConfirmationInfo confirmationInfo = new ConfirmationInfo();
        if (firmwareVersion.getFirmwareType() != FirmwareType.CA_CONFIG_IMAGE) {
            FirmwareManagementDeviceUtils utils = firmwareService.getFirmwareManagementDeviceUtilsFor(device);
            FirmwareCheckManagementOptions checkOptions = firmwareService.findFirmwareManagementOptions(device.getDeviceType())
                    .map(FirmwareCheckManagementOptions.class::cast)
                    .orElse(FirmwareCheckManagementOptions.EMPTY);
            firmwareService.getFirmwareChecks().forEach(check -> {
                try {
                    check.execute(checkOptions, utils, firmwareVersion);
                } catch (FirmwareCheck.FirmwareCheckException e) {
                    confirmationInfo.errors.add(new ErrorInfo(check.getKey(), check.getTitle(thesaurus), e.getLocalizedMessage()));
                }
            });
        }
        return Optional.of(confirmationInfo)
                .filter(confirmation -> !confirmation.errors.isEmpty());
    }

    private static class ConfirmationInfo {
        public final boolean confirmation = true;
        public final boolean success = false;
        public List<ErrorInfo> errors = new ArrayList<>();
    }

    private static class ErrorInfo {
        public String id;
        public String title;
        public String msg;

        private ErrorInfo(String id, String title, String msg) {
            this.id = id;
            this.title = title;
            this.msg = msg;
        }
    }

    @PUT
    @Transactional
    @Path("/firmwaremessages/{messageId}/activate")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response activateFirmwareOnDevice(@PathParam("name") String name, @PathParam("messageId") Long messageId, FirmwareMessageInfo info) {
        Device device = resourceHelper.getLockedDevice(name, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(name)
                        .withActualVersion(() -> deviceService.findDeviceByName(name).map(Device::getVersion).orElse(null))
                        .supplier());

        DeviceMessage upgradeMessage = device.getMessages().stream()
                .filter(message -> message.getId() == messageId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.FIRMWARE_UPLOAD_NOT_FOUND, messageId));

        FirmwareManagementDeviceUtils versionUtils = this.firmwareService.getFirmwareManagementDeviceUtilsFor(device);
        Optional<ProtocolSupportedFirmwareOptions> protocolSupportedFirmwareOptions = versionUtils.getUploadOptionFromMessage(upgradeMessage);
        if (!protocolSupportedFirmwareOptions.isPresent() || !ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER.equals(protocolSupportedFirmwareOptions.get())) {
            throw exceptionFactory.newException(MessageSeeds.FIRMWARE_CANNOT_BE_ACTIVATED);
        }
        Device.DeviceMessageBuilder deviceMessageBuilder = device.newDeviceMessage(DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE);
        deviceMessageBuilder.setTrackingId(String.valueOf(messageId));
        deviceMessageBuilder.setReleaseDate(info.releaseDate);

        upgradeMessage.getSpecification()
                .getPropertySpecs()
                .stream()
                .filter(propertySpec -> propertySpec.getValueFactory().getValueType().equals(Date.class))
                .findAny()
                .ifPresent(propertySpec -> deviceMessageBuilder.addProperty(propertySpec.getName(), info.releaseDate != null ? Date.from(info.releaseDate) : new Date()));
        try {
            deviceMessageBuilder.add();
            rescheduleFirmwareUpgradeTask(device);
        } catch (VerboseConstraintViolationException e) {
            throw exceptionFactory.newException(MessageSeeds.FIRMWARE_CANNOT_BE_ACTIVATED);
        }
        return Response.ok().build();
    }

    /**
     * This method converts the incoming info object to property-value map (according to the specified message id)
     */
    private Map<String, Object> getConvertedProperties(DeviceMessageSpec firmwareMessageSpec, FirmwareMessageInfo info) {
        Map<String, Object> convertedProperties = new HashMap<>();
        try {
            for (PropertySpec propertySpec : firmwareMessageSpec.getPropertySpecs()) {
                Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, info.getProperties());
                if (propertyValue != null) {
                    convertedProperties.put(propertySpec.getName(), propertyValue);
                }
            }
        } catch (LocalizedFieldValidationException e) {
            throw new LocalizedFieldValidationException(e.getMessageSeed(), "properties." + e.getViolatingProperty());
        }
        return convertedProperties;
    }

    private void prepareCommunicationTask(Device device, Map<String, Object> convertedProperties, DeviceMessageSpec firmwareMessageSpec) {
        FirmwareManagementDeviceUtils helper = this.firmwareService.getFirmwareManagementDeviceUtilsFor(device);
        Optional<ComTaskExecution> fuComTaskExecutionRef = helper.lockFirmwareComTaskExecution();
        if (!fuComTaskExecutionRef.isPresent()) {
            createFirmwareComTaskExecution(device);
        } else {
            cancelOldFirmwareUpdates(helper, convertedProperties, firmwareMessageSpec);
        }
    }

    private void cancelOldFirmwareUpdates(FirmwareManagementDeviceUtils helper, Map<String, Object> convertedProperties, DeviceMessageSpec firmwareMessageSpec) {
        Optional<PropertySpec> firmwareVersionPropertySpec = firmwareMessageSpec.getPropertySpecs()
                .stream()
                .filter(propertySpec -> propertySpec.getValueFactory().getValueType().equals(BaseFirmwareVersion.class))
                .findAny();
        if (firmwareVersionPropertySpec.isPresent()) {
            FirmwareVersion requestedFirmwareVersion = (FirmwareVersion) convertedProperties.get(firmwareVersionPropertySpec.get().getName());
            if (requestedFirmwareVersion != null) {
                helper.cancelPendingFirmwareUpdates(requestedFirmwareVersion.getFirmwareType());
            }
        }
    }

    @DELETE
    @Transactional
    @Path("/firmwaremessages/{msgId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response cancelFirmwareUpload(@PathParam("name") String name, @PathParam("msgId") long msgId, DeviceFirmwareVersionInfos info) {
        Device device = resourceHelper.getLockedDevice(name, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(name)
                        .withActualVersion(() -> deviceService.findDeviceByName(name).map(Device::getVersion).orElse(null))
                        .supplier());
        DeviceMessage upgradeMessage = deviceMessageService.findAndLockDeviceMessageById(msgId)
                .filter(deviceMessage -> deviceMessage.getDevice() instanceof Device && ((Device) deviceMessage.getDevice()).getId() == device.getId())
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.FIRMWARE_UPLOAD_NOT_FOUND, msgId));
        if (!upgradeMessage.getStatus().isPredecessorOf(DeviceMessageStatus.CANCELED)) {
            throw conflictFactory.contextDependentConflictOn(upgradeMessage.getSpecification().getName())
                    .withActualVersion(upgradeMessage::getVersion).build();
        }

        firmwareService.getFirmwareCampaignService().findActiveFirmwareItemByDevice(device)
                .map(DeviceInFirmwareCampaign::getServiceCall)
                .ifPresent(serviceCall -> {
                    if (serviceCall.canTransitionTo(DefaultState.CANCELLED)) {
                        serviceCall.transitionWithLockIfPossible(DefaultState.CANCELLED);
                    }
                });
        FirmwareManagementDeviceUtils firmwareManagementDeviceUtils = this.firmwareService.getFirmwareManagementDeviceUtilsFor(device);
        if (upgradeMessage.getStatus() != DeviceMessageStatus.WAITING && firmwareManagementDeviceUtils.isFirmwareUploadTaskBusy()) {
            throw exceptionFactory.newException(MessageSeeds.FIRMWARE_UPLOAD_HAS_BEEN_STARTED_CANNOT_BE_CANCELED);
        }
        upgradeMessage.revoke();
        // if we have the pending message that means we need to reschedule comTaskExecution for firmware upgrade
        rescheduleFirmwareUpgradeTaskAfterCancellingMessage(device, upgradeMessage);
        return Response.ok().build();
    }

    @GET
    @Transactional
    @Path("/firmwaresactions")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE})
    public Response getDynamicActions(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<DeviceFirmwareActionInfo> deviceFirmwareActions = new ArrayList<>();
        FirmwareManagementDeviceUtils helper = this.firmwareService.getFirmwareManagementDeviceUtilsFor(device);
        if (isDeviceFirmwareUpgradeAllowed(helper)) {
            deviceFirmwareActions = firmwareService.getAllowedFirmwareManagementOptionsFor(device.getDeviceType())
                    .stream()
                    .map(option -> new DeviceFirmwareActionInfo(option.getId(), thesaurus.getString(option.getId(), option.getId())))
                    .collect(Collectors.toList());
        }

        Optional<ScheduledConnectionTask> defaultConnectionTask = device.getScheduledConnectionTasks()
                .stream()
                .filter(ConnectionTask::isDefault)
                .findFirst();
        if (defaultConnectionTask.isPresent() && ConnectionStrategy.MINIMIZE_CONNECTIONS.equals(defaultConnectionTask.get().getConnectionStrategy())) {
            String runActionTitle = thesaurus.getFormat(MessageSeeds.FIRMWARE_ACTION_CHECK_VERSION).format();
            DeviceFirmwareActionInfo info = new DeviceFirmwareActionInfo("run", runActionTitle);
            info.version = device.getVersion();
            deviceFirmwareActions.add(info);
        }
        String runNowActionTitle = thesaurus.getFormat(TranslationKeys.FIRMWARE_ACTION_CHECK_VERSION_NOW_TRANSLATION_KEY).format();
        DeviceFirmwareActionInfo info = new DeviceFirmwareActionInfo("runnow", runNowActionTitle);
        info.version = device.getVersion();
        deviceFirmwareActions.add(info);
        return Response.ok(PagedInfoList.fromPagedList("firmwareactions", deviceFirmwareActions, queryParameters)).build();
    }

    @PUT
    @Transactional
    @Path("/status/run")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE})
    public Response runFirmwareVersionCheck(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters, DeviceFirmwareActionInfo info) {
        String actionName = thesaurus.getFormat(MessageSeeds.FIRMWARE_ACTION_CHECK_VERSION).format();
        Device device = resourceHelper.getLockedDevice(name, info.version)
                .orElseThrow(conflictFactory.conflict()
                        .withActualVersion(() -> deviceService.findDeviceByName(name).map(Device::getVersion).orElse(null))
                        .withMessageTitle(MessageSeeds.FIRMWARE_CHECK_TASK_CONCURRENT_FAIL_TITLE, actionName)
                        .withMessageBody(MessageSeeds.FIRMWARE_CHECK_TASK_CONCURRENT_FAIL_BODY, actionName)
                        .supplier());
        device.runStatusInformationTask(ComTaskExecution::scheduleNow);
        return Response.ok().build();
    }

    @PUT
    @Transactional
    @Path("/status/runnow")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE})
    public Response runFirmwareVersionCheckNow(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters, DeviceFirmwareActionInfo info) {
        String actionName = thesaurus.getFormat(TranslationKeys.FIRMWARE_ACTION_CHECK_VERSION_NOW_TRANSLATION_KEY).format();
        Device device = resourceHelper.getLockedDevice(name, info.version)
                .orElseThrow(conflictFactory.conflict()
                        .withActualVersion(() -> deviceService.findDeviceByName(name).map(Device::getVersion).orElse(null))
                        .withMessageTitle(MessageSeeds.FIRMWARE_CHECK_TASK_CONCURRENT_FAIL_TITLE, actionName)
                        .withMessageBody(MessageSeeds.FIRMWARE_CHECK_TASK_CONCURRENT_FAIL_BODY, actionName)
                        .supplier());
        try {
            device.runStatusInformationTask(ComTaskExecution::runNow);
        } catch (NoStatusInformationTaskException e) {
            throw exceptionFactory.newException(MessageSeeds.FIRMWARE_CHECK_TASK_IS_NOT_ACTIVE);
        }
        return Response.ok().build();
    }

    private boolean isDeviceFirmwareUpgradeAllowed(FirmwareManagementDeviceUtils helper) {
        Optional<ComTaskExecution> firmwareUpgradeExecution = helper.getFirmwareComTaskExecution();
        return helper.getPendingFirmwareMessages().stream()
                .filter(message -> !firmwareUpgradeExecution.isPresent()
                        || firmwareUpgradeExecution.get().getLastExecutionStartTimestamp() == null
                        || !message.getReleaseDate().isBefore(firmwareUpgradeExecution.get().getLastExecutionStartTimestamp()))
                .count() == 0 && !helper.isFirmwareUploadTaskBusy();
    }

    /**
     * Checks that device type allows the requested firmware upgrade option
     */
    private void checkFirmwareUpgradeOption(DeviceType deviceType, String uploadOption) {
        ProtocolSupportedFirmwareOptions requestedFUOption = resourceHelper.findProtocolSupportedFirmwareOptionsOrThrowException(uploadOption);
        Set<ProtocolSupportedFirmwareOptions> deviceTypeFUAllowedOptions = firmwareService.getAllowedFirmwareManagementOptionsFor(deviceType);
        if (deviceTypeFUAllowedOptions.isEmpty()) { // firmware upgrade is not allowed for the device type
            throw exceptionFactory.newException(MessageSeeds.FIRMWARE_UPGRADE_OPTIONS_ARE_DISABLED_FOR_DEVICE_TYPE);
        }
        if (!deviceTypeFUAllowedOptions.contains(requestedFUOption)) { // the requested firmware upgrade option is not allowed on device type
            throw exceptionFactory.newException(MessageSeeds.FIRMWARE_UPGRADE_OPTION_ARE_DISABLED_FOR_DEVICE_TYPE);
        }
    }

    private ComTaskExecution createFirmwareComTaskExecution(Device device) {
        // Check firmware upgrade task
        ComTask comTask = taskService.findFirmwareComTask()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.DEFAULT_FIRMWARE_MANAGEMENT_TASK_CAN_NOT_BE_FOUND));
        ComTaskEnablement comTaskEnablement = device.getDeviceConfiguration().getComTaskEnablementFor(comTask)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.DEFAULT_FIRMWARE_MANAGEMENT_TASK_IS_NOT_ACTIVE));
        ComTaskExecution firmwareComTaskExecution = device.newFirmwareComTaskExecution(comTaskEnablement).add();
        device.save();
        return firmwareComTaskExecution;
    }

    private void rescheduleFirmwareUpgradeTaskAfterCancellingMessage(Device device, DeviceMessage updateMessage) {
        FirmwareManagementDeviceUtils helper = this.firmwareService.getFirmwareManagementDeviceUtilsFor(device);
        Optional<Instant> earliestReleaseDate = helper.getPendingFirmwareMessages().stream()
                .filter(deviceMessage -> deviceMessage.getId() != updateMessage.getId())
                .map(DeviceMessage::getReleaseDate)
                .min(Instant::compareTo);
        rescheduleFirmwareUpgradeTask(helper, earliestReleaseDate.orElse(null));
    }


    private void rescheduleFirmwareUpgradeTask(Device device) {
        FirmwareManagementDeviceUtils helper = this.firmwareService.getFirmwareManagementDeviceUtilsFor(device);
        Optional<Instant> earliestReleaseDate = helper.getPendingFirmwareMessages().stream()
                .map(DeviceMessage::getReleaseDate)
                .sorted(Instant::compareTo)
                .findFirst();
        rescheduleFirmwareUpgradeTask(helper, earliestReleaseDate.orElse(null));
    }

    private void rescheduleFirmwareUpgradeTask(FirmwareManagementDeviceUtils helper, Instant earliestReleaseDate) {
        Optional<ComTaskExecution> firmwareComTaskExecution = helper.lockFirmwareComTaskExecution();
        firmwareComTaskExecution.ifPresent(comTaskExecution -> {
            if (comTaskExecution.getNextExecutionTimestamp() == null || earliestReleaseDate == null || comTaskExecution.getNextExecutionTimestamp().isAfter(earliestReleaseDate)) {
                comTaskExecution.schedule(earliestReleaseDate);
            }
        });
    }
}
