/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import com.energyict.mdc.cim.webservices.inbound.soap.MeterInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;

import javax.inject.Inject;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DeviceBuilder {
    private static final String METER_CONFIG_MULTIPLIER_ITEM = "MeterConfig.Meter.multiplier";
    private static final String METER_CONFIG_STATUS_ITEM = "MeterConfig.Meter.status.value";

    private final BatchService batchService;
    private final Clock clock;
    private final DeviceLifeCycleService deviceLifeCycleService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceService deviceService;
    private final MeterConfigFaultMessageFactory faultMessageFactory;

    @Inject
    public DeviceBuilder(BatchService batchService, Clock clock, DeviceLifeCycleService deviceLifeCycleService,
            DeviceConfigurationService deviceConfigurationService, DeviceService deviceService,
            MeterConfigFaultMessageFactory faultMessageFactory) {
        this.batchService = batchService;
        this.clock = clock;
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceService = deviceService;
        this.faultMessageFactory = faultMessageFactory;
    }

    public PreparedDeviceBuilder prepareCreateFrom(MeterInfo meter) throws FaultMessage {
        DeviceConfiguration deviceConfig = findDeviceConfiguration(meter, meter.getDeviceConfigurationName(),
                meter.getDeviceType());
        return () -> {
            if (!getExistingDevices(meter.getDeviceName(), meter.getSerialNumber()).isEmpty()) {
                throw faultMessageFactory
                        .meterConfigFaultMessageSupplier(meter.getDeviceName(), MessageSeeds.NAME_MUST_BE_UNIQUE).get();
            }
            com.energyict.mdc.device.data.DeviceBuilder deviceBuilder = deviceService.newDeviceBuilder(deviceConfig,
                    meter.getDeviceName(), meter.getShipmentDate());
            deviceBuilder.withBatch(meter.getBatch());
            deviceBuilder.withSerialNumber(meter.getSerialNumber());
            deviceBuilder.withManufacturer(meter.getManufacturer());
            deviceBuilder.withModelNumber(meter.getModelNumber());
            deviceBuilder.withModelVersion(meter.getModelVersion());
            deviceBuilder.withMultiplier(meter.getMultiplier());
            return deviceBuilder.create();
        };
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
        String newDeviceConfigurationName = meter.getDeviceConfigurationName();

        return () -> {
            Device changedDevice = mrid.isPresent() ? findDeviceByMRID(meter, mrid.get())
                    : deviceService.findDeviceByName(meter.getDeviceName())
                            .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(meter.getDeviceName(),
                                    MessageSeeds.NO_DEVICE_WITH_NAME, meter.getDeviceName()));
            if (newDeviceConfigurationName != null
                    && !changedDevice.getDeviceConfiguration().getName().equals(newDeviceConfigurationName)) {
                DeviceConfiguration deviceConfig = findDeviceConfiguration(meter, newDeviceConfigurationName,
                        meter.getDeviceType());
                deviceService.changeDeviceConfigurationForSingleDevice(changedDevice.getId(),
                        changedDevice.getVersion(), deviceConfig.getId(), deviceConfig.getVersion());
            }
            String currentModelNumber = changedDevice.getModelNumber();
            String currentModelVersion = changedDevice.getModelVersion();
            String currentManufacturer = changedDevice.getManufacturer();

            if (configurationEventReason.isPresent()) {
                EventReason.forReason(configurationEventReason.get())
                        .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(meter.getDeviceName(),
                                MessageSeeds.NOT_VALID_CONFIGURATION_REASON, configurationEventReason.get()));
            }

            if (configurationEventReason.flatMap(EventReason::forReason).filter(EventReason.CHANGE_STATUS::equals)
                    .isPresent()) {
                String state = statusValue.orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(
                        meter.getDeviceName(), MessageSeeds.MISSING_ELEMENT, METER_CONFIG_STATUS_ITEM));
                Instant effectiveDate = statusEffectiveDate.orElse(clock.instant());
                ExecutableAction executableAction = deviceLifeCycleService.getExecutableActions(changedDevice).stream()
                        .filter(action -> action.getAction() instanceof AuthorizedTransitionAction)
                        .filter(action -> isActionForState((AuthorizedTransitionAction) action.getAction(), state))
                        .findFirst()
                        .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(meter.getDeviceName(),
                                MessageSeeds.UNABLE_TO_CHANGE_DEVICE_STATE, statusValue.orElse("")));
                executableAction.execute(effectiveDate, Collections.emptyList());
                updateDevice(changedDevice);
                changedDevice = findDeviceByMRID(meter, changedDevice.getmRID());
            }

            if (configurationEventReason.flatMap(EventReason::forReason).filter(EventReason.CHANGE_MULTIPLIER::equals)
                    .isPresent()) {
                changedDevice.setMultiplier(
                        multiplier.orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(
                                meter.getDeviceName(), MessageSeeds.MISSING_ELEMENT, METER_CONFIG_MULTIPLIER_ITEM)),
                        multiplierEffectiveDate.orElse(clock.instant()));
            }

            if (mrid.isPresent()) {
                changedDevice.setName(meter.getDeviceName());
            }

            if (batch.isPresent()) {
                batchService.findOrCreateBatch(batch.get()).addDevice(changedDevice);
            }

            serialNumber.ifPresent(changedDevice::setSerialNumber);
            changedDevice.setModelNumber(modelNumber.orElse(currentModelNumber));
            changedDevice.setModelVersion(modelVersion.orElse(currentModelVersion));
            changedDevice.setManufacturer(manufacturer.orElse(currentManufacturer));
            return updateDevice(changedDevice);
        };
    }

    @FunctionalInterface
    public interface PreparedDeviceBuilder {

        @TransactionRequired
        Device build() throws FaultMessage;
    }

    private Device findDeviceByMRID(MeterInfo meter, String mrid) throws FaultMessage {
        return deviceService.findDeviceByMrid(mrid).orElseThrow(faultMessageFactory
                .meterConfigFaultMessageSupplier(meter.getDeviceName(), MessageSeeds.NO_DEVICE_WITH_MRID, mrid));
    }

    private DeviceConfiguration findDeviceConfiguration(MeterInfo meter, String deviceConfigurationName,
            String deviceType) throws FaultMessage {
        Optional<DeviceConfiguration> deviceConfiguration = deviceConfigurationService.findDeviceTypeByName(deviceType)
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(meter.getDeviceName(),
                        MessageSeeds.NO_SUCH_DEVICE_TYPE, deviceType))
                .getConfigurations().stream().filter(config -> config.getName().equals(deviceConfigurationName)
                        || deviceConfigurationName == null && config.isDefault())
                .findAny();
        if (deviceConfiguration.isPresent()) {
            return deviceConfiguration.get();
        }
        if (deviceConfigurationName == null) {
            throw faultMessageFactory.meterConfigFaultMessageSupplier(meter.getDeviceName(),
                    MessageSeeds.NO_DEFAULT_DEVICE_CONFIGURATION).get();
        }
        throw faultMessageFactory.meterConfigFaultMessageSupplier(meter.getDeviceName(),
                MessageSeeds.NO_SUCH_DEVICE_CONFIGURATION, deviceConfigurationName).get();
    }

    public boolean isActionForState(AuthorizedTransitionAction authorizedTransitionAction, String state) {
        Optional<DefaultState> defaultState = DefaultState
                .from(authorizedTransitionAction.getStateTransition().getTo());
        return defaultState.isPresent() && defaultState.get().getDefaultFormat().equals(state);
    }

    private Device updateDevice(Device device) throws FaultMessage {
        long id = device.getId();
        if (getExistingDevices(device.getName(), device.getSerialNumber()).stream().noneMatch(e -> e.getId() != id)) {
            device.save();
        } else {
            throw faultMessageFactory
                    .meterConfigFaultMessageSupplier(device.getName(), MessageSeeds.NAME_MUST_BE_UNIQUE).get();
        }
        return device;
    }

    private List<Device> getExistingDevices(String name, String serialNumber) {
        Condition condition = Where.where("name").isEqualTo(name);
        if (serialNumber != null) {
            condition = condition.or(Where.where("serialNumber").isEqualTo(serialNumber));
        }
        return deviceService.findAllDevices(condition).paged(0, 10).find();
    }
}