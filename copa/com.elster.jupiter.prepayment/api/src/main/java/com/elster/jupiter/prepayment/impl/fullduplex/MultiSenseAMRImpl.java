package com.elster.jupiter.prepayment.impl.fullduplex;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.prepayment.impl.MessageSeeds;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.protocol.api.TrackingCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.StatusInformationTask;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * MultiSense implementation of FullDuplex
 *
 * @author sva
 * @since 1/04/2016 - 9:15
 */
public class MultiSenseAMRImpl implements FullDuplexInterface {

    private static final String UNDEFINED = "undefined";

    private final Clock clock;
    private final DeviceService deviceService;
    private final ExceptionFactory exceptionFactory;
    private final MeteringService meteringService;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;

    @Inject
    public MultiSenseAMRImpl(Clock clock, DeviceService deviceService, ExceptionFactory exceptionFactory, MeteringService meteringService, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.clock = clock;
        this.deviceService = deviceService;
        this.exceptionFactory = exceptionFactory;
        this.meteringService = meteringService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    public void armBreaker(EndDevice endDevice, ServiceCall serviceCall, Instant activationDate) {
        Device device = findDeviceForEndDevice(endDevice);
        List<DeviceMessageId> deviceMessageIds = new ArrayList<>();
        deviceMessageIds.add(activationDate != null ? DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE : DeviceMessageId.CONTACTOR_OPEN);
        deviceMessageIds.add(activationDate != null ? DeviceMessageId.CONTACTOR_ARM_WITH_ACTIVATION_DATE : DeviceMessageId.CONTACTOR_ARM);

        Map<String, Object> attributes = new HashMap<>();
        if (activationDate != null) {
            attributes.put(DeviceMessageConstants.contactorActivationDateAttributeName, Date.from(activationDate));
        }
        createDeviceMessagesOnDevice(device, serviceCall, deviceMessageIds, attributes);
        scheduleDeviceCommandsComTaskEnablement(device, deviceMessageIds);
    }

    public void connectBreaker(EndDevice endDevice, ServiceCall serviceCall, Instant activationDate) {
        Device device = findDeviceForEndDevice(endDevice);
        List<DeviceMessageId> deviceMessageIds = new ArrayList<>();
        deviceMessageIds.add(activationDate != null ? DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE : DeviceMessageId.CONTACTOR_CLOSE);

        Map<String, Object> attributes = new HashMap<>();
        if (activationDate != null) {
            attributes.put(DeviceMessageConstants.contactorActivationDateAttributeName, Date.from(activationDate));
        }
        createDeviceMessagesOnDevice(device, serviceCall, deviceMessageIds, attributes);
        scheduleDeviceCommandsComTaskEnablement(device, deviceMessageIds);
    }

    public void disconnectBreaker(EndDevice endDevice, ServiceCall serviceCall, Instant activationDate) {
        Device device = findDeviceForEndDevice(endDevice);
        List<DeviceMessageId> deviceMessageIds = new ArrayList<>();
        deviceMessageIds.add(activationDate != null ? DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE : DeviceMessageId.CONTACTOR_OPEN);

        Map<String, Object> attributes = new HashMap<>();
        if (activationDate != null) {
            attributes.put(DeviceMessageConstants.contactorActivationDateAttributeName, Date.from(activationDate));
        }
        createDeviceMessagesOnDevice(device, serviceCall, deviceMessageIds, attributes);
        scheduleDeviceCommandsComTaskEnablement(device, deviceMessageIds);
    }

    public void disableLoadLimiting(EndDevice endDevice, ServiceCall serviceCall) {
        Device device = findDeviceForEndDevice(endDevice);
        List<DeviceMessageId> deviceMessageIds = new ArrayList<>();
        deviceMessageIds.add(DeviceMessageId.LOAD_BALANCING_DISABLE_LOAD_LIMITING);

        createDeviceMessagesOnDevice(device, serviceCall, deviceMessageIds, Collections.emptyMap());
        scheduleDeviceCommandsComTaskEnablement(device, deviceMessageIds);
    }

    public void configureLoadLimitThresholdAndDuration(EndDevice endDevice, ServiceCall serviceCall, BigDecimal limit, String unit, Integer loadTolerance) {
        Device device = findDeviceForEndDevice(endDevice);
        List<DeviceMessageId> deviceMessageIds = new ArrayList<>();
        deviceMessageIds.add(DeviceMessageId.LOAD_BALANCING_CONFIGURE_LOAD_LIMIT_THRESHOLD_AND_DURATION);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(DeviceMessageConstants.normalThresholdAttributeName, limit);
        attributes.put(DeviceMessageConstants.unitAttributeName, (unit == null || unit.isEmpty()) ? UNDEFINED : unit);
        attributes.put(DeviceMessageConstants.overThresholdDurationAttributeName, TimeDuration.seconds(loadTolerance));
        createDeviceMessagesOnDevice(device, serviceCall, deviceMessageIds, attributes);
        scheduleDeviceCommandsComTaskEnablement(device, deviceMessageIds);
    }

    public void configureLoadLimitThreshold(EndDevice endDevice, ServiceCall serviceCall, BigDecimal limit, String unit) {
        Device device = findDeviceForEndDevice(endDevice);
        List<DeviceMessageId> deviceMessageIds = new ArrayList<>();
        deviceMessageIds.add(DeviceMessageId.LOAD_BALANCING_SET_LOAD_LIMIT_THRESHOLD);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(DeviceMessageConstants.normalThresholdAttributeName, limit);
        attributes.put(DeviceMessageConstants.unitAttributeName, (unit == null || unit.isEmpty()) ? UNDEFINED : unit);
        createDeviceMessagesOnDevice(device, serviceCall, deviceMessageIds, attributes);
        scheduleDeviceCommandsComTaskEnablement(device, deviceMessageIds);
    }

    private void scheduleDeviceCommandsComTaskEnablement(Device device, List<DeviceMessageId> deviceMessageIds) {
        getComTaskEnablementsForDeviceMessages(device, deviceMessageIds).forEach(comTaskEnablement -> {
            Optional<ComTaskExecution> existingComTaskExecution = device.getComTaskExecutions().stream()
                    .filter(cte -> cte.getComTasks().stream()
                            .anyMatch(comTask -> comTask.getId() == comTaskEnablement.getComTask().getId()))
                    .findFirst();
            existingComTaskExecution.orElseGet(() -> createAdHocComTaskExecution(device, comTaskEnablement)).runNow();
        });
    }

    private ManuallyScheduledComTaskExecution createAdHocComTaskExecution(Device device, ComTaskEnablement comTaskEnablement) {
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        if (comTaskEnablement.hasPartialConnectionTask()) {
            device.getConnectionTasks().stream()
                    .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == comTaskEnablement.getPartialConnectionTask().get().getId())
                    .findFirst()
                    .ifPresent(comTaskExecutionBuilder::connectionTask);
        }
        ManuallyScheduledComTaskExecution manuallyScheduledComTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        return manuallyScheduledComTaskExecution;
    }

    private Stream<ComTaskEnablement> getComTaskEnablementsForDeviceMessages(Device device, List<DeviceMessageId> deviceMessageIds) {
        List<ComTaskEnablement> comTaskEnablements = new ArrayList<>();
        deviceMessageIds.stream()
                .forEach(deviceMessageId -> comTaskEnablements.add(device.getDeviceConfiguration()
                        .getComTaskEnablements()
                        .stream()
                        .filter(cte -> cte.getComTask().getProtocolTasks().stream().
                                filter(task -> task instanceof MessagesTask).
                                flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                                flatMap(category -> category.getMessageSpecifications().stream()).
                                filter(dms -> dms.getId().equals(deviceMessageId)).
                                findFirst().
                                isPresent())
                        .findAny()
                        .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_COMTASK_FOR_COMMAND))));

        return comTaskEnablements.stream().distinct();
    }

    /**
     * @throws javax.validation.ConstraintViolationException in case not all DeviceMessageAttributes have a value
     */
    private List<DeviceMessage<Device>> createDeviceMessagesOnDevice(Device device, ServiceCall serviceCall, List<DeviceMessageId> deviceMessageIds, Map<String, Object> attributes) {
        List<DeviceMessage<Device>> deviceMessages = new ArrayList<>();
        for (DeviceMessageId deviceMessageId : deviceMessageIds) {
            Optional<DeviceMessageSpec> deviceMessageSpec = this.deviceMessageSpecificationService.findMessageSpecById(deviceMessageId.dbValue());
            Device.DeviceMessageBuilder deviceMessageBuilder = device.newDeviceMessage(deviceMessageId, TrackingCategory.serviceCall)
                    .setTrackingId("" + serviceCall.getId())
                    .setReleaseDate(clock.instant());
            for (PropertySpec propertySpec : deviceMessageSpec.get().getPropertySpecs()) {
                if (attributes.containsKey(propertySpec.getName())) {
                    deviceMessageBuilder.addProperty(propertySpec.getName(), attributes.get(propertySpec.getName()));
                }
            }
            deviceMessages.add(deviceMessageBuilder.add());
        }
        return deviceMessages;
    }

    @Override
    public void scheduleStatusInformationTask(EndDevice endDevice, Instant scheduleTime) {
        Device device = findDeviceForEndDevice(endDevice);
        ComTaskEnablement comTaskEnablement = getStatusInformationComTaskEnablement(device);
        Optional<ComTaskExecution> existingComTaskExecution = device.getComTaskExecutions().stream()
                .filter(cte -> cte.getComTasks().stream().anyMatch(comTask -> comTask.getId() == comTaskEnablement.getComTask().getId())).findFirst();
        existingComTaskExecution.orElseGet(() -> createAdHocComTaskExecution(device, comTaskEnablement)).schedule(scheduleTime);
    }

    private ComTaskEnablement getStatusInformationComTaskEnablement(Device device) {
        return device.getDeviceConfiguration()
                .getComTaskEnablements()
                .stream()
                .filter(cte -> cte.getComTask().getProtocolTasks().stream().
                        filter(task -> task instanceof StatusInformationTask).
                        findFirst().
                        isPresent())
                .findAny()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_COMTASK_FOR_STATUS_INFORMATION));
    }

    private Device findDeviceForEndDevice(EndDevice endDevice) {
        return deviceService.findByUniqueMrid(endDevice.getMRID()).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE, endDevice.getMRID()));
    }
}