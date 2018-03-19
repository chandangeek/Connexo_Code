/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.Name;
import com.elster.jupiter.orm.TransactionRequired;
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
    public DeviceBuilder(BatchService batchService, Clock clock,
                         DeviceLifeCycleService deviceLifeCycleService, DeviceConfigurationService deviceConfigurationService,
                         DeviceService deviceService, MeterConfigFaultMessageFactory faultMessageFactory) {
        this.batchService = batchService;
        this.clock = clock;
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceService = deviceService;
        this.faultMessageFactory = faultMessageFactory;
    }

    public PreparedDeviceBuilder prepareCreateFrom(MeterInfo meter) throws FaultMessage {
        DeviceConfiguration deviceConfig = findDeviceConfiguration(meter, meter.getDeviceConfigurationName(), meter.getDeviceType());
        Optional<String> batch = Optional.ofNullable(meter.getBatch());
        Optional<String> serialNumber = Optional.ofNullable(meter.getSerialNumber());
        Optional<String> manufacturer = Optional.ofNullable(meter.getManufacturer());
        Optional<String> modelNumber = Optional.ofNullable(meter.getModelNumber());
        Optional<String> modelVersion = Optional.ofNullable(meter.getModelVersion());
        Optional<BigDecimal> multiplier = Optional.ofNullable(meter.getMultiplier());

        return () -> {
            Device createdDevice = batch.isPresent() ?
                    deviceService.newDevice(deviceConfig, meter.getDeviceName(), batch.get(), meter.getShipmentDate()) :
                    deviceService.newDevice(deviceConfig, meter.getDeviceName(), meter.getShipmentDate());
            serialNumber.ifPresent(createdDevice::setSerialNumber);
            manufacturer.ifPresent(createdDevice::setManufacturer);
            modelNumber.ifPresent(createdDevice::setModelNumber);
            modelVersion.ifPresent(createdDevice::setModelVersion);
            multiplier.ifPresent(m -> createdDevice.setMultiplier(m, meter.getShipmentDate()));
            createdDevice.save();
            return createdDevice;
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
        Optional<String> statusReason = Optional.ofNullable(meter.getStatusReason());
        Optional<String> statusValue = Optional.ofNullable(meter.getStatusValue());
        Optional<Instant> statusEffectiveDate = Optional.ofNullable(meter.getStatusEffectiveDate());
        Optional<String> multiplierReason = Optional.ofNullable(meter.getMultiplierReason());
        Optional<Instant> multiplierEffectiveDate = Optional.ofNullable(meter.getMultiplierEffectiveDate());

        return () -> {
            Device changedDevice = mrid.isPresent() ? findDeviceByMRID(meter, mrid.get()) :
                    deviceService.findDeviceByName(meter.getDeviceName())
                            .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(meter.getDeviceName(), MessageSeeds.NO_DEVICE_WITH_NAME, meter.getDeviceName()));

            String currentModelNumber = changedDevice.getModelNumber();
            String currentModelVersion = changedDevice.getModelVersion();
            String currentManufacturer = changedDevice.getManufacturer();

            if (statusReason.isPresent()) {
                EventReason.forReason(statusReason.get())
                        .filter(EventReason.CHANGE_STATUS::equals)
                        .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(meter.getDeviceName(), MessageSeeds.NOT_VALID_STATUS_REASON, statusReason
                                .get()));
                String state = statusValue.orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(meter.getDeviceName(), MessageSeeds.MISSING_ELEMENT, METER_CONFIG_STATUS_ITEM));
                Instant effectiveDate = statusEffectiveDate.orElse(clock.instant());
                ExecutableAction executableAction = deviceLifeCycleService.getExecutableActions(changedDevice)
                        .stream()
                        .filter(action -> action.getAction() instanceof AuthorizedTransitionAction)
                        .filter(action -> isActionForState((AuthorizedTransitionAction) action.getAction(), state))
                        .findFirst()
                        .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(meter.getDeviceName(), MessageSeeds.UNABLE_TO_CHANGE_DEVICE_STATE, statusValue
                                .orElse("")));
                executableAction.execute(effectiveDate, Collections.emptyList());
                changedDevice.save();
                changedDevice = findDeviceByMRID(meter, changedDevice.getmRID());
            }

            if (multiplierReason.isPresent()) {
                EventReason.forReason(multiplierReason.get())
                        .filter(EventReason.CHANGE_MULTIPLIER::equals)
                        .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(meter.getDeviceName(), MessageSeeds.NOT_VALID_MULTIPLIER_REASON, multiplierReason
                                .get()));
                changedDevice.setMultiplier(multiplier.orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(meter.getDeviceName(), MessageSeeds.MISSING_ELEMENT, METER_CONFIG_MULTIPLIER_ITEM)),
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
            changedDevice.save();

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
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(meter.getDeviceName(), MessageSeeds.NO_DEVICE_WITH_MRID, mrid));
    }

    private DeviceConfiguration findDeviceConfiguration(MeterInfo meter, String deviceConfigurationName, String deviceType) throws
            FaultMessage {
        return deviceConfigurationService.findDeviceTypeByName(deviceType)
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(meter.getDeviceName(), MessageSeeds.NO_SUCH_DEVICE_TYPE, deviceType))
                .getConfigurations()
                .stream()
                .filter(config -> deviceConfigurationName.equals(config.getName()))
                .findAny()
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(meter.getDeviceName(), MessageSeeds.NO_SUCH_DEVICE_CONFIGURATION,
                        deviceConfigurationName));
    }

    public boolean isActionForState(AuthorizedTransitionAction authorizedTransitionAction, String state) {
        Optional<DefaultState> defaultState = DefaultState.from(authorizedTransitionAction.getStateTransition().getTo());
        return defaultState.isPresent() && defaultState.get().getDefaultFormat().equals(state);
    }
}