/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation.impl;

import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.validation.Validator;
import com.energyict.mdc.device.data.validation.DeviceDataQualityKpiResults;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class DeviceDataQualityKpiResultsImpl implements DeviceDataQualityKpiResults {

    private Instant lastSuspect;
    private long amountOfSuspects;
    private long channelSuspects;
    private long registerSuspects;
    private long amountOfAdded;
    private long amountOfEdited;
    private long amountOfRemoved;
    private long amountOfConfirmed;
    private long amountOfEstimated;
    private long amountOfInformatives;

    // validators
    //estimators

    static DeviceDataQualityKpiResultsImpl from(ResultSet resultSet) throws SQLException {
        return new DeviceDataQualityKpiResultsImpl(
//                resultSet.getLong(6),
//                resultSet.getLong(7),
//                resultSet.getLong(8),
//                resultSet.getInt(9),
//                Instant.ofEpochMilli(resultSet.getLong(5)),
//                resultSet.getLong(10),
//                resultSet.getLong(11),
//                resultSet.getLong(12),
//                resultSet.getLong(13)
        );
    }

    @Override
    public Instant getLastSuspect() {
        return lastSuspect;
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
    public long getAmountOfAdded() {
        return amountOfAdded;
    }

    @Override
    public long getAmountOfEdited() {
        return amountOfEdited;
    }

    @Override
    public long getAmountOfRemoved() {
        return amountOfRemoved;
    }

    @Override
    public long getAmountOfConfirmed() {
        return amountOfConfirmed;
    }

    @Override
    public long getAmountOfEstimated() {
        return amountOfEstimated;
    }

    @Override
    public long getAmountOfInformatives() {
        return amountOfInformatives;
    }

    @Override
    public long getAmountOfValidatedBy(Validator validator) {
        return 0;
    }

    @Override
    public long getAmountOfEstimatedBy(Estimator estimator) {
        return 0;
    }
}