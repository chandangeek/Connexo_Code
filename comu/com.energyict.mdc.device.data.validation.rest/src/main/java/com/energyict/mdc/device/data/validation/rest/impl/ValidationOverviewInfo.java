/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation.rest.impl;

import com.energyict.mdc.device.data.validation.DataQualityOverview;

import java.time.Instant;

/**
 * Ships information of a {@link DataQualityOverview} between front and backend.
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

    public static ValidationOverviewInfo from(DataQualityOverview overview) {
        ValidationOverviewInfo info = new ValidationOverviewInfo();
        info.name = overview.getDeviceName();
        info.serialNumber = overview.getDeviceSerialNumber();
        info.deviceType = overview.getDeviceTypeName();
        info.deviceConfig = overview.getDeviceConfigurationName();
        info.allDataValidated = overview.getDataQualityKpiResults().isAllDataValidated();
        info.amountOfSuspects = overview.getDataQualityKpiResults().getAmountOfSuspects();
        info.channelSuspects = overview.getDataQualityKpiResults().getChannelSuspects();
        info.registerSuspects = overview.getDataQualityKpiResults().getRegisterSuspects();
        info.lastSuspect = overview.getDataQualityKpiResults().getLastSuspect();
        info.thresholdValidator = overview.getDataQualityKpiResults().isThresholdValidator();
        info.missingValuesValidator = overview.getDataQualityKpiResults().isMissingValuesValidator();
        info.readingQualitiesValidator = overview.getDataQualityKpiResults().isReadingQualitiesValidator();
        info.registerIncreaseValidator = overview.getDataQualityKpiResults().isRegisterIncreaseValidator();
        return info;
    }
}