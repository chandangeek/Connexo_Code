/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.identifiers;

import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a DeviceIdentifier that uniquely identifies a Device which you have given in the Constructor.
 */
@XmlRootElement
public final class DeviceIdentifierForAlreadyKnownDevice implements DeviceIdentifier {

    private long deviceId;
    private String devicemrID;

    /**
     * Constructor only to be used by JSON (de)marshalling or in unit tests
     */
    public DeviceIdentifierForAlreadyKnownDevice() {
    }

    public DeviceIdentifierForAlreadyKnownDevice(long deviceId, String deviceMrId) {
        this();
        this.deviceId = deviceId;
        this.devicemrID = deviceMrId;
    }

    public DeviceIdentifierForAlreadyKnownDevice(Device device) {
        this.deviceId = device.getId();
        this.devicemrID = device.getmRID();
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @Override
    public String toString() {
        return "deviceId having MRID " + getDevicemrID();
    }

    @XmlAttribute
    public long getDeviceId() {
        return deviceId;
    }

    @XmlAttribute
    public String getDevicemrID() {
        return devicemrID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeviceIdentifierForAlreadyKnownDevice that = (DeviceIdentifierForAlreadyKnownDevice) o;

        return this.deviceId == that.deviceId;

    }

    @Override
    public int hashCode() {
        return Long.valueOf(deviceId).hashCode();
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "Actual";
        }

        @Override
        public Set<String> getRoles() {
            return new HashSet<>(Arrays.asList("actual", "mRID"));
        }

        @Override
        public Object getValue(String role) {
            switch (role) {
                case "actual": {
                    return deviceId;
                }
                case "mRID": {
                    return getDevicemrID();
                }
                default: {
                    throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
                }
            }
        }
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }
}