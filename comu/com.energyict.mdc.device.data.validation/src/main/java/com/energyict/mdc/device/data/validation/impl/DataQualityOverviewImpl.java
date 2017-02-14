/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation.impl;

import com.energyict.mdc.device.data.validation.DataQualityOverview;

import java.sql.ResultSet;
import java.sql.SQLException;

class DataQualityOverviewImpl implements DataQualityOverview<IdWithNameHolder> {

    enum ResultSetColumn {
        DEVICE_ID(1),
        DEVICE_NAME(2),
        DEVICE_SERIAL_NUMBER(3),
        DEVICE_TYPE_ID(4),
        DEVICE_TYPE_NAME(5),
        DEVICE_CONFIGURATION_ID(6),
        DEVICE_CONFIGURATION_NAME(7);

        private int index;

        ResultSetColumn(int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }

    private final IdWithNameHolder device;
    private final String serialNumber;
    private final IdWithNameHolder deviceType;
    private final IdWithNameHolder deviceConfiguration;
    private final DeviceDataQualityKpiResultsImpl deviceValidationKpiResults;

    static DataQualityOverviewImpl from(ResultSet resultSet) throws SQLException {
        IdWithNameHolder device = new IdWithNameHolder(
                resultSet.getLong(ResultSetColumn.DEVICE_ID.index()),
                resultSet.getString(ResultSetColumn.DEVICE_NAME.index())
        );
        String serialNumber = resultSet.getString(ResultSetColumn.DEVICE_SERIAL_NUMBER.index());
        IdWithNameHolder deviceType = new IdWithNameHolder(
                resultSet.getLong(ResultSetColumn.DEVICE_TYPE_ID.index()),
                resultSet.getString(ResultSetColumn.DEVICE_TYPE_NAME.index())
        );
        IdWithNameHolder deviceConfiguration = new IdWithNameHolder(
                resultSet.getLong(ResultSetColumn.DEVICE_CONFIGURATION_ID.index()),
                resultSet.getString(ResultSetColumn.DEVICE_CONFIGURATION_NAME.index())
        );
        return new DataQualityOverviewImpl(
                device,
                serialNumber,
                deviceType,
                deviceConfiguration,
                DeviceDataQualityKpiResultsImpl.from(resultSet));
    }

    // For testing purposes
    DataQualityOverviewImpl(IdWithNameHolder device, String serialNumber, IdWithNameHolder deviceType, IdWithNameHolder deviceConfiguration, DeviceDataQualityKpiResultsImpl deviceValidationKpiResults) {
        this.device = device;
        this.serialNumber = serialNumber;
        this.deviceType = deviceType;
        this.deviceConfiguration = deviceConfiguration;
        this.deviceValidationKpiResults = deviceValidationKpiResults;
    }

    @Override
    public IdWithNameHolder getDevice() {
        return device;
    }

    @Override
    public String getDeviceSerialNumber() {
        return serialNumber;
    }

    @Override
    public IdWithNameHolder getDeviceType() {
        return deviceType;
    }

    @Override
    public IdWithNameHolder getDeviceConfiguration() {
        return deviceConfiguration;
    }

    @Override
    public DeviceDataQualityKpiResultsImpl getDataQualityKpiResults() {
        return deviceValidationKpiResults;
    }
}
