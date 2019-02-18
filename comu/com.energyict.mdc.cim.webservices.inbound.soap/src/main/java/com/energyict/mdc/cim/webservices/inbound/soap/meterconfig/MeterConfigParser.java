/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import ch.iec.tc57._2011.meterconfig.ConfigurationEvent;
import ch.iec.tc57._2011.meterconfig.EndDeviceInfo;
import ch.iec.tc57._2011.meterconfig.LifecycleDate;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterMultiplier;
import ch.iec.tc57._2011.meterconfig.Name;
import ch.iec.tc57._2011.meterconfig.ProductAssetModel;
import ch.iec.tc57._2011.meterconfig.SimpleEndDeviceFunction;
import ch.iec.tc57._2011.meterconfig.Status;
import ch.iec.tc57._2011.meterconfig.Zone;

import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.cim.webservices.inbound.soap.MeterInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.OperationEnum;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/*
 * Uses for parsing ch.iec.tc57._2011.meterconfig
 */
public class MeterConfigParser {
    private final MeterConfigFaultMessageFactory faultMessageFactory;

    @Inject
    public MeterConfigParser(MeterConfigFaultMessageFactory faultMessageFactory) {
        this.faultMessageFactory = faultMessageFactory;
    }

    public MeterInfo asMeterInfo(Meter meter, List<SimpleEndDeviceFunction> endDeviceFunctions, OperationEnum operationEnum) throws FaultMessage {
        MeterInfo meterInfo = new MeterInfo();

        switch (operationEnum) {
            case CREATE:
                meterInfo.setDeviceName(extractDeviceNameForCreate(meter));
                meterInfo.setDeviceConfigurationName(extractDeviceConfig(meter, endDeviceFunctions));
                meterInfo.setShipmentDate(extractShipmentDate(meter));
                meterInfo.setDeviceType(extractDeviceTypeName(meter));
                meterInfo.setZones(extractDeviceZones(meter, endDeviceFunctions));
                break;
            case UPDATE:
                meterInfo.setDeviceName(extractDeviceNameForUpdate(meter));
                meterInfo.setmRID(extractMrid(meter).orElse(null));
                meterInfo.setConfigurationEventReason(extractConfigurationReason(meter).orElse(null));
                meterInfo.setStatusValue(extractStatusValue(meter).orElse(null));
                meterInfo.setStatusEffectiveDate(extractConfigurationEffectiveDate(meter).orElse(null));
                meterInfo.setMultiplierEffectiveDate(extractConfigurationEffectiveDate(meter).orElse(null));
                meterInfo.setZones(extractDeviceZones(meter, endDeviceFunctions));
                break;
        }

        meterInfo.setBatch(extractBatch(meter).orElse(null));
        meterInfo.setSerialNumber(extractSerialNumber(meter).orElse(null));
        meterInfo.setManufacturer(extractManufacturer(meter).orElse(null));
        meterInfo.setModelNumber(extractModelNumber(meter).orElse(null));
        meterInfo.setModelVersion(extractModelVersion(meter).orElse(null));
        meterInfo.setMultiplier(extractMultiplier(meter).orElse(null));
        meterInfo.setElectronicAddress(meter.getElectronicAddress());
        return meterInfo;
    }

    public Optional<String> extractMrid(Meter meter) {
        return Optional.ofNullable(meter.getMRID())
                .filter(mrid -> !Checks.is(mrid).emptyOrOnlyWhiteSpace());
    }

    public Optional<String> extractSerialNumber(Meter meter) {
        return Optional.ofNullable(meter.getSerialNumber())
                .filter(serialNumber -> !Checks.is(serialNumber).emptyOrOnlyWhiteSpace());
    }

    public Optional<String> extractName(List<Name> names) {
        return names.stream()
                .map(Name::getName)
                .filter(name -> !Checks.is(name).emptyOrOnlyWhiteSpace())
                .findFirst();
    }

    public Optional<ProductAssetModel> extractAssetModel(Meter meter) {
        return extractEndDeviceInfo(meter)
                .map(EndDeviceInfo::getAssetModel);
    }

    public Optional<String> extractManufacturer(Meter meter) {
        return extractAssetModel(meter)
                .map(ProductAssetModel::getManufacturer)
                .flatMap(manufacturer -> extractName(manufacturer.getNames()));
    }

    public Optional<String> extractModelNumber(Meter meter) {
        return extractAssetModel(meter)
                .map(ProductAssetModel::getModelNumber)
                .filter(modelNumber -> !Checks.is(modelNumber).emptyOrOnlyWhiteSpace());
    }

    public Optional<String> extractModelVersion(Meter meter) {
        return extractAssetModel(meter)
                .map(ProductAssetModel::getModelVersion)
                .filter(modelVersion -> !Checks.is(modelVersion).emptyOrOnlyWhiteSpace());
    }

    public Optional<BigDecimal> extractMultiplier(Meter meter) {
        return meter.getMeterMultipliers()
                .stream()
                .map(MeterMultiplier::getValue)
                .filter(Objects::nonNull)
                .findFirst()
                .map(BigDecimal::valueOf);
    }

    public Optional<Status> extractMeterStatus(Meter meter) {
        return Optional.ofNullable(meter.getStatus());
    }

    public Optional<String> extractStatusReason(Meter meter) {
        return extractMeterStatus(meter)
                .map(Status::getReason)
                .filter(value -> !Checks.is(value).emptyOrOnlyWhiteSpace());

    }

    public Optional<String> extractStatusValue(Meter meter) {
        return extractMeterStatus(meter)
                .map(Status::getValue)
                .filter(value -> !Checks.is(value).emptyOrOnlyWhiteSpace());
    }

    public Optional<Instant> extractStatusEffectiveDate(Meter meter) {
        return extractMeterStatus(meter)
                .map(Status::getDateTime);
    }

    public String extractDeviceConfig(Meter meter, List<SimpleEndDeviceFunction> endDeviceFunctions) throws FaultMessage {
        String comFuncReference = extractEndDeviceFunctionRef(meter);
        SimpleEndDeviceFunction endDeviceFunction = endDeviceFunctions
                .stream()
                .filter(endDeviceFunc -> comFuncReference.equals(endDeviceFunc.getMRID()))
                .findAny()
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter), MessageSeeds.ELEMENT_BY_REFERENCE_NOT_FOUND,
                        "MeterConfig.Meter.SimpleEndDeviceFunction", "MeterConfig.SimpleEndDeviceFunction"));
        return Optional.ofNullable(endDeviceFunction.getConfigID())
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter), MessageSeeds.MISSING_ELEMENT,
                        "MeterConfig.SimpleEndDeviceFunction[" + endDeviceFunctions.indexOf(endDeviceFunction) + "].configID"));
    }

    public String extractDeviceNameForCreate(Meter meter) throws FaultMessage {
        return Stream.of(extractName(meter.getNames()), extractSerialNumber(meter), extractMrid(meter))
                .flatMap(Functions.asStream())
                .findFirst()
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter), MessageSeeds.CREATE_DEVICE_IDENTIFIER_MISSING));
    }

    public String extractDeviceNameForUpdate(Meter meter) throws FaultMessage {
        return extractName(meter.getNames())
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter), MessageSeeds.CHANGE_DEVICE_IDENTIFIER_MISSING));
    }

    public String extractDeviceTypeName(Meter meter) throws FaultMessage {
        return Optional.ofNullable(meter.getType())
                .filter(deviceTypeName -> !Checks.is(deviceTypeName).emptyOrOnlyWhiteSpace())
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter), MessageSeeds.MISSING_ELEMENT, "MeterConfig.Meter.type"));
    }

    public String extractEndDeviceFunctionRef(Meter meter) throws FaultMessage {
        return meter.getComFunctionOrConnectDisconnectFunctionOrSimpleEndDeviceFunction()
                .stream()
                .filter(Meter.SimpleEndDeviceFunction.class::isInstance)
                .map(Meter.SimpleEndDeviceFunction.class::cast)
                .map(Meter.SimpleEndDeviceFunction::getRef)
                .filter(ref -> !Checks.is(ref).emptyOrOnlyWhiteSpace())
                .findFirst()
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter), MessageSeeds.MISSING_ELEMENT, "MeterConfig.Meter.SimpleEndDeviceFunction.ref"));
    }

    public Instant extractShipmentDate(Meter meter) throws FaultMessage {
        return Optional.ofNullable(meter.getLifecycle())
                .map(LifecycleDate::getReceivedDate)
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter), MessageSeeds.MISSING_ELEMENT, "MeterConfig.Meter.lifecycle.receivedDate"));
    }

    public Optional<String> extractBatch(Meter meter) {
        return Optional.ofNullable(meter.getLotNumber())
                .filter(lotNumber -> !Checks.is(lotNumber).emptyOrOnlyWhiteSpace());
    }

    public Optional<String> extractConfigurationReason(Meter meter) throws FaultMessage {
        return extractConfigurationEvent(meter)
                .map(ConfigurationEvent::getReason)
                .filter(value -> !Checks.is(value).emptyOrOnlyWhiteSpace());
    }

    public Optional<Instant> extractConfigurationEffectiveDate(Meter meter) throws FaultMessage {
        return extractConfigurationEvent(meter)
                .map(ConfigurationEvent::getEffectiveDateTime);
    }

    public List<Zone> extractDeviceZones(Meter meter, List<SimpleEndDeviceFunction> endDeviceFunctions) throws FaultMessage {
        String comFuncReference = extractEndDeviceFunctionRef(meter);
        SimpleEndDeviceFunction endDeviceFunction = endDeviceFunctions
                .stream()
                .filter(endDeviceFunc -> comFuncReference.equals(endDeviceFunc.getMRID()))
                .findAny()
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter), MessageSeeds.ELEMENT_BY_REFERENCE_NOT_FOUND,
                        "MeterConfig.Meter.SimpleEndDeviceFunction", "MeterConfig.SimpleEndDeviceFunction"));

        if(endDeviceFunction.getZones() !=  null) {
            endDeviceFunction.getZones().getZone()
                    .stream()
                    .findAny()
                    .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter), MessageSeeds.EMPTY_LIST,
                            "MeterConfig.SimpleEndDeviceFunction[" + endDeviceFunctions.indexOf(endDeviceFunction) + "].Zones"));

            if(endDeviceFunction.getZones().getZone().size() != endDeviceFunction.getZones().getZone().stream().map(Zone::getZoneType).distinct().count())
               throw faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter), MessageSeeds.IS_NOT_ALLOWED_TO_HAVE_DUPLICATED_ZONE_TYPES).get();

            if(endDeviceFunction.getZones().getZone().stream().filter(zone->zone.getZoneName() == null || zone.getZoneName().isEmpty()).findAny().isPresent())
                throw faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter), MessageSeeds.ELEMENT_BY_REFERENCE_NOT_FOUND_OR_EMPTY,
                        "MeterConfig.Meter.SimpleEndDeviceFunction.Zones.Zone.zoneName").get();

            if(endDeviceFunction.getZones().getZone().stream().filter(zone->zone.getZoneType() == null || zone.getZoneType().isEmpty()).findAny().isPresent())
                throw faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter), MessageSeeds.ELEMENT_BY_REFERENCE_NOT_FOUND_OR_EMPTY,
                        "MeterConfig.Meter.SimpleEndDeviceFunction.Zones.Zone.zoneType").get();

            return endDeviceFunction.getZones().getZone();
        }

        return new ArrayList<>();
    }

    private Optional<EndDeviceInfo> extractEndDeviceInfo(Meter meter) {
        return Optional.ofNullable(meter.getEndDeviceInfo());
    }

    private Optional<ConfigurationEvent> extractConfigurationEvent(Meter meter) {
        return Optional.ofNullable(meter.getConfigurationEvents());
    }

    private String getMeterName(Meter meter){
        return meter.getNames().stream().findFirst().map(Name::getName).orElse(null);
    }
}
