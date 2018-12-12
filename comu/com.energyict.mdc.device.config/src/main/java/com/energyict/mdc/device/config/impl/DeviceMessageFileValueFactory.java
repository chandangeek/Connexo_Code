/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.config.DeviceMessageFile;
import com.energyict.mdc.device.config.DeviceType;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Provides an implementation for the {@link ValueFactory} interface
 * for references to a {@link DeviceMessageFile}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-13 (10:46)
 */
class DeviceMessageFileValueFactory implements ValueFactory<DeviceMessageFile> {

    private final DeviceType deviceType;

    DeviceMessageFileValueFactory(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    @Override
    public boolean isReference() {
        return true;
    }

    @Override
    public boolean isValid(DeviceMessageFile value) {
        return this.deviceType.getDeviceMessageFiles().contains(value);
    }

    @Override
    public DeviceMessageFile fromStringValue(String stringValue) {
        return this.deviceType
                .getDeviceMessageFiles()
                .stream()
                .filter(deviceMessageFile -> this.toStringValue(deviceMessageFile).equals(stringValue))
                .findAny()
                .orElse(null);
    }

    @Override
    public String toStringValue(DeviceMessageFile deviceMessageFile) {
        return String.valueOf(deviceMessageFile.getId());
    }

    @Override
    public Class<DeviceMessageFile> getValueType() {
        return DeviceMessageFile.class;
    }

    @Override
    public DeviceMessageFile valueFromDatabase(Object object) {
        return this.valueFromDatabase((Long) object);
    }

    private DeviceMessageFile valueFromDatabase(Long id) {
        return this.deviceType
                .getDeviceMessageFiles()
                .stream()
                .filter(deviceMessageFile -> this.valueToDatabase(deviceMessageFile).equals(id))
                .findAny()
                .orElse(null);
    }

    @Override
    public Long valueToDatabase(DeviceMessageFile deviceMessageFile) {
        return deviceMessageFile.getId();
    }

    @Override
    public void bind(PreparedStatement statement, int offset, DeviceMessageFile deviceMessageFile) throws SQLException {
        statement.setLong(offset, this.valueToDatabase(deviceMessageFile));
    }

    @Override
    public void bind(SqlBuilder builder, DeviceMessageFile deviceMessageFile) {
        builder.addLong(this.valueToDatabase(deviceMessageFile));
    }
}