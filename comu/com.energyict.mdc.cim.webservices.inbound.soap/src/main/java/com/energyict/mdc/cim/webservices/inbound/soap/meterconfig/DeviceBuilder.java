/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.DefaultState;
import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.cim.webservices.inbound.soap.MeterInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.SecurityInfo;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.common.scheduling.ComSchedule;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.scheduling.SchedulingService;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import ch.iec.tc57._2011.meterconfig.Attribute;
import ch.iec.tc57._2011.meterconfig.ConnectionAttributes;
import ch.iec.tc57._2011.meterconfig.SharedCommunicationSchedule;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DeviceBuilder {
    private static final String METER_CONFIG_MULTIPLIER_ITEM = "MeterConfig.Meter.multiplier";
    private static final String METER_CONFIG_STATUS_ITEM = "MeterConfig.Meter.status.value";

    private final BatchService batchService;
    private final Clock clock;
    private final DeviceLifeCycleService deviceLifeCycleService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceService deviceService;
    private final MeterConfigFaultMessageFactory faultMessageFactory;
    private final MeteringTranslationService meteringTranslationService;
    private final SchedulingService schedulingService;

    @Inject
    public DeviceBuilder(BatchService batchService, Clock clock, DeviceLifeCycleService deviceLifeCycleService,
                         DeviceConfigurationService deviceConfigurationService, DeviceService deviceService,
                         MeterConfigFaultMessageFactory faultMessageFactory,
                         MeteringTranslationService meteringTranslationService,
                         SchedulingService schedulingService) {
        this.batchService = batchService;
        this.clock = clock;
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceService = deviceService;
        this.faultMessageFactory = faultMessageFactory;
        this.meteringTranslationService = meteringTranslationService;
        this.schedulingService = schedulingService;
    }

    public PreparedDeviceBuilder prepareCreateFrom(MeterInfo meter) throws FaultMessage {
        DeviceConfiguration deviceConfig = findDeviceConfiguration(meter, meter.getDeviceConfigurationName(),
                meter.getDeviceType());
        return () -> {
            List<Device> existentDevices = getExistentDevices(meter.getDeviceName(), meter.getSerialNumber());
            if (!existentDevices.isEmpty()) {
                if (!meter.isFailOnExistentDevice() && existentDevices.size() == 1 && haveSameNameSerialAndDeviceType(existentDevices.get(0), meter)) {
                    return existentDevices.get(0);
                } else {
                    throw faultMessageSupplier(meter.getDeviceName(), MessageSeeds.NAME_AND_SERIAL_MUST_BE_UNIQUE).get();
                }
            }
            com.energyict.mdc.device.data.DeviceBuilder deviceBuilder = deviceService.newDeviceBuilder(deviceConfig,
                    meter.getDeviceName(), meter.getShipmentDate());
            deviceBuilder.withBatch(meter.getBatch());
            deviceBuilder.withSerialNumber(meter.getSerialNumber());
            deviceBuilder.withManufacturer(meter.getManufacturer());
            deviceBuilder.withModelNumber(meter.getModelNumber());
            deviceBuilder.withModelVersion(meter.getModelVersion());
            deviceBuilder.withMultiplier(meter.getMultiplier());
            Multimap<String, String> mapZones = ArrayListMultimap.create();
            meter.getZones().forEach(zone -> mapZones.put(zone.getZoneName(), zone.getZoneType()));
            deviceBuilder.withZones(mapZones);
            Device device = deviceBuilder.create();
            setConnectionAttributes(device, meter.getConnectionAttributes());
            Set<String> scheduleNames = extractSharedCommunicationSchedules(meter);
            List<ComSchedule> comSchedules = new ArrayList<>();
            for (String scheduleName : scheduleNames) {
                Optional<ComSchedule> optionalComSchedule = schedulingService.findScheduleByName(scheduleName);
                if (optionalComSchedule.isPresent()) {
                    comSchedules.add(optionalComSchedule.get());
                } else {
                    throw faultMessageSupplier(meter.getDeviceName(), MessageSeeds.SCHEDULE_FOR_METER_NOT_FOUND, scheduleName).get();
                }
            }
            for (ComSchedule comSchedule : comSchedules) {
                device.newScheduledComTaskExecution(comSchedule).add();
            }
            return device;
        };
    }

    private Set<String> extractSharedCommunicationSchedules(MeterInfo meter) {
        Set<String> result = new HashSet<>();
        for (SharedCommunicationSchedule schedule : meter.getSharedCommunicationSchedules()) {
            result.add(schedule.getName());
        }
        return result;
    }


    public PreparedDeviceBuilder prepareChangeFrom(MeterInfo meter) throws FaultMessage {
        Optional<String> batch = Optional.ofNullable(meter.getBatch());
        Optional<String> serialNumber = Optional.ofNullable(meter.getSerialNumber());
        Optional<String> manufacturer = Optional.ofNullable(meter.getManufacturer());
        Optional<String> modelNumber = Optional.ofNullable(meter.getModelNumber());
        Optional<String> modelVersion = Optional.ofNullable(meter.getModelVersion());
        Optional<BigDecimal> multiplier = Optional.ofNullable(meter.getMultiplier());
        Optional<String> mrid = Optional.ofNullable(meter.getmRID());
        Optional<String> configurationEventReason = Optional.ofNullable(meter.getConfigurationEventReason());
        Optional<String> statusValue = Optional.ofNullable(meter.getStatusValue());
        Optional<Instant> statusEffectiveDate = Optional.ofNullable(meter.getStatusEffectiveDate());
        Optional<Instant> multiplierEffectiveDate = Optional.ofNullable(meter.getMultiplierEffectiveDate());
        Optional<Instant> shipmentDate = Optional.ofNullable(meter.getShipmentDate());
        String newDeviceConfigurationName = meter.getDeviceConfigurationName();

        return () -> {
            Device changedDevice;
            if (mrid.isPresent()) {
                changedDevice = findDeviceByMRID(meter, mrid.get());
            } else if (meter.getDeviceName() != null) {
                changedDevice = deviceService.findDeviceByName(meter.getDeviceName())
                        .orElseThrow(faultMessageSupplier(meter.getDeviceName(),
                                MessageSeeds.NO_DEVICE_WITH_NAME, meter.getDeviceName()));
            } else {
                List<Device> foundDevices = deviceService.findDevicesBySerialNumber(meter.getSerialNumber());
                if (foundDevices.isEmpty()) {
                    throw faultMessageSupplier(meter.getDeviceName(),
                            MessageSeeds.NO_DEVICE_WITH_SERIAL_NUMBER, meter.getSerialNumber()).get();
                } else if (foundDevices.size() > 1) {
                    throw faultMessageSupplier(meter.getDeviceName(),
                            MessageSeeds.MORE_DEVICES_WITH_SAME_SERIAL_NUMBER, meter.getSerialNumber()).get();
                } else {
                    changedDevice = foundDevices.get(0);
                }
            }
            changedDevice = deviceService.findAndLockDeviceById(changedDevice.getId())
                    .orElseThrow(faultMessageSupplier(meter.getDeviceName(), MessageSeeds.NO_SUCH_DEVICE, changedDevice.getId()));

            validateSecurityKeyChangeIsAllowedOnUpdate(changedDevice, meter.getSecurityInfo());

            if (newDeviceConfigurationName != null
                    && !changedDevice.getDeviceConfiguration().getName().equals(newDeviceConfigurationName)) {
                DeviceConfiguration deviceConfig = changedDevice.getDeviceType().getConfigurations().stream()
                        .filter(config -> newDeviceConfigurationName.equals(config.getName())).findAny()
                        .orElseThrow(faultMessageSupplier(meter.getDeviceName(),
                                MessageSeeds.NO_SUCH_DEVICE_CONFIGURATION, newDeviceConfigurationName));

                changedDevice = deviceService.changeDeviceConfigurationForSingleDevice(changedDevice.getId(),
                        changedDevice.getVersion(), deviceConfig.getId(), deviceConfig.getVersion());
            }
            String currentModelNumber = changedDevice.getModelNumber();
            String currentModelVersion = changedDevice.getModelVersion();
            String currentManufacturer = changedDevice.getManufacturer();

            if (configurationEventReason.isPresent()) {
                EventReason.forReason(configurationEventReason.get())
                        .orElseThrow(faultMessageSupplier(meter.getDeviceName(),
                                MessageSeeds.NOT_VALID_CONFIGURATION_REASON, configurationEventReason.get()));
            }

            if (configurationEventReason.flatMap(EventReason::forReason).filter(EventReason.CHANGE_MULTIPLIER::equals)
                    .isPresent()) {
                changedDevice.setMultiplier(
                        multiplier.orElseThrow(faultMessageSupplier(
                                meter.getDeviceName(), MessageSeeds.MISSING_ELEMENT, METER_CONFIG_MULTIPLIER_ITEM)),
                        multiplierEffectiveDate.orElse(clock.instant()));
            }

            if (mrid.isPresent() && meter.getDeviceName() != null) {
                changedDevice.setName(meter.getDeviceName());
            }

            if (batch.isPresent()) {
                batchService.findOrCreateBatch(batch.get()).addDevice(changedDevice);
            }
            if (shipmentDate.isPresent() && shipmentDate.get().isAfter(new Date(0).toInstant())) {
                changedDevice.getLifecycleDates().setReceivedDate(shipmentDate.get());
            }
            serialNumber.ifPresent(changedDevice::setSerialNumber);
            changedDevice.setModelNumber(modelNumber.orElse(currentModelNumber));
            changedDevice.setModelVersion(modelVersion.orElse(currentModelVersion));
            changedDevice.setManufacturer(manufacturer.orElse(currentManufacturer));
            updateDevice(changedDevice);

            Multimap<String, String> mapZones = ArrayListMultimap.create();
            changedDevice.removeZonesOnDevice();
            meter.getZones().forEach(zone -> mapZones.put(zone.getZoneName(), zone.getZoneType()));
            for (Map.Entry<String, String> zone : mapZones.entries()) {
                changedDevice.addZone(zone.getKey(), zone.getValue());
            }

            if (configurationEventReason.flatMap(EventReason::forReason).filter(EventReason.CHANGE_STATUS::equals)
                    .isPresent()) {
                String state = statusValue.orElseThrow(faultMessageSupplier(
                        meter.getDeviceName(), MessageSeeds.MISSING_ELEMENT, METER_CONFIG_STATUS_ITEM));
                Instant effectiveDate = statusEffectiveDate.orElse(clock.instant());
                ExecutableAction executableAction = deviceLifeCycleService.getExecutableActions(changedDevice)
                        .stream()
                        .filter(action -> action.getAction() instanceof AuthorizedTransitionAction)
                        .filter(action -> isActionForState((AuthorizedTransitionAction) action.getAction(), state))
                        .findFirst()
                        .orElseThrow(faultMessageSupplier(meter.getDeviceName(),
                                MessageSeeds.UNABLE_TO_CHANGE_DEVICE_STATE, statusValue.orElse("")));
                executableAction.execute(effectiveDate, Collections.emptyList());
                //in case the device is removed, this will not be found anymore when searching for mRID
                if (!deviceService.findDeviceByMrid(changedDevice.getmRID()).isPresent()) {
                    return null;
                }
                changedDevice = deviceService.findDeviceByMrid(changedDevice.getmRID()).get();
            }

            setConnectionAttributes(changedDevice, meter.getConnectionAttributes());
            return changedDevice;
        };
    }

    @FunctionalInterface
    public interface PreparedDeviceBuilder {

        @TransactionRequired
        Device build() throws FaultMessage;
    }

    private Device findDeviceByMRID(MeterInfo meter, String mrid) throws FaultMessage {
        return deviceService.findDeviceByMrid(mrid)
                .orElseThrow(faultMessageSupplier(meter.getDeviceName(), MessageSeeds.NO_DEVICE_WITH_MRID, mrid));
    }

    private void validateSecurityKeyChangeIsAllowedOnUpdate(Device device, SecurityInfo securityInfo)
            throws FaultMessage {
        if (securityInfo.getSecurityKeys().isEmpty()) {
            return;
        }
        if (securityInfo.isDeviceStatusesElementPresent()) {
            final List<String> allowedStatuses = securityInfo.getDeviceStatuses();
            final State status = device.getState();
            final String deviceStatus = DefaultState.from(status)
                    .map(meteringTranslationService::getDisplayName).orElseGet(status::getName);
            if (!allowedStatuses.contains(deviceStatus)) {
                throw faultMessageSupplier(device.getName(),
                        MessageSeeds.SECURITY_KEY_UPDATE_FORBIDDEN_FOR_DEVICE_STATUS, device.getName(), deviceStatus)
                        .get();
            }
        }
    }

    private DeviceConfiguration findDeviceConfiguration(MeterInfo meter, String deviceConfigurationName,
                                                        String deviceType) throws FaultMessage {
        Optional<DeviceConfiguration> deviceConfiguration =
                deviceConfigurationService.findDeviceTypeByName(deviceType)
                        .orElseThrow(faultMessageSupplier(meter.getDeviceName(),
                                MessageSeeds.NO_SUCH_DEVICE_TYPE, deviceType))
                        .getConfigurations()
                        .stream()
                        .filter(config -> config.getName().equals(deviceConfigurationName)
                                || deviceConfigurationName == null && config.isDefault())
                        .findAny();
        if (deviceConfiguration.isPresent()) {
            return deviceConfiguration.get();
        }
        if (deviceConfigurationName == null) {
            throw faultMessageSupplier(meter.getDeviceName(),
                    MessageSeeds.NO_DEFAULT_DEVICE_CONFIGURATION).get();
        }
        throw faultMessageSupplier(meter.getDeviceName(),
                MessageSeeds.NO_SUCH_DEVICE_CONFIGURATION, deviceConfigurationName).get();
    }

    public boolean isActionForState(AuthorizedTransitionAction authorizedTransitionAction, String state) {
        Optional<DefaultState> defaultState = DefaultState.from(authorizedTransitionAction.getStateTransition().getTo());
        return defaultState.isPresent() && defaultState.get().getDefaultFormat().equals(state);
    }

    private Device updateDevice(Device device) throws FaultMessage {
        long id = device.getId();
        if (getExistentDevices(device.getName(), device.getSerialNumber()).stream().allMatch(e -> e.getId() == id)) {
            device.save();
        } else {
            throw faultMessageSupplier(device.getName(), MessageSeeds.NAME_AND_SERIAL_MUST_BE_UNIQUE).get();
        }
        return device;
    }

    private List<Device> getExistentDevices(String name, String serialNumber) {
        Condition condition = Where.where("name").isEqualTo(name);
        if (serialNumber != null) {
            condition = condition.or(Where.where("serialNumber").isEqualTo(serialNumber));
        }
        return deviceService.findAllDevices(condition).paged(0, 2).find();
    }

    private void setConnectionAttributes(Device device, List<ConnectionAttributes> connectionAttributes) throws FaultMessage {
        for (ConnectionAttributes connAttribute : connectionAttributes) {
            if (!Checks.is(connAttribute.getConnectionMethod()).empty()) {
                Optional<ConnectionTask<?, ?>> connTask = device.getConnectionTasks().stream()
                        .filter(connectionTask -> connectionTask.getName().equals(connAttribute.getConnectionMethod()))
                        .findFirst();
                if (connTask.isPresent()) {
                    setConnectionTaskProperties(connTask.get(), connAttribute);
                } else {
                    throw faultMessageSupplier(device.getName(), MessageSeeds.NO_CONNECTION_METHOD_WITH_NAME, connAttribute.getConnectionMethod()).get();
                }
            } else {
                if (!device.getConnectionTasks().isEmpty()) {
                    for (ConnectionTask<?, ?> task : device.getConnectionTasks()) {
                        setConnectionTaskProperties(task, connAttribute);
                    }
                } else {
                    throw faultMessageSupplier(device.getName(), MessageSeeds.NO_CONNECTION_METHODS).get();
                }
            }
        }
    }

    private void setConnectionTaskProperties(ConnectionTask<?, ?> deviceConnectionTask, ConnectionAttributes connAttribute) throws FaultMessage {
        Map<String, PropertySpec> propertySpecMap = deviceConnectionTask.getPluggableClass().getPropertySpecs()
                .stream().collect(Collectors.toMap(PropertySpec::getName, item -> item));
        for (Attribute attribute : connAttribute.getAttribute()) {
            PropertySpec propertySpec = propertySpecMap.get(attribute.getName());
            if (propertySpec != null) {
                deviceConnectionTask.setProperty(attribute.getName(), propertySpec.getValueFactory().fromStringValue(attribute.getValue()));
            } else {
                throw faultMessageSupplier(deviceConnectionTask.getDevice().getName(), MessageSeeds.NO_CONNECTION_ATTRIBUTE, attribute.getName(), deviceConnectionTask.getName()).get();
            }
        }
        deviceConnectionTask.saveAllProperties();
    }

    private static boolean haveSameNameSerialAndDeviceType(Device device, MeterInfo info) {
        return device.getName().equals(info.getDeviceName())
                && Objects.equals(device.getSerialNumber(), info.getSerialNumber())
                && device.getDeviceType().getName().equals(info.getDeviceType());
    }

    private Supplier<FaultMessage> faultMessageSupplier(String deviceName, MessageSeeds messageSeed, Object... args) {
        return faultMessageFactory.meterConfigFaultMessageSupplier(deviceName, messageSeed, args);
    }
}
