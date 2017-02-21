/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami;

import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author sva
 * @since 15/06/2016 - 11:49
 */
public class EndDeviceControlTypeMappingTest {

    @Test
    public void testGetPossibleDeviceMessageIds() throws Exception {
        // Business method
        List<DeviceMessageId> possibleDeviceMessageIds = EndDeviceControlTypeMapping.ARM_REMOTE_SWITCH_FOR_OPEN.getPossibleDeviceMessageIds();

        // Asserts
        assertEquals(4, possibleDeviceMessageIds.size());
        assertThat(possibleDeviceMessageIds).contains(DeviceMessageId.CONTACTOR_OPEN);
        assertThat(possibleDeviceMessageIds).contains(DeviceMessageId.CONTACTOR_ARM);
        assertThat(possibleDeviceMessageIds).contains(DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
        assertThat(possibleDeviceMessageIds).contains(DeviceMessageId.CONTACTOR_ARM_WITH_ACTIVATION_DATE);
    }
}