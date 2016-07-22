package com.energyict.mdc.device.data.validation.rest.impl;

import com.energyict.mdc.device.data.validation.ValidationOverview;

import java.time.Instant;

/**
 * Created by Lucian on 6/18/2015.
 */
public class ValidationSummaryInfo {

    public String mrid;
    public String serialNumber;
    public String deviceType;
    public String deviceConfig;
    public long amountOfSuspects;
    public long channelSuspects;
    public long registerSuspects;
    public boolean allDataValidated;
    public Instant lastSuspect;

    public ValidationSummaryInfo(ValidationOverview validationOverview) {
        this.mrid = validationOverview.getMrid();
        this.serialNumber = validationOverview.getSerialNumber();
        this.deviceType = validationOverview.getDeviceType();
        this.deviceConfig = validationOverview.getDeviceConfig();
        this.amountOfSuspects = validationOverview.getDeviceValidationKpiResults().getAmountOfSuspects();
        this.channelSuspects = validationOverview.getDeviceValidationKpiResults().getChannelSuspects();
        this.registerSuspects = validationOverview.getDeviceValidationKpiResults().getRegisterSuspects();
        this.allDataValidated = validationOverview.getDeviceValidationKpiResults().isAllDataValidated();
        this.lastSuspect = validationOverview.getDeviceValidationKpiResults().getLastSuspect();
    }
}
