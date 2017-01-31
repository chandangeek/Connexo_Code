/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.shipment;

import com.energyict.mdc.device.data.importers.impl.FileImportRecord;

import java.time.ZonedDateTime;

public class DeviceShipmentImportRecord extends FileImportRecord {

    private String deviceType;
    private String deviceConfiguration;
    private ZonedDateTime shipmentDate;
    private String serialNumber;
    private Integer yearOfCertification;
    private String batch;

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceConfiguration() {
        return deviceConfiguration;
    }

    public void setDeviceConfiguration(String deviceConfiguration) {
        this.deviceConfiguration = deviceConfiguration;
    }

    public ZonedDateTime getShipmentDate() {
        return shipmentDate;
    }

    public void setShipmentDate(ZonedDateTime shipmentDate) {
        this.shipmentDate = shipmentDate;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Integer getYearOfCertification() {
        return yearOfCertification;
    }

    public void setYearOfCertification(Integer yearOfCertification) {
        this.yearOfCertification = yearOfCertification;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }
}
