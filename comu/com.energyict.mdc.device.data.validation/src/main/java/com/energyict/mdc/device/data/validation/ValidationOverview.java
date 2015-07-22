package com.energyict.mdc.device.data.validation;

/**
 * Created by Lucian on 7/2/2015.
 */
public class ValidationOverview {
    private String mrid;
    private String serialNumber;
    private String deviceType;
    private String deviceConfig;

    public ValidationOverview(String mrid, String serialNumber, String deviceType, String deviceConfig){
        this.mrid = mrid;
        this.serialNumber = serialNumber;
        this.deviceType = deviceType;
        this.deviceConfig = deviceConfig;
    }

    public String getMrid() {
        return mrid;
    }

    public void setMrid(String mrid) {
        this.mrid = mrid;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceConfig() {
        return deviceConfig;
    }

    public void setDeviceConfig(String deviceConfig) {
        this.deviceConfig = deviceConfig;
    }
}