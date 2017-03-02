/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.impl;

import com.energyict.mdc.device.dataquality.DataQualityOverview;

import java.sql.ResultSet;
import java.sql.SQLException;

class DataQualityOverviewImpl implements DataQualityOverview {

    enum ResultSetColumn {
        DEVICE_NAME(1),
        DEVICE_SERIAL_NUMBER(2),
        DEVICE_TYPE_ID(3),
        DEVICE_TYPE_NAME(4),
        DEVICE_CONFIGURATION_ID(5),
        DEVICE_CONFIGURATION_NAME(6),
        LAST_SUSPECT(7);

        private int index;

        ResultSetColumn(int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }

    private final String deviceName;
    private final String serialNumber;
    private final IdWithNameImpl deviceType;
    private final IdWithNameImpl deviceConfiguration;
    private final DeviceDataQualityKpiResultsImpl deviceValidationKpiResults;

    static DataQualityOverviewImpl from(ResultSet resultSet, DataQualityOverviewSpecificationImpl specification) throws SQLException {
        String deviceName = resultSet.getString(ResultSetColumn.DEVICE_NAME.index());
        String serialNumber = resultSet.getString(ResultSetColumn.DEVICE_SERIAL_NUMBER.index());
        IdWithNameImpl deviceType = new IdWithNameImpl(
                resultSet.getLong(ResultSetColumn.DEVICE_TYPE_ID.index()),
                resultSet.getString(ResultSetColumn.DEVICE_TYPE_NAME.index())
        );
        IdWithNameImpl deviceConfiguration = new IdWithNameImpl(
                resultSet.getLong(ResultSetColumn.DEVICE_CONFIGURATION_ID.index()),
                resultSet.getString(ResultSetColumn.DEVICE_CONFIGURATION_NAME.index())
        );
        return new DataQualityOverviewImpl(
                deviceName,
                serialNumber,
                deviceType,
                deviceConfiguration,
                DeviceDataQualityKpiResultsImpl.from(resultSet, specification));
    }

    private DataQualityOverviewImpl(String deviceName, String serialNumber, IdWithNameImpl deviceType,
                            IdWithNameImpl deviceConfiguration, DeviceDataQualityKpiResultsImpl deviceValidationKpiResults) {
        this.deviceName = deviceName;
        this.serialNumber = serialNumber;
        this.deviceType = deviceType;
        this.deviceConfiguration = deviceConfiguration;
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
    @SuppressWarnings("unchecked")
    public IdWithNameImpl getDeviceType() {
        return deviceType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IdWithNameImpl getDeviceConfiguration() {
        return deviceConfiguration;
    }

    @Override
    public DeviceDataQualityKpiResultsImpl getDataQualityKpiResults() {
        return deviceValidationKpiResults;
    }
}
