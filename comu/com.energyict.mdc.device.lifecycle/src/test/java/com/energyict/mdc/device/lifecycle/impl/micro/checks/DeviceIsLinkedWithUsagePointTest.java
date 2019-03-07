/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;

import java.time.Instant;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceIsLinkedWithUsagePoint} component
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceIsLinkedWithUsagePointTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Device device;

    @Test
    public void deviceIsNotLinkedToUsagePoint() {
        when(this.device.getUsagePoint()).thenReturn(Optional.<UsagePoint>empty());
        DeviceIsLinkedWithUsagePoint microCheck = this.getTestInstance();

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    @Test
    public void deviceIsLinkedToUsagePoint() {
        when(this.device.getUsagePoint()).thenReturn(Optional.of(mock(UsagePoint.class)));
        DeviceIsLinkedWithUsagePoint microCheck = this.getTestInstance();

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    private DeviceIsLinkedWithUsagePoint getTestInstance() {
        DeviceIsLinkedWithUsagePoint deviceIsLinkedWithUsagePoint = new DeviceIsLinkedWithUsagePoint();
        deviceIsLinkedWithUsagePoint.setThesaurus(this.thesaurus);
        return deviceIsLinkedWithUsagePoint;
    }
}
