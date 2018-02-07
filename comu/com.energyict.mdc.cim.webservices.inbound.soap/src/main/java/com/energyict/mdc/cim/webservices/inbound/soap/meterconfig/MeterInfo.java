/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.streams.Functions;

import ch.iec.tc57._2011.meterconfig.EndDeviceInfo;
import ch.iec.tc57._2011.meterconfig.LifecycleDate;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterMultiplier;
import ch.iec.tc57._2011.meterconfig.Name;
import ch.iec.tc57._2011.meterconfig.ProductAssetModel;
import ch.iec.tc57._2011.meterconfig.SimpleEndDeviceFunction;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class MeterInfo {
    private String deviceName;
    private String mRID;
    private String name;
    private String serialNumber;
    private String batch;
    private String manufacturer;
    private String modelNumber;
    private String modelVersion;
    private BigDecimal multiplier;
    private Instant shipmentDate;
    private String deviceType;
    private String deviceConfigurationName;

    public MeterInfo(){}

    public MeterInfo(Meter meter, List<SimpleEndDeviceFunction> endDeviceFunctions) {
        this.deviceName = extractDeviceName(meter);
        this.mRID = extractMrid(meter).orElse(null);
        this.name = extractName(meter.getNames()).orElse(null);
        this.serialNumber = extractSerialNumber(meter).orElse(null);
        this.batch = Optional.ofNullable(meter.getLotNumber())
                .filter(lotNumber -> !Checks.is(lotNumber).emptyOrOnlyWhiteSpace()).orElse(null);
        this.manufacturer = extractManufacturer(meter).orElse(null);
        this.modelNumber = extractModelNumber(meter).orElse(null);
        this.modelVersion = extractModelVersion(meter).orElse(null);
        this.multiplier = extractMultiplier(meter).orElse(null);
        this.shipmentDate = extractShipmentDate(meter);
        this.deviceConfigurationName = extractDeviceConfig(meter, endDeviceFunctions);
        this.deviceType = extractDeviceTypeName(meter);
    }

    private String extractDeviceName(Meter meter) {
        return Stream.of(extractName(meter.getNames()), extractSerialNumber(meter), extractMrid(meter))
                .flatMap(Functions.asStream())
                .findFirst().orElse(null);
    }

    private Optional<String> extractMrid(Meter meter) {
        return Optional.ofNullable(meter.getMRID())
                .filter(mrid -> !Checks.is(mrid).emptyOrOnlyWhiteSpace());
    }

    private String extractDeviceConfig(Meter meter, List<SimpleEndDeviceFunction> endDeviceFunctions) {
        String comFuncReference = extractEndDeviceFunctionRef(meter);
        SimpleEndDeviceFunction endDeviceFunction = endDeviceFunctions
                .stream()
                .filter(endDeviceFunc -> comFuncReference.equals(endDeviceFunc.getMRID()))
                .findAny()
                .orElse(null);
        return Optional.ofNullable(endDeviceFunction.getConfigID()).orElse(null);
    }

    private String extractEndDeviceFunctionRef(Meter meter) {
        return meter.getComFunctionOrConnectDisconnectFunctionOrSimpleEndDeviceFunction()
                .stream()
                .filter(Meter.SimpleEndDeviceFunction.class::isInstance)
                .map(Meter.SimpleEndDeviceFunction.class::cast)
                .map(Meter.SimpleEndDeviceFunction::getRef)
                .filter(ref -> !Checks.is(ref).emptyOrOnlyWhiteSpace())
                .findFirst()
                .orElse(null);
    }

    private String extractDeviceTypeName(Meter meter) {
        return Optional.ofNullable(meter.getType())
                .filter(deviceTypeName -> !Checks.is(deviceTypeName).emptyOrOnlyWhiteSpace())
                .orElse(null);
    }

    private Instant extractShipmentDate(Meter meter) {
        return Optional.ofNullable(meter.getLifecycle())
                .map(LifecycleDate::getReceivedDate).orElse(null);
    }

    private Optional<String> extractSerialNumber(Meter meter) {
        return Optional.ofNullable(meter.getSerialNumber())
                .filter(serialNumber -> !Checks.is(serialNumber).emptyOrOnlyWhiteSpace());
    }

    private Optional<String> extractName(List<Name> names) {
        return names.stream()
                .map(Name::getName)
                .filter(name -> !Checks.is(name).emptyOrOnlyWhiteSpace())
                .findFirst();
    }

    private Optional<EndDeviceInfo> extractEndDeviceInfo(Meter meter) {
        return Optional.ofNullable(meter.getEndDeviceInfo());
    }

    private Optional<ProductAssetModel> extractAssetModel(Meter meter) {
        return extractEndDeviceInfo(meter)
                .map(EndDeviceInfo::getAssetModel);
    }

    private Optional<String> extractManufacturer(Meter meter) {
        return extractAssetModel(meter)
                .map(ProductAssetModel::getManufacturer)
                .flatMap(manufacturer -> extractName(manufacturer.getNames()));
    }

    private Optional<String> extractModelNumber(Meter meter) {
        return extractAssetModel(meter)
                .map(ProductAssetModel::getModelNumber)
                .filter(modelNumber -> !Checks.is(modelNumber).emptyOrOnlyWhiteSpace());
    }

    private Optional<String> extractModelVersion(Meter meter) {
        return extractAssetModel(meter)
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

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getmRID() {
        return mRID;
    }

    public void setmRID(String mRID) {
        this.mRID = mRID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

    @JsonIgnore
    public Instant getShipmentDate() {
        return this.shipmentDate;
    }

    public void setShipmentDate(Instant time) {
        this.shipmentDate = time;
    }

    @JsonGetter
    private long getEpochTime() {
        return this.shipmentDate.toEpochMilli();
    }

    @JsonSetter
    private void setEpochTime(long time) {
        this.shipmentDate = Instant.ofEpochMilli(time);
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceConfigurationName() {
        return deviceConfigurationName;
    }

    public void setDeviceConfigurationName(String deviceConfigurationName) {
        this.deviceConfigurationName = deviceConfigurationName;
    }
}
