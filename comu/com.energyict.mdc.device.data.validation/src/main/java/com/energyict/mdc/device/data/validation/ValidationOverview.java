package com.energyict.mdc.device.data.validation;

/**
 * Created by Lucian on 7/2/2015.
 */
public class ValidationOverview {
    private String mrid;
    private String serialNumber;
    private String deviceType;
    private String deviceConfig;
    public long amountOfSuspects;
    public long channelSuspects;
    public long registerSuspects;
    public boolean allDataValidated;


    public ValidationOverview(String mrid, String serialNumber, String deviceType, String deviceConfig, long amountOfSuspects, long channelSuspects, long registerSuspects, boolean allDataValidated){
        this.mrid = mrid;
        this.serialNumber = serialNumber;
        this.deviceType = deviceType;
        this.deviceConfig = deviceConfig;
        this.amountOfSuspects = amountOfSuspects;
        this.allDataValidated = allDataValidated;
        this.channelSuspects = channelSuspects;
        this.registerSuspects = registerSuspects;
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

    public boolean isAllDataValidated() {
        return allDataValidated;
    }

    public void setAllDataValidated(boolean allDataValidated) {
        this.allDataValidated = allDataValidated;
    }

    public long getAmountOfSuspects() {
        return amountOfSuspects;
    }

    public void setAmountOfSuspects(long amountOfSuspects) {
        this.amountOfSuspects = amountOfSuspects;
    }

    public long getChannelSuspects() {
        return channelSuspects;
    }

    public void setChannelSuspects(long channelSuspects) {
        this.channelSuspects = channelSuspects;
    }

    public long getRegisterSuspects() {
        return registerSuspects;
    }

    public void setRegisterSuspects(long registerSuspects) {
        this.registerSuspects = registerSuspects;
    }
}