/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation.rest.impl;

import com.energyict.mdc.device.data.validation.DataQualityOverview;

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

    public ValidationSummaryInfo(DataQualityOverview dataQualityOverview) {
        this.name = dataQualityOverview.getDeviceName();
        this.serialNumber = dataQualityOverview.getDeviceSerialNumber();
        this.deviceType = dataQualityOverview.getDeviceTypeName();
        this.deviceConfig = dataQualityOverview.getDeviceConfigurationName();
        this.amountOfSuspects = dataQualityOverview.getDataQualityKpiResults().getAmountOfSuspects();
        this.channelSuspects = dataQualityOverview.getDataQualityKpiResults().getChannelSuspects();
        this.registerSuspects = dataQualityOverview.getDataQualityKpiResults().getRegisterSuspects();
        this.allDataValidated = dataQualityOverview.getDataQualityKpiResults().isAllDataValidated();
        this.lastSuspect = dataQualityOverview.getDataQualityKpiResults().getLastSuspect();
        this.thresholdValidator = dataQualityOverview.getDataQualityKpiResults().isThresholdValidator();
        this.missingValuesValidator = dataQualityOverview.getDataQualityKpiResults().isMissingValuesValidator();
        this.readingQualitiesValidator = dataQualityOverview.getDataQualityKpiResults().isReadingQualitiesValidator();
        this.registerIncreaseValidator = dataQualityOverview.getDataQualityKpiResults().isRegisterIncreaseValidator();
    }
}
