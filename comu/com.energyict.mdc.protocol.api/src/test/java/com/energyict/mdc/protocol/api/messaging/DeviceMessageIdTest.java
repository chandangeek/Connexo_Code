/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.messaging;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Tests that all {@link DeviceMessageId} enumeration values have a unique db value.
 */
public class DeviceMessageIdTest {

    @Test
    public void allEnumValuesHaveUniqueDbValue () {
        Set<DeviceMessageId> nonUnique = EnumSet.noneOf(DeviceMessageId.class);
        Set<Long> uniqueDbValues = new HashSet<>();
        for (DeviceMessageId deviceMessageId : DeviceMessageId.values()) {
            if (!uniqueDbValues.add(deviceMessageId.dbValue())) {
                nonUnique.add(deviceMessageId);
            }
        }
        if (!nonUnique.isEmpty()) {
            for (DeviceMessageId deviceMessageId : nonUnique) {
                System.out.println("DeviceMessageId#" + deviceMessageId.name() + " does not have a unique db value");
            }
            fail("Some MessageId enumeration values do not have a unique db value, see list above");
        }
    }

}