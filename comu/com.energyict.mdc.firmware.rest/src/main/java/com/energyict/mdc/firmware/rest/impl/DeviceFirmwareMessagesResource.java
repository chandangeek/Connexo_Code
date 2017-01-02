package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.NoStatusInformationTaskException;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.FirmwareComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;
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

/**
 * TODO replace by /devices
 */
@Path("/device/{mrid}")
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

    @Inject
    public DeviceFirmwareMessagesResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, FirmwareService firmwareService, FirmwareMessageInfoFactory firmwareMessageInfoFactory, TaskService taskService, Clock clock, MdcPropertyUtils mdcPropertyUtils, Thesaurus thesaurus, ConcurrentModificationExceptionFactory conflictFactory, DeviceService deviceService) {
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
    }

    @GET @Transactional
    @Path("/firmwaremessagespecs/{uploadOption}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE})
    public Response getMessageAttributes(@PathParam("mrid") String mrid, @PathParam("uploadOption") String uploadOption, @QueryParam("firmwareType") String firmwareType) {
        Device device = resourceHelper.findDeviceByMridOrThrowException(mrid);
        DeviceMessageSpec firmwareMessageSpec = resourceHelper.findFirmwareMessageSpecOrThrowException(device.getDeviceType(), uploadOption);
        return Response.ok(firmwareMessageInfoFactory.from(firmwareMessageSpec, device, uploadOption, firmwareType)).build();
    }

    @POST @Transactional
    @Path("/firmwaremessages")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response uploadFirmwareToDevice(@PathParam("mrid") String mrid, FirmwareMessageInfo info) {
        Device device = resourceHelper.findDeviceByMridOrThrowException(mrid);
        checkFirmwareUpgradeOption(device.getDeviceType(), info.uploadOption);

        DeviceMessageId firmwareMessageId = resourceHelper.findFirmwareMessageIdOrThrowException(device.getDeviceType(), info.uploadOption);
        DeviceMessageSpec firmwareMessageSpec = resourceHelper.findFirmwareMessageSpecOrThrowException(firmwareMessageId);
        Map<String, Object> convertedProperties = getConvertedProperties(firmwareMessageSpec, info);
        Instant releaseDate = info.releaseDate == null ? this.clock.instant() : info.releaseDate;

        prepareCommunicationTask(device, convertedProperties);
        Device.DeviceMessageBuilder deviceMessageBuilder = device.newDeviceMessage(firmwareMessageId).setReleaseDate(releaseDate);
        for (Map.Entry<String, Object> property : convertedProperties.entrySet()) {
            deviceMessageBuilder.addProperty(property.getKey(), property.getValue());
        }
        deviceMessageBuilder.add();
        rescheduleFirmwareUpgradeTask(device);
        return Response.status(Response.Status.CREATED).build();
    }


    @PUT @Transactional
    @Path("/firmwaremessages/{messageId}/activate")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response activateFirmwareOnDevice(@PathParam("mrid") String mrid, @PathParam("messageId") Long messageId, FirmwareMessageInfo info) {
        Device device = resourceHelper.getLockedDevice(mrid, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(mrid)
                        .withActualVersion(() -> deviceService.findByUniqueMrid(mrid).map(Device::getVersion).orElse(null))
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
        deviceMessageBuilder.addProperty(DeviceMessageConstants.firmwareUpdateActivationDateAttributeName, info.releaseDate != null ? Date.from(info.releaseDate) : new Date());
        deviceMessageBuilder.add();
        rescheduleFirmwareUpgradeTask(device);
        return Response.ok().build();
    }

    /**
     * This method converts the incoming info object to property-value map (according to the specified message id)
     */
    private Map<String, Object> getConvertedProperties(DeviceMessageSpec firmwareMessageSpec, FirmwareMessageInfo info) {
        Map<String, Object> convertedProperties = new HashMap<>();
        try {
            for (PropertySpec propertySpec : firmwareMessageSpec.getPropertySpecs()) {
                Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, info.properties);
                if (propertyValue != null) {
                    convertedProperties.put(propertySpec.getName(), propertyValue);
                }
            }
        } catch (LocalizedFieldValidationException e) {
            throw new LocalizedFieldValidationException(e.getMessageSeed(), "properties." + e.getViolatingProperty());
        }
        return convertedProperties;
    }

    private void prepareCommunicationTask(Device device, Map<String, Object> convertedProperties) {
        FirmwareManagementDeviceUtils helper = this.firmwareService.getFirmwareManagementDeviceUtilsFor(device);
        Optional<ComTaskExecution> fuComTaskExecutionRef = helper.getFirmwareComTaskExecution();
        if (!fuComTaskExecutionRef.isPresent()) {
            createFirmwareComTaskExecution(device);
        } else {
            cancelOldFirmwareUpdates(helper, convertedProperties);
        }
    }

    private void cancelOldFirmwareUpdates(FirmwareManagementDeviceUtils helper, Map<String, Object> convertedProperties) {
        String firmwareVersionPropertyName = DeviceMessageConstants.firmwareUpdateFileAttributeName;
        FirmwareVersion requestedFirmwareVersion = (FirmwareVersion) convertedProperties.get(firmwareVersionPropertyName);
        if (requestedFirmwareVersion != null) {
            helper.cancelPendingFirmwareUpdates(requestedFirmwareVersion.getFirmwareType());
        }
    }

    @DELETE @Transactional
    @Path("/firmwaremessages/{msgId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response cancelFirmwareUpload(@PathParam("mrid") String mrid, @PathParam("msgId") long msgId, DeviceFirmwareVersionInfos info) {
        Device device = resourceHelper.getLockedDevice(mrid, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(mrid)
                        .withActualVersion(() -> deviceService.findByUniqueMrid(mrid).map(Device::getVersion).orElse(null))
                        .supplier());
        DeviceMessage upgradeMessage = device.getMessages().stream()
                .filter(message -> message.getId() == msgId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.FIRMWARE_UPLOAD_NOT_FOUND, msgId));
        FirmwareManagementDeviceUtils firmwareManagementDeviceUtils = this.firmwareService.getFirmwareManagementDeviceUtilsFor(device);
        if (!firmwareManagementDeviceUtils.isPendingMessage(upgradeMessage)) {
            throw exceptionFactory.newException(MessageSeeds.FIRMWARE_UPLOAD_HAS_BEEN_STARTED_CANNOT_BE_CANCELED);
        }
        upgradeMessage.revoke();
        // if we have the pending message that means we need to reschedule comTaskExecution for firmware upgrade
        rescheduleFirmwareUpgradeTask(device);
        return Response.ok().build();
    }

    @GET @Transactional
    @Path("/firmwaresactions")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE})
    public Response getDynamicActions(@PathParam("mrid") String mrid, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMridOrThrowException(mrid);
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
        String runNowActionTitle = thesaurus.getFormat(MessageSeeds.Keys.FIRMWARE_ACTION_CHECK_VERSION_NOW_TRANSLATION_KEY).format();
        DeviceFirmwareActionInfo info = new DeviceFirmwareActionInfo("runnow", runNowActionTitle);
        info.version = device.getVersion();
        deviceFirmwareActions.add(info);
        return Response.ok(PagedInfoList.fromPagedList("firmwareactions", deviceFirmwareActions, queryParameters)).build();
    }

    @PUT @Transactional
    @Path("/status/run")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE})
    public Response runFirmwareVersionCheck(@PathParam("mrid") String mrid, @BeanParam JsonQueryParameters queryParameters, DeviceFirmwareActionInfo info) {
        String actionName = thesaurus.getFormat(MessageSeeds.FIRMWARE_ACTION_CHECK_VERSION).format();
        Device device = resourceHelper.getLockedDevice(mrid, info.version)
                .orElseThrow(conflictFactory.conflict()
                        .withActualVersion(() -> deviceService.findByUniqueMrid(mrid).map(Device::getVersion).orElse(null))
                        .withMessageTitle(MessageSeeds.FIRMWARE_CHECK_TASK_CONCURRENT_FAIL_TITLE, actionName)
                        .withMessageBody(MessageSeeds.FIRMWARE_CHECK_TASK_CONCURRENT_FAIL_BODY, actionName)
                        .supplier());
        device.runStatusInformationTask(ComTaskExecution::scheduleNow);
        return Response.ok().build();
    }


    @PUT @Transactional
    @Path("/status/runnow")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE})
    public Response runFirmwareVersionCheckNow(@PathParam("mrid") String mrid, @BeanParam JsonQueryParameters queryParameters, DeviceFirmwareActionInfo info) {
        String actionName = thesaurus.getFormat(MessageSeeds.Keys.FIRMWARE_ACTION_CHECK_VERSION_NOW_TRANSLATION_KEY).format();
        Device device = resourceHelper.getLockedDevice(mrid, info.version)
                .orElseThrow(conflictFactory.conflict()
                        .withActualVersion(() -> deviceService.findByUniqueMrid(mrid).map(Device::getVersion).orElse(null))
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
                .filter(message -> !firmwareUpgradeExecution.isPresent() || firmwareUpgradeExecution.get().getLastExecutionStartTimestamp() == null || !message.getReleaseDate().isBefore(firmwareUpgradeExecution.get().getLastExecutionStartTimestamp()))
                .count() == 0 && !helper.firmwareUploadTaskIsBusy();
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
        FirmwareManagementDeviceUtils helper = this.firmwareService.getFirmwareManagementDeviceUtilsFor(device);
        Optional<Instant> earliestReleaseDate = helper.getPendingFirmwareMessages().stream()
                .map(DeviceMessage::getReleaseDate)
                .sorted(Instant::compareTo)
                .findFirst();
        rescheduleFirmwareUpgradeTask(helper, earliestReleaseDate.orElse(null));
    }

    private void rescheduleFirmwareUpgradeTask(FirmwareManagementDeviceUtils helper, Instant earliestReleaseDate) {
        Optional<ComTaskExecution> firmwareComTaskExecution = helper.getFirmwareComTaskExecution();
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
