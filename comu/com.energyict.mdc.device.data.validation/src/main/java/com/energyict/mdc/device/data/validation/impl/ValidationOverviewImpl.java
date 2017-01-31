/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation.impl;

import com.energyict.mdc.device.data.validation.ValidationOverview;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provides an implementation for the {@link ValidationOverview} interface.
 *
 * @author Lucian
 * @since 2015-02-07
 */
public class ValidationOverviewImpl implements ValidationOverview {
    private final String deviceName;
    private final String serialNumber;
    private final String deviceTypeName;
    private final String deviceConfigurationName;
    private final DeviceValidationKpiResultsImpl deviceValidationKpiResults;

    static ValidationOverviewImpl from(ResultSet resultSet, DeviceDataValidationServiceImpl.ValidationOverviewSpecificationImpl specification) throws SQLException {
        return new ValidationOverviewImpl(
                resultSet.getString(1),
                resultSet.getString(2),
                resultSet.getString(3),
                resultSet.getString(4),
                DeviceValidationKpiResultsImpl.from(resultSet));
    }

    // For testing purposes
    public ValidationOverviewImpl(String deviceName, String serialNumber, String deviceTypeName, String deviceConfigurationName, DeviceValidationKpiResultsImpl deviceValidationKpiResults) {
        this.deviceName = deviceName;
        this.serialNumber = serialNumber;
        this.deviceTypeName = deviceTypeName;
        this.deviceConfigurationName = deviceConfigurationName;
        this.deviceValidationKpiResults = deviceValidationKpiResults;
    }

    @Override
    public String getDeviceName() {
        return deviceName;
    }

    @Override
    public String getDeviceSerialNumber() {
        return serialNumber;
    }

    @Override
    public String getDeviceTypeName() {
        return deviceTypeName;
    }

    @Override
    public String getDeviceConfigurationName() {
        return deviceConfigurationName;
    }

    @Override
    public DeviceValidationKpiResultsImpl getDeviceValidationKpiResults() {
        return deviceValidationKpiResults;
    }
}
