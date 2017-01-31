/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation.rest.impl;

import com.energyict.mdc.device.data.validation.ValidationOverview;

import java.time.Instant;

/**
 * Ships information of a {@link ValidationOverview} between front and backend.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-09 (15:53)
 */
public final class ValidationOverviewInfo {
    public String name;
    public String serialNumber;
    public String deviceType;
    public String deviceConfig;

    public boolean allDataValidated;
    public long amountOfSuspects;
    public long channelSuspects;
    public long registerSuspects;
    public Instant lastSuspect;
    public boolean thresholdValidator;
    public boolean missingValuesValidator;
    public boolean readingQualitiesValidator;
    public boolean registerIncreaseValidator;

    public static ValidationOverviewInfo from(ValidationOverview overview) {
        ValidationOverviewInfo info = new ValidationOverviewInfo();
        info.name = overview.getDeviceName();
        info.serialNumber = overview.getDeviceSerialNumber();
        info.deviceType = overview.getDeviceTypeName();
        info.deviceConfig = overview.getDeviceConfigurationName();
        info.allDataValidated = overview.getDeviceValidationKpiResults().isAllDataValidated();
        info.amountOfSuspects = overview.getDeviceValidationKpiResults().getAmountOfSuspects();
        info.channelSuspects = overview.getDeviceValidationKpiResults().getChannelSuspects();
        info.registerSuspects = overview.getDeviceValidationKpiResults().getRegisterSuspects();
        info.lastSuspect = overview.getDeviceValidationKpiResults().getLastSuspect();
        info.thresholdValidator = overview.getDeviceValidationKpiResults().isThresholdValidator();
        info.missingValuesValidator = overview.getDeviceValidationKpiResults().isMissingValuesValidator();
        info.readingQualitiesValidator = overview.getDeviceValidationKpiResults().isReadingQualitiesValidator();
        info.registerIncreaseValidator = overview.getDeviceValidationKpiResults().isRegisterIncreaseValidator();
        return info;
    }
}