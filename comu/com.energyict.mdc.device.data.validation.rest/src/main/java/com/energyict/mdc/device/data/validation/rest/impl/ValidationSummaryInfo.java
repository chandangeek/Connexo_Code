/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation.rest.impl;

import com.energyict.mdc.device.data.validation.ValidationOverview;

import java.time.Instant;

/**
 * Created by Lucian on 6/18/2015.
 */
public class ValidationSummaryInfo {

    public String name;
    public String serialNumber;
    public String deviceType;
    public String deviceConfig;
    public long amountOfSuspects;
    public long channelSuspects;
    public long registerSuspects;
    public boolean allDataValidated;
    public Instant lastSuspect;
    public boolean thresholdValidator;
    public boolean missingValuesValidator;
    public boolean readingQualitiesValidator;
    public boolean registerIncreaseValidator;

    public ValidationSummaryInfo(ValidationOverview validationOverview) {
        this.name = validationOverview.getDeviceName();
        this.serialNumber = validationOverview.getDeviceSerialNumber();
        this.deviceType = validationOverview.getDeviceTypeName();
        this.deviceConfig = validationOverview.getDeviceConfigurationName();
        this.amountOfSuspects = validationOverview.getDeviceValidationKpiResults().getAmountOfSuspects();
        this.channelSuspects = validationOverview.getDeviceValidationKpiResults().getChannelSuspects();
        this.registerSuspects = validationOverview.getDeviceValidationKpiResults().getRegisterSuspects();
        this.allDataValidated = validationOverview.getDeviceValidationKpiResults().isAllDataValidated();
        this.lastSuspect = validationOverview.getDeviceValidationKpiResults().getLastSuspect();
        this.thresholdValidator = validationOverview.getDeviceValidationKpiResults().isThresholdValidator();
        this.missingValuesValidator = validationOverview.getDeviceValidationKpiResults().isMissingValuesValidator();
        this.readingQualitiesValidator = validationOverview.getDeviceValidationKpiResults().isReadingQualitiesValidator();
        this.registerIncreaseValidator = validationOverview.getDeviceValidationKpiResults().isRegisterIncreaseValidator();
    }
}
