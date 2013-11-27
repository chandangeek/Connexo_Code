package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class EndDeviceImplTest {

    
    @Test
    public void testGetMrId() {
        assertThat(EndDevice.TYPE_IDENTIFIER).isNotEqualTo(Meter.TYPE_IDENTIFIER);
    }

}
