package com.energyict.mdc.device.data.validation.impl;

import com.energyict.mdc.device.data.validation.DeviceValidationKpiResults;
import com.energyict.mdc.device.data.validation.ValidationOverview;

/**
 * Created by Lucian on 7/2/2015.
 */
public class ValidationOverviewImpl implements ValidationOverview {
    private String mrid;
    private String serialNumber;
    private String deviceType;
    private String deviceConfig;
    private DeviceValidationKpiResults deviceValidationKpiResults;


    public ValidationOverviewImpl(String mrid, String serialNumber, String deviceType, String deviceConfig, DeviceValidationKpiResults deviceValidationKpiResults){
        this.mrid = mrid;
        this.serialNumber = serialNumber;
        this.deviceType = deviceType;
        this.deviceConfig = deviceConfig;
        this. deviceValidationKpiResults = deviceValidationKpiResults;
    }

    @Override public String getMrid() {
        return mrid;
    }

    @Override public void setMrid(String mrid) {
        this.mrid = mrid;
    }

    @Override public String getSerialNumber() {
        return serialNumber;
    }

    @Override public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override public String getDeviceType() {
        return deviceType;
    }

    @Override public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    @Override public String getDeviceConfig() {
        return deviceConfig;
    }

    @Override public void setDeviceConfig(String deviceConfig) {
        this.deviceConfig = deviceConfig;
    }

    @Override public DeviceValidationKpiResults getDeviceValidationKpiResults() {
        return deviceValidationKpiResults;
    }

    @Override public void setDeviceValidationKpiResults(DeviceValidationKpiResults deviceValidationKpiResults) {
        this.deviceValidationKpiResults = deviceValidationKpiResults;
    }
}