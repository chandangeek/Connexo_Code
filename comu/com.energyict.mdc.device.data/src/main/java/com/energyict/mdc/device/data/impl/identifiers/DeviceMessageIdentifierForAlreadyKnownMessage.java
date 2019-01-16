/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of a {@link MessageIdentifier} that uniquely identifies a {@link DeviceMessage}
 * based on the ID of the DeviceMessage.
 *
 * Date: 22/03/13
 * Time: 9:05
 */
public class DeviceMessageIdentifierForAlreadyKnownMessage implements MessageIdentifier {

    private final DeviceMessage deviceMessage;

    // For JSON serialization only or in unit tests
    @SuppressWarnings("unused")
    public DeviceMessageIdentifierForAlreadyKnownMessage() {
        this.deviceMessage = null;
    }

    public DeviceMessageIdentifierForAlreadyKnownMessage(DeviceMessage deviceMessage) {
        this.deviceMessage = deviceMessage;
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return new DeviceIdentifierForAlreadyKnownDevice(deviceMessage.getDevice());
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @Override
    public String toString() {
        return "message having id " + deviceMessage.getId();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DeviceMessageIdentifierForAlreadyKnownMessage that = (DeviceMessageIdentifierForAlreadyKnownMessage) obj;
        return this.deviceMessage.getId() == that.deviceMessage.getId();
    }

    @Override
    public int hashCode() {
        return (int) this.deviceMessage.getId();
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "Actual";
        }

        @Override
        public Set<String> getRoles() {
            return new HashSet<>(Arrays.asList("actual", "device", "databaseValue"));
        }

        @Override
        public Object getValue(String role) {
            switch (role) {
                case "actual": {
                    return deviceMessage;
                }
                case "device": {
                    return getDeviceIdentifier();
                }
                case "databaseValue": {
                    return deviceMessage.getId();
                }
                default: {
                    throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
                }
            }
        }
    }

}