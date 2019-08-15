/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;

import java.time.Instant;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link AllIssuesAreClosed} component
 */
@RunWith(MockitoJUnitRunner.class)
public class AllIssuesAreClosedTest {

    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    @Mock
    private Device device;
    @Mock
    private State state;

    @Test
    public void deviceWithIssues() {
        when(this.device.hasOpenIssues()).thenReturn(true);
        AllIssuesAreClosed microCheck = this.getTestInstance();

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    @Test
    public void deviceWithoutIssues() {
        when(this.device.hasOpenIssues()).thenReturn(false);
        AllIssuesAreClosed microCheck = this.getTestInstance();

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isEmpty();
    }

    private AllIssuesAreClosed getTestInstance() {
        AllIssuesAreClosed allIssuesAreClosed = new AllIssuesAreClosed();
        allIssuesAreClosed.setThesaurus(this.thesaurus);
        return allIssuesAreClosed;
    }
}
