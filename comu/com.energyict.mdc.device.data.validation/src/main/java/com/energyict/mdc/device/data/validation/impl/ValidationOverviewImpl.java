package com.energyict.mdc.device.data.validation.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
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
    private final EndDeviceGroup deviceGroup;
    private final String mRID;
    private final String serialNumber;
    private final String deviceTypeName;
    private final String deviceConfigurationName;
    private final DeviceValidationKpiResultsImpl deviceValidationKpiResults;

    static ValidationOverviewImpl from(ResultSet resultSet, DeviceDataValidationServiceImpl.ValidationOverviewSpecificationImpl specification) throws SQLException {
        return new ValidationOverviewImpl(
                specification.group(resultSet.getLong(2)),
                resultSet.getString(1),
                resultSet.getString(3),
                resultSet.getString(4),
                resultSet.getString(5),
                DeviceValidationKpiResultsImpl.from(resultSet));
    }

    // For testing purposes
    public ValidationOverviewImpl(EndDeviceGroup deviceGroup, String mRID, String serialNumber, String deviceTypeName, String deviceConfigurationName, DeviceValidationKpiResultsImpl deviceValidationKpiResults) {
        this.deviceGroup = deviceGroup;
        this.mRID = mRID;
        this.serialNumber = serialNumber;
        this.deviceTypeName = deviceTypeName;
        this.deviceConfigurationName = deviceConfigurationName;
        this.deviceValidationKpiResults = deviceValidationKpiResults;
    }

    EndDeviceGroup getDeviceGroup() {
        return deviceGroup;
    }

    @Override
    public String getDevice_mRID() {
        return mRID;
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