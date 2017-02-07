/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.checks;

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
import static org.mockito.Mockito.when;

/**
 * Tests the {@link AllIssuesAreClosed} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-17 (15:27)
 */
@RunWith(MockitoJUnitRunner.class)
public class AllIssuesAreClosedTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Device device;

    @Test
    public void deviceWithIssues() {
        when(this.device.hasOpenIssues()).thenReturn(true);
        AllIssuesAreClosed microCheck = this.getTestInstance();

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.ALL_ISSUES_AND_ALARMS_ARE_CLOSED);
    }

    @Test
    public void deviceWithoutIssues() {
        when(this.device.hasOpenIssues()).thenReturn(false);
        AllIssuesAreClosed microCheck = this.getTestInstance();

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    private AllIssuesAreClosed getTestInstance() {
        return new AllIssuesAreClosed(this.thesaurus);
    }

}