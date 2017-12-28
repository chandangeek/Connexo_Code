/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import ch.iec.tc57._2011.meterconfig.EndDeviceInfo;
import ch.iec.tc57._2011.meterconfig.LifecycleDate;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfig.MeterMultiplier;
import ch.iec.tc57._2011.meterconfig.Name;
import ch.iec.tc57._2011.meterconfig.ConfigurationEvent;
import ch.iec.tc57._2011.meterconfig.ProductAssetModel;
import ch.iec.tc57._2011.meterconfig.Status;
import ch.iec.tc57._2011.meterconfig.SimpleEndDeviceFunction;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

class DeviceBuilder {

    private final DeviceLifeCycleService deviceLifeCycleService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceService deviceService;
    private final BatchService batchService;
    private final Clock clock;
    private final MeterConfigFaultMessageFactory faultMessageFactory;

    @Inject
    DeviceBuilder(DeviceLifeCycleService deviceLifeCycleService, DeviceConfigurationService deviceConfigurationService,
                  DeviceService deviceService, BatchService batchService, Clock clock, MeterConfigFaultMessageFactory faultMessageFactory) {
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceService = deviceService;
        this.batchService = batchService;
        this.clock = clock;
        this.faultMessageFactory = faultMessageFactory;
    }

    PreparedDeviceBuilder prepareCreateFrom(Meter meter, MeterConfig meterConfig) throws FaultMessage {
        String deviceName = extractDeviceNameForCreateOrThrowException(meter);
        DeviceConfiguration deviceConfig = extractDeviceConfigOrThrowException(meter, meterConfig.getSimpleEndDeviceFunction());
        Instant shipmentDate = extractShipmentDateOrThrowException(meter);
        Optional<String> batch = extractBatch(meter);
        Optional<String> serialNumber = extractSerialNumber(meter);
        Optional<String> manufacturer = extractManufacturer(meter);
        Optional<String> modelNumber = extractModelNumber(meter);
        Optional<String> modelVersion = extractModelVersion(meter);
        Optional<BigDecimal> multiplier = extractMultiplier(meter);
        return () -> {
            Device createdDevice = batch.isPresent() ?
                    deviceService.newDevice(deviceConfig, deviceName, batch.get(), shipmentDate) :
                    deviceService.newDevice(deviceConfig, deviceName, shipmentDate);
            serialNumber.ifPresent(createdDevice::setSerialNumber);
            manufacturer.ifPresent(createdDevice::setManufacturer);
            modelNumber.ifPresent(createdDevice::setModelNumber);
            modelVersion.ifPresent(createdDevice::setModelVersion);
            multiplier.ifPresent(m -> createdDevice.setMultiplier(m, shipmentDate));
            createdDevice.save();
            return createdDevice;
        };
    }

    PreparedDeviceBuilder prepareChangeFrom(Meter meter) throws FaultMessage {
        String deviceName = extractDeviceNameForUpdateOrThrowException(meter);
        Optional<String> mrid = extractMrid(meter);
        Optional<String> batch = extractBatch(meter);
        Optional<String> serialNumber = extractSerialNumber(meter);
        Optional<String> manufacturer = extractManufacturer(meter);
        Optional<String> modelNumber = extractModelNumber(meter);
        Optional<String> modelVersion = extractModelVersion(meter);
        Optional<BigDecimal> multiplier = extractMultiplier(meter);
        Optional<String> statusReason = extractStatusReason(meter);
        Optional<String> statusValue = extractStatusValue(meter);
        Optional<Instant> statusEffectiveDate = extractStatusEffectiveDate(meter);
        Optional<String> multiplierReason = extractConfigurationReason(meter);
        Optional<Instant> multiplierEffectiveDate = extractConfigurationEffectiveDate(meter);
        return () -> {
            Device changedDevice = mrid.isPresent() ? findDeviceByMRID(mrid.get()) :
                    deviceService.findDeviceByName(deviceName)
                            .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.NO_DEVICE_WITH_NAME, deviceName));
            if (statusReason.isPresent()) {
                EventReason.forReason(statusReason.get())
                        .filter(EventReason.CHANGE_STATUS::equals)
                        .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.NOT_VALID_STATUS_REASON, statusReason.get()));
                String state = statusValue.orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT, "MeterConfig.Meter[0].status.value"));
                Instant effectiveDate = statusEffectiveDate.orElse(clock.instant());
                ExecutableAction executableAction = deviceLifeCycleService.getExecutableActions(changedDevice)
                        .stream()
                        .filter(action -> action.getAction() instanceof AuthorizedTransitionAction)
                        .filter(action -> isActionForState((AuthorizedTransitionAction) action.getAction(), state))
                        .findFirst()
                        .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.UNABLE_TO_CHANGE_DEVICE_STATE, statusValue.orElse("")));
                executableAction.execute(effectiveDate, Collections.emptyList());
                changedDevice.save();
                changedDevice = findDeviceByMRID(changedDevice.getmRID());
            }

            if (multiplierReason.isPresent()) {
                EventReason.forReason(multiplierReason.get())
                        .filter(EventReason.CHANGE_MULTIPLIER::equals)
                        .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.NOT_VALID_MULTIPLIER_REASON, multiplierReason.get()));
                changedDevice.setMultiplier(multiplier.orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT, "MeterConfig.Meter[0].multiplier")),
                        multiplierEffectiveDate.orElse(clock.instant()));
            }

            if (mrid.isPresent()) {
                changedDevice.setName(deviceName);
            }

            if (batch.isPresent()) {
                batchService.findOrCreateBatch(batch.get()).addDevice(changedDevice);
            }

            serialNumber.ifPresent(changedDevice::setSerialNumber);
            manufacturer.ifPresent(changedDevice::setManufacturer);
            modelNumber.ifPresent(changedDevice::setModelNumber);
            modelVersion.ifPresent(changedDevice::setModelVersion);
            changedDevice.save();

            return changedDevice;
        };
    }

    @FunctionalInterface
    interface PreparedDeviceBuilder {

        @TransactionRequired
        Device build() throws FaultMessage;
    }

    private Device findDeviceByMRID(String mrid) throws FaultMessage {
        return deviceService.findDeviceByMrid(mrid)
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.NO_DEVICE_WITH_MRID, mrid));
    }

    private DeviceConfiguration extractDeviceConfigOrThrowException(Meter meter, List<SimpleEndDeviceFunction> endDeviceFunctions) throws FaultMessage {
        DeviceType deviceType = extractDeviceTypeOrThrowException(meter);
        String comFuncReference = extractEndDeviceFunctionRef(meter);
        SimpleEndDeviceFunction endDeviceFunction = endDeviceFunctions
                .stream()
                .filter(endDeviceFunc -> comFuncReference.equals(endDeviceFunc.getMRID()))
                .findAny()
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.ELEMENT_BY_REFERENCE_NOT_FOUND,
                        "MeterConfig.Meter[0].SimpleEndDeviceFunction", "MeterConfig.SimpleEndDeviceFunction"));
        String deviceConfigurationName = Optional.ofNullable(endDeviceFunction.getConfigID())
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT,
                        "MeterConfig.SimpleEndDeviceFunction[" + endDeviceFunctions.indexOf(endDeviceFunction) + "].configID"));
        return deviceType.getConfigurations()
                .stream()
                .filter(config -> deviceConfigurationName.equals(config.getName()))
                .findAny()
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.NO_SUCH_DEVICE_CONFIGURATION, deviceConfigurationName));
    }

    private DeviceType extractDeviceTypeOrThrowException(Meter meter) throws FaultMessage {
        String deviceTypeName = extractDeviceTypeName(meter);
        return deviceConfigurationService.findDeviceTypeByName(deviceTypeName)
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.NO_SUCH_DEVICE_TYPE, deviceTypeName));
    }

    private String extractDeviceNameForCreateOrThrowException(Meter meter) throws FaultMessage {
        return Stream.of(extractName(meter.getNames()), extractSerialNumber(meter), extractMrid(meter))
                .flatMap(Functions.asStream())
                .findFirst()
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.CREATE_DEVICE_IDENTIFIER_MISSING));
    }

    private String extractDeviceNameForUpdateOrThrowException(Meter meter) throws FaultMessage {
        return extractName(meter.getNames())
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.CHANGE_DEVICE_IDENTIFIER_MISSING));
    }

    private Optional<String> extractName(List<Name> names) {
        return names.stream()
                .map(Name::getName)
                .filter(name -> !Checks.is(name).emptyOrOnlyWhiteSpace())
                .findFirst();
    }

    private Optional<String> extractSerialNumber(Meter meter) {
        return Optional.ofNullable(meter.getSerialNumber())
                .filter(serialNumber -> !Checks.is(serialNumber).emptyOrOnlyWhiteSpace());
    }

    private Optional<String> extractMrid(Meter meter) {
        return Optional.ofNullable(meter.getMRID())
                .filter(mrid -> !Checks.is(mrid).emptyOrOnlyWhiteSpace());
    }

    private String extractDeviceTypeName(Meter meter) throws FaultMessage {
        return Optional.ofNullable(meter.getType())
                .filter(deviceTypeName -> !Checks.is(deviceTypeName).emptyOrOnlyWhiteSpace())
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT, "MeterConfig.Meter[0].type"));
    }

    private String extractEndDeviceFunctionRef(Meter meter) throws FaultMessage {
        return meter.getComFunctionOrConnectDisconnectFunctionOrSimpleEndDeviceFunction()
                .stream()
                .filter(Meter.SimpleEndDeviceFunction.class::isInstance)
                .map(Meter.SimpleEndDeviceFunction.class::cast)
                .map(Meter.SimpleEndDeviceFunction::getRef)
                .filter(ref -> !Checks.is(ref).emptyOrOnlyWhiteSpace())
                .findFirst()
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT, "MeterConfig.Meter[0].SimpleEndDeviceFunction.ref"));
    }

    private Instant extractShipmentDateOrThrowException(Meter meter) throws FaultMessage {
        return Optional.ofNullable(meter.getLifecycle())
                .map(LifecycleDate::getReceivedDate)
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.MISSING_ELEMENT, "MeterConfig.Meter[0].lifecycle.receivedDate"));
    }

    private Optional<String> extractBatch(Meter meter) {
        return Optional.ofNullable(meter.getLotNumber())
                .filter(lotNumber -> !Checks.is(lotNumber).emptyOrOnlyWhiteSpace());
    }

    private Optional<String> extractManufacturer(Meter meter) {
        return Optional.ofNullable(meter.getEndDeviceInfo())
                .flatMap(endDeviceInfo -> Optional.ofNullable(endDeviceInfo.getAssetModel()))
                .flatMap(productAssetModel -> Optional.ofNullable(productAssetModel.getManufacturer()))
                .flatMap(manufacturer -> extractName(manufacturer.getNames()));
    }

    private Optional<String> extractModelNumber(Meter meter) {
        return Optional.ofNullable(meter.getEndDeviceInfo())
                .map(EndDeviceInfo::getAssetModel)
                .map(ProductAssetModel::getModelNumber)
                .filter(modelNumber -> !Checks.is(modelNumber).emptyOrOnlyWhiteSpace());
    }

    private Optional<String> extractModelVersion(Meter meter) {
        return Optional.ofNullable(meter.getEndDeviceInfo())
                .map(EndDeviceInfo::getAssetModel)
                .map(ProductAssetModel::getModelVersion)
                .filter(modelVersion -> !Checks.is(modelVersion).emptyOrOnlyWhiteSpace());
    }

    private Optional<BigDecimal> extractMultiplier(Meter meter) {
        return meter.getMeterMultipliers()
                .stream()
                .map(MeterMultiplier::getValue)
                .filter(Objects::nonNull)
                .findFirst()
                .map(BigDecimal::valueOf);
    }

    private Optional<Status> extractMeterStatus(Meter meter) {
        return Optional.ofNullable(meter.getStatus());
    }

    private Optional<String> extractStatusReason(Meter meter) throws FaultMessage {
        return extractMeterStatus(meter)
                .map(Status::getReason)
                .filter(value -> !Checks.is(value).emptyOrOnlyWhiteSpace());
    }

    private Optional<String> extractStatusValue(Meter meter) throws FaultMessage {
        return extractMeterStatus(meter)
                .map(Status::getValue)
                .filter(value -> !Checks.is(value).emptyOrOnlyWhiteSpace());
    }

    private Optional<Instant> extractStatusEffectiveDate(Meter meter) throws FaultMessage {
        return extractMeterStatus(meter)
                .map(Status::getDateTime);
    }

    private Optional<ConfigurationEvent> extractConfigurationEvent(Meter meter) {
        return Optional.ofNullable(meter.getConfigurationEvents());
    }

    private Optional<String> extractConfigurationReason(Meter meter) throws FaultMessage {
        return extractConfigurationEvent(meter)
                .map(ConfigurationEvent::getReason)
                .filter(value -> !Checks.is(value).emptyOrOnlyWhiteSpace());
    }

    private Optional<Instant> extractConfigurationEffectiveDate(Meter meter) throws FaultMessage {
        return extractConfigurationEvent(meter)
                .map(ConfigurationEvent::getEffectiveDateTime);
    }

    private boolean isActionForState(AuthorizedTransitionAction authorizedTransitionAction, String state) {
        Optional<DefaultState> defaultState = DefaultState.from(authorizedTransitionAction.getStateTransition().getTo());
        return defaultState.isPresent() && defaultState.get().getDefaultFormat().equals(state);
    }
}