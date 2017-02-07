/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifierType;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;

import javax.xml.bind.annotation.XmlElement;

public class DeviceIdentifierByLoadProfile implements DeviceIdentifier<Device> {

    private final LoadProfileIdentifier loadProfileIdentifier;

    public DeviceIdentifierByLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
        this.loadProfileIdentifier = loadProfileIdentifier;
    }

    @Override
    public Device findDevice() {
        return (Device) this.loadProfileIdentifier.findLoadProfile().getDevice();
    }

    @Override
    public String getIdentifier() {
        return loadProfileIdentifier.toString();
    }

    @Override
    public DeviceIdentifierType getDeviceIdentifierType() {
        return DeviceIdentifierType.Other;
    }

    @Override
    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeviceIdentifierByLoadProfile)) {
            return false;
        }

        DeviceIdentifierByLoadProfile that = (DeviceIdentifierByLoadProfile) o;

        return loadProfileIdentifier.equals(that.loadProfileIdentifier);

    }

    @Override
    public int hashCode() {
        return loadProfileIdentifier.hashCode();
    }

    @Override
    public String toString() {
        return "device having load profile identified by '" + this.loadProfileIdentifier + "'";
    }
}
