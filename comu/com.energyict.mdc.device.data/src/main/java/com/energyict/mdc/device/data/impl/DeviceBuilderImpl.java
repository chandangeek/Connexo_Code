package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public class DeviceBuilderImpl implements DeviceBuilder {

    private DeviceConfiguration deviceConfiguration;
    private String name;
    private Instant startDate;
    private String batch;
    private String serialNumber;
    private String manufacturer;
    private String modelNumber;
    private String modelVersion;
    private BigDecimal multiplier;
    private Integer yearOfCertification;

    private DeviceDataModelService deviceDataModelService;

    public DeviceBuilderImpl(DeviceConfiguration deviceConfiguration, String name, Instant startDate, DeviceDataModelService deviceDataModelService) {
        this.deviceConfiguration = deviceConfiguration;
        this.name = name;
        this.startDate = startDate;
        this.deviceDataModelService = deviceDataModelService;
    }

    @Override
    public DeviceBuilder withBatch(String batch) {
        this.batch = batch;
        return this;
    }

    @Override
    public DeviceBuilder withSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
        return this;
    }

    @Override
    public DeviceBuilder withManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
        return this;
    }

    @Override
    public DeviceBuilder withModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
        return this;
    }

    @Override
    public DeviceBuilder withModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        return this;
    }

    @Override
    public DeviceBuilder withMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
        return this;
    }

    @Override
    public DeviceBuilder withYearOfCertification(Integer yearOfCertification) {
        this.yearOfCertification = yearOfCertification;
        return this;
    }

    @Override
    public Device create() {
        Device device = this.deviceDataModelService.dataModel()
                .getInstance(DeviceImpl.class)
                .initialize(deviceConfiguration, name, startDate);
        Optional.ofNullable(serialNumber).ifPresent(device::setSerialNumber);
        Optional.ofNullable(manufacturer).ifPresent(device::setManufacturer);
        Optional.ofNullable(modelNumber).ifPresent(device::setModelNumber);
        Optional.ofNullable(modelVersion).ifPresent(device::setModelVersion);
        Optional.ofNullable(multiplier).ifPresent(mul -> device.setMultiplier(mul, startDate));
        Optional.ofNullable(yearOfCertification).ifPresent(device::setYearOfCertification);
        device.save();
        Optional.ofNullable(batch).ifPresent(b -> this.deviceDataModelService.batchService().findOrCreateBatch(b).addDevice(device));
        return device;
    }
}
