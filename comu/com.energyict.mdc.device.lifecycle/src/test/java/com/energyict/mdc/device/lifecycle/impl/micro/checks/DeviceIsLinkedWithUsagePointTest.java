/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

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
 * Tests the {@link DeviceIsLinkedWithUsagePoint} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-17 (12:56)
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
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now(), null);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.LINKED_WITH_USAGE_POINT);
    }

    @Test
    public void deviceIsLinkedToUsagePoint() {
        when(this.device.getUsagePoint()).thenReturn(Optional.of(mock(UsagePoint.class)));
        DeviceIsLinkedWithUsagePoint microCheck = this.getTestInstance();

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now(), null);

        // Asserts
        assertThat(violation).isEmpty();
    }

    private DeviceIsLinkedWithUsagePoint getTestInstance() {
        return new DeviceIsLinkedWithUsagePoint(this.thesaurus);
    }

}