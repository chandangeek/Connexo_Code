/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation.impl;

import com.energyict.mdc.device.data.validation.DeviceValidationKpiResults;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class DeviceValidationKpiResultsImpl implements DeviceValidationKpiResults {

    private boolean allDataValidated;
    private long amountOfSuspects;
    private long channelSuspects;
    private long registerSuspects;
    private Instant lastSuspect;
    private boolean thresholdValidator;
    private boolean missingValuesValidator;
    private boolean readingQualitiesValidator;
    private boolean registerIncreaseValidator;

    static DeviceValidationKpiResultsImpl from(ResultSet resultSet) throws SQLException {
        return new DeviceValidationKpiResultsImpl(
                resultSet.getLong(6),
                resultSet.getLong(7),
                resultSet.getLong(8),
                resultSet.getInt(9),
                Instant.ofEpochMilli(resultSet.getLong(5)),
                resultSet.getLong(10),
                resultSet.getLong(11),
                resultSet.getLong(12),
                resultSet.getLong(13));
    }

    // For testing purposes
    public DeviceValidationKpiResultsImpl(
            long amountOfSuspects, long channelSuspects, long registerSuspects, long allDataValidated, Instant lastSuspect,
            long thresholdValidator, long missingValuesValidator, long readingQualitiesValidator, long registerIncreaseValidator) {
        this.amountOfSuspects = amountOfSuspects;
        this.channelSuspects = channelSuspects;
        this.registerSuspects = registerSuspects;
        this.allDataValidated = allDataValidated == 1;
        this.lastSuspect = lastSuspect;
        this.thresholdValidator = thresholdValidator >= 1;
        this.missingValuesValidator = missingValuesValidator >= 1;
        this.readingQualitiesValidator = readingQualitiesValidator >= 1;
        this.registerIncreaseValidator = registerIncreaseValidator >= 1;
    }

    @Override
    public boolean isAllDataValidated() {
        return allDataValidated;
    }

    @Override
    public long getAmountOfSuspects() {
        return amountOfSuspects;
    }

    @Override
    public long getChannelSuspects() {
        return channelSuspects;
    }

    @Override
    public long getRegisterSuspects() {
        return registerSuspects;
    }

    @Override
    public Instant getLastSuspect() {
        return lastSuspect;
    }

    @Override
    public boolean isThresholdValidator() {
        return thresholdValidator;
    }

    @Override
    public boolean isMissingValuesValidator() {
        return missingValuesValidator;
    }

    @Override
    public boolean isReadingQualitiesValidator() {
        return readingQualitiesValidator;
    }

    @Override
    public boolean isRegisterIncreaseValidator() {
        return registerIncreaseValidator;
    }

}