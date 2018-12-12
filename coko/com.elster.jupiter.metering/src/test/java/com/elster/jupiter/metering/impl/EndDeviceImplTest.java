/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class EndDeviceImplTest {

    
    @Test
    public void testGetMrId() {
        assertThat(EndDevice.TYPE_IDENTIFIER).isNotEqualTo(Meter.TYPE_IDENTIFIER);
    }

}
