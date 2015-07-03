package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.FirmwareComTaskExecution;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** TODO replace by /devices*/
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
    private final Provider<FirmwareManagementDeviceUtils.Factory> utilProvider;

    @Inject
    public DeviceFirmwareMessagesResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, FirmwareService firmwareService, FirmwareMessageInfoFactory firmwareMessageInfoFactory, TaskService taskService, Clock clock, MdcPropertyUtils mdcPropertyUtils, Thesaurus thesaurus, Provider<FirmwareManagementDeviceUtils.Factory> utilProvider) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.firmwareService = firmwareService;
        this.firmwareMessageInfoFactory = firmwareMessageInfoFactory;
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
    public Response getMessageAttributes(@PathParam("mrid") String mrid, @PathParam("uploadOption") String uploadOption, @QueryParam("firmwareType") String firmwareType){
        Device device = resourceHelper.findDeviceByMridOrThrowException(mrid);
        DeviceMessageSpec firmwareMessageSpec = resourceHelper.findFirmwareMessageSpecOrThrowException(device.getDeviceType(), uploadOption);
        return Response.ok(firmwareMessageInfoFactory.from(firmwareMessageSpec, device, uploadOption, firmwareType)).build();
    }

    @POST
    @Path("/firmwaremessages")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.OPERATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response uploadFirmwareToDevice(@PathParam("mrid") String mrid, FirmwareMessageInfo info){
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
        DeviceMessage<Device> newFirmwareMessage = deviceMessageBuilder.add();
        // Default message validation was successfully passed, trigger activation date validation
        validateActivationDate(newFirmwareMessage, convertedProperties);
        rescheduleFirmwareUpgradeTask(device);
        return Response.status(Response.Status.CREATED).build();
    }

    private void validateActivationDate(DeviceMessage<Device> firmwareMessage, Map<String, Object> properties) {
        if (DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE.equals(firmwareMessage.getDeviceMessageId())){
            Object activationDateAsObj = properties.get(DeviceMessageConstants.firmwareUpdateActivationDateAttributeName);
            if (activationDateAsObj != null) {
                if (((Date) activationDateAsObj).toInstant().isBefore(firmwareMessage.getReleaseDate())) {
                    new RestValidationBuilder().addValidationError(
                            new LocalizedFieldValidationException(MessageSeeds.FIRMWARE_ACTIVATION_DATE_IS_BEFORE_UPLOAD,
                                    "deviceMessageAttributes." + DeviceMessageConstants.firmwareUpdateActivationDateAttributeName))
                            .validate();
                }
            }
        }
    }

    @PUT
    @Path("/firmwaremessages/{messageId}/activate")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.OPERATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response activateFirmwareOnDevice(@PathParam("mrid") String mrid, @PathParam("messageId") Long messageId, FirmwareMessageInfo info) {
        Device device = resourceHelper.findDeviceByMridOrThrowException(mrid);

        DeviceMessage<Device> upgradeMessage = device.getMessages().stream()
                .filter(message -> message.getId() == messageId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.FIRMWARE_UPLOAD_NOT_FOUND, messageId));

        FirmwareManagementDeviceUtils versionUtils = utilProvider.get().onDevice(device);
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

    /** This method converts the incoming info object to property-value map (according to the specified message id) */
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
            throw new LocalizedFieldValidationException(e.getMessageSeed(), "properties."+e.getViolatingProperty());
        }
        return convertedProperties;
    }

    private void prepareCommunicationTask(Device device, Map<String, Object> convertedProperties) {
        Optional<ComTaskExecution> fuComTaskExecutionRef = findFirmwareComTaskExecution(device);
        if (!fuComTaskExecutionRef.isPresent()){
            createFirmwareComTaskExecution(device);
        } else {
            cancelOldFirmwareUpdates(device, convertedProperties);
        }
    }

    private void cancelOldFirmwareUpdates(Device device, Map<String, Object> convertedProperties) {
        String firmwareVersionPropertyName = DeviceMessageConstants.firmwareUpdateFileAttributeName;
        FirmwareVersion requestedFirmwareVersion = (FirmwareVersion) convertedProperties.get(firmwareVersionPropertyName);
        if (requestedFirmwareVersion != null) {
            utilProvider.get()
                    .onDevice(device)
                    .cancelPendingFirmwareUpdates(requestedFirmwareVersion.getFirmwareType());
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
        if (!FirmwareManagementDeviceUtils.PENDING_STATUSES.contains(upgradeMessage.getStatus())){
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
    public Response getDynamicActions(@PathParam("mrid") String mrid, @BeanParam JsonQueryParameters queryParameters){
        Device device = resourceHelper.findDeviceByMridOrThrowException(mrid);
        List<DeviceFirmwareActionInfo> deviceFirmwareActions = new ArrayList<>();
        FirmwareManagementDeviceUtils helper = utilProvider.get().onDevice(device);
        if (isDeviceFirmwareUpgradeAllowed(helper)) {
            deviceFirmwareActions = firmwareService.getAllowedFirmwareManagementOptionsFor(device.getDeviceType())
                    .stream()
                    .map(option -> new DeviceFirmwareActionInfo(option.getId(), thesaurus.getString(option.getId(), option.getId())))
                    .collect(Collectors.toList());
        }

        Optional<ScheduledConnectionTask> defaultConnectionTask = device.getScheduledConnectionTasks()
                .stream()
                .filter(conTask -> conTask.isDefault())
                .findFirst();
        if (defaultConnectionTask.isPresent() && ConnectionStrategy.MINIMIZE_CONNECTIONS.equals(defaultConnectionTask.get().getConnectionStrategy())){
            deviceFirmwareActions.add(new DeviceFirmwareActionInfo("run", thesaurus.getString(MessageSeeds.FIRMWARE_ACTION_CHECK_VERSION.getKey(), MessageSeeds.FIRMWARE_ACTION_CHECK_VERSION.getDefaultFormat())));
        }
        deviceFirmwareActions.add(new DeviceFirmwareActionInfo("runnow", thesaurus.getString(MessageSeeds.FIRMWARE_ACTION_CHECK_VERSION_NOW.getKey(), MessageSeeds.FIRMWARE_ACTION_CHECK_VERSION_NOW.getDefaultFormat())));
        return Response.ok(PagedInfoList.fromPagedList("firmwareactions", deviceFirmwareActions, queryParameters)).build();
    }

    @PUT
    @Path("/status/run")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.VIEW_DEVICE})
    public Response runFirmwareVersionCheck(@PathParam("mrid") String mrid, @BeanParam JsonQueryParameters queryParameters){
        Device device = resourceHelper.findDeviceByMridOrThrowException(mrid);
        launchFirmwareCheck(device, ComTaskExecution::scheduleNow);
        return Response.ok().build();
    }


    @PUT
    @Path("/status/runnow")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.VIEW_DEVICE})
    public Response runFirmwareVersionCheckNow(@PathParam("mrid") String mrid, @BeanParam JsonQueryParameters queryParameters){
        Device device = resourceHelper.findDeviceByMridOrThrowException(mrid);
        launchFirmwareCheck(device, ComTaskExecution::runNow);
        return Response.ok().build();
    }

    private void launchFirmwareCheck(Device device, Consumer<ComTaskExecution> requestedActionOnExec) {
        Optional<ComTaskExecution> firmwareCheckExecution = device.getComTaskExecutions()
                .stream()
                .filter(ComTaskExecution::isConfiguredToReadStatusInformation)
                .findFirst();
        if (!firmwareCheckExecution.isPresent()){
            firmwareCheckExecution = createFirmwareCheckExecution(device);
        }
        if (!firmwareCheckExecution.isPresent()){
            throw exceptionFactory.newException(MessageSeeds.FIRMWARE_CHECK_TASK_IS_NOT_ACTIVE);
        }
        requestedActionOnExec.accept(firmwareCheckExecution.get());
    }


    private Optional<ComTaskExecution> createFirmwareCheckExecution(Device device) {
        Optional<ComTaskEnablement> firmwareCheckEnablementRef = FirmwareManagementDeviceUtils.getFirmwareCheckEnablement(device);
        if (firmwareCheckEnablementRef.isPresent()) {
            ComTaskEnablement firmwareCheckEnablement = firmwareCheckEnablementRef.get();
            ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> firmwareCheckExecutionBuilder = device.newAdHocComTaskExecution(firmwareCheckEnablement);
            if (firmwareCheckEnablement.hasPartialConnectionTask()) {
                device.getConnectionTasks().stream()
                        .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == firmwareCheckEnablement.getPartialConnectionTask().get().getId())
                        .forEach(firmwareCheckExecutionBuilder::connectionTask);
            }
            ManuallyScheduledComTaskExecution manuallyScheduledComTaskExecution = firmwareCheckExecutionBuilder.add();
            device.save();
            return Optional.of(manuallyScheduledComTaskExecution);
        }
        return Optional.empty();
    }

    private boolean isDeviceFirmwareUpgradeAllowed(FirmwareManagementDeviceUtils helper) {
        Optional<ComTaskExecution> firmwareUpgradeExecution = helper.getFirmwareExecution();
        return helper.getFirmwareMessages().stream()
                .filter(message -> FirmwareManagementDeviceUtils.PENDING_STATUSES.contains(message.getStatus()))
                .filter(message -> !firmwareUpgradeExecution.isPresent()|| firmwareUpgradeExecution.get().getLastExecutionStartTimestamp() == null || !message.getReleaseDate().isBefore(firmwareUpgradeExecution.get().getLastExecutionStartTimestamp()))
                .count() == 0 && !helper.taskIsBusy();
    }

    /** Checks that device type allows the requested firmware upgrade option */
    private void checkFirmwareUpgradeOption(DeviceType deviceType, String uploadOption) {
        ProtocolSupportedFirmwareOptions requestedFUOption = resourceHelper.findProtocolSupportedFirmwareOptionsOrThrowException(uploadOption);
        Set<ProtocolSupportedFirmwareOptions> deviceTypeFUAllowedOptions = firmwareService.getAllowedFirmwareManagementOptionsFor(deviceType);
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
        FirmwareManagementDeviceUtils versionUtils = utilProvider.get().onDevice(device);
        Optional<Instant> earliestReleaseDate = versionUtils.getFirmwareMessages().stream()
                .filter(message -> FirmwareManagementDeviceUtils.PENDING_STATUSES.contains(message.getStatus()))
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
