/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceCommunicationFunction;

import java.util.EnumSet;
import java.util.Set;

/**
 * Provides persistence services for a Set of {@link DeviceCommunicationFunction}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-10 (13:52)
 */
final class DeviceCommunicationFunctionSetPersister {

    public int toDb(Set<DeviceCommunicationFunction> deviceCommunicationFunctions) {
        int bits = 0;   // Make sure all bits are cleared
        int mask = 1;   // 1 ^0
        for (DeviceCommunicationFunction deviceCommunicationFunction : DeviceCommunicationFunction.values()) {
            if (deviceCommunicationFunctions.contains(deviceCommunicationFunction)) {
                bits = bits + mask;
            }
            mask = mask * 2;    // Shift one bit left
        }
        return bits;
    }

    public Set<DeviceCommunicationFunction> fromDb (int deviceCommunicationFunctionBits) {
        Set<DeviceCommunicationFunction> deviceCommunicationFunctions = EnumSet.noneOf(DeviceCommunicationFunction.class);
        int mask = 1;   // 1 ^0
        for (DeviceCommunicationFunction deviceCommunicationFunction : DeviceCommunicationFunction.values()) {
            if ((mask & deviceCommunicationFunctionBits) != 0) {
                deviceCommunicationFunctions.add(deviceCommunicationFunction);
            }
            mask = mask * 2;    // Shift one bit left
        }
        return deviceCommunicationFunctions;
    }

}