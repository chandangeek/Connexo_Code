/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifierType;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;

public class DeviceIdentifierByLogBook implements DeviceIdentifier<Device> {

    private final LogBookIdentifier logBookIdentifier;

    public DeviceIdentifierByLogBook(LogBookIdentifier logBookIdentifier) {
        this.logBookIdentifier = logBookIdentifier;
    }

    @Override
    public Device findDevice() {
        return ((LogBook) this.logBookIdentifier.getLogBook()).getDevice();
    }

    @Override
    public String getIdentifier() {
        return this.logBookIdentifier.toString();
    }

    @Override
    public DeviceIdentifierType getDeviceIdentifierType() {
        return DeviceIdentifierType.Other;
    }

    @Override
    public String getXmlType() {
        return null;
    }

    @Override
    public void setXmlType(String ignore) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeviceIdentifierByLogBook)) {
            return false;
        }

        DeviceIdentifierByLogBook that = (DeviceIdentifierByLogBook) o;

        return logBookIdentifier.equals(that.logBookIdentifier);

    }

    @Override
    public int hashCode() {
        return logBookIdentifier.hashCode();
    }

    @Override
    public String toString() {
        return "device having logbook identified by '" + this.logBookIdentifier + "'";
    }
}
