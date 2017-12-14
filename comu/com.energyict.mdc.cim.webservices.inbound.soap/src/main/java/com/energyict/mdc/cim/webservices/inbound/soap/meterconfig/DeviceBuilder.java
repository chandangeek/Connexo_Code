/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import ch.iec.tc57._2011.meterconfig.ConfigurationEvent;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import ch.iec.tc57._2011.meterconfig.Status;
import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import ch.iec.tc57._2011.meterconfig.EndDeviceInfo;
import ch.iec.tc57._2011.meterconfig.LifecycleDate;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfig.MeterMultiplier;
import ch.iec.tc57._2011.meterconfig.Name;
import ch.iec.tc57._2011.meterconfig.ProductAssetModel;
import ch.iec.tc57._2011.meterconfig.SimpleEndDeviceFunction;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import javax.inject.Inject;
import java.math.BigDecimal;
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
    private final MeterConfigFaultMessageFactory faultMessageFactory;

    @Inject
    DeviceBuilder(DeviceLifeCycleService deviceLifeCycleService, DeviceConfigurationService deviceConfigurationService,
                  DeviceService deviceService, MeterConfigFaultMessageFactory faultMessageFactory) {
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceService = deviceService;
        this.faultMessageFactory = faultMessageFactory;
    }

    PreparedDeviceBuilder prepareCreateFrom(Meter meter, MeterConfig meterConfig) throws FaultMessage {
        String deviceName = extractDeviceNameOrThrowException(meter);
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

    PreparedDeviceBuilder prepareUpdateFrom(Meter meter) throws FaultMessage {
        String deviceName = extractDeviceNameOrThrowException(meter);
        Optional<String> deviceNewName = extractNewDeviceName(meter);
        Optional<String> serialNumber = extractSerialNumber(meter);
        Optional<String> manufacturer = extractManufacturer(meter);
        Optional<String> modelNumber = extractModelNumber(meter);
        Optional<String> modelVersion = extractModelVersion(meter);
        Optional<BigDecimal> multiplier = extractMultiplier(meter);
        Optional<Instant> effectiveDate = extractEffectiveDate(meter);
        Optional<String> deviceNewStatus = extractNewDeviceStatus(meter);
        return () -> {
            Device updatedDevice = deviceService.findDeviceByName(deviceName)
                    .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.NO_METER_WITH_NAME, deviceName));

            deviceNewName.ifPresent(updatedDevice::setName);
            serialNumber.ifPresent(updatedDevice::setSerialNumber);
            manufacturer.ifPresent(updatedDevice::setManufacturer);
            modelNumber.ifPresent(updatedDevice::setModelNumber);
            modelVersion.ifPresent(updatedDevice::setModelVersion);
            effectiveDate.ifPresent(ed -> multiplier.ifPresent(m -> updatedDevice.setMultiplier(m, ed)));
            updatedDevice.save();

            effectiveDate.ifPresent(ed -> deviceNewStatus.ifPresent(s -> {
                deviceLifeCycleService.getExecutableActions(updatedDevice)
                        .stream()
                        .filter(action -> action.getAction() instanceof AuthorizedTransitionAction)
                        .filter(action -> isActionForState((AuthorizedTransitionAction) action.getAction(), s))
                        .findFirst()
                        .ifPresent(action -> action.execute(ed, Collections.emptyList()));
            }));
            updatedDevice.save();

            return updatedDevice;
        };
    }

    @FunctionalInterface
    interface PreparedDeviceBuilder {

        @TransactionRequired
        Device build() throws FaultMessage;
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

    private String extractDeviceNameOrThrowException(Meter meter) throws FaultMessage {
        return Stream.of(extractName(meter.getNames()), extractSerialNumber(meter), extractMrid(meter))
                .flatMap(Functions.asStream())
                .findFirst()
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(MessageSeeds.DEVICE_IDENTIFIER_MISSING));
    }

    private Optional<String> extractName(List<Name> names) {
        return names.stream()
                .map(Name::getName)
                .filter(name -> !Checks.is(name).emptyOrOnlyWhiteSpace())
                .findFirst();
    }

    private Optional<String> extractSerialNumber(Meter meter) {
        return Optional.ofNullable(meter.getSerialNumber()).filter(serialNumber -> !Checks.is(serialNumber).emptyOrOnlyWhiteSpace());
    }

    private Optional<String> extractMrid(Meter meter) {
        return Optional.ofNullable(meter.getMRID()).filter(mrid -> !Checks.is(mrid).emptyOrOnlyWhiteSpace());
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

    private Optional<ConfigurationEvent> extractConfigurationEvent(Meter meter) {
        return Optional.ofNullable(meter.getConfigurationEvents());
    }

    private Optional<Instant> extractEffectiveDate(Meter meter) throws FaultMessage {
        return extractConfigurationEvent(meter)
                .map(ConfigurationEvent::getEffectiveDateTime);
    }

    private Optional<String> extractNewDeviceName(Meter meter) throws FaultMessage {
        return extractConfigurationEvent(meter)
                .map(ConfigurationEvent::getNames)
                .flatMap(this::extractName);
    }

    private Optional<String> extractNewDeviceStatus(Meter meter) throws FaultMessage {
        return extractConfigurationEvent(meter)
                .map(ConfigurationEvent::getStatus)
                .map(Status::getValue)
                .filter(value -> !Checks.is(value).emptyOrOnlyWhiteSpace());
    }

    private boolean isActionForState(AuthorizedTransitionAction authorizedTransitionAction, String state) {
        Optional<DefaultState> defaultState = DefaultState.from(authorizedTransitionAction.getStateTransition().getTo());
        return defaultState.isPresent() && defaultState.get().getDefaultFormat().equals(state);
    }
}