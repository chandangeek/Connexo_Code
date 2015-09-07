package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.MessageSeeds;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceLifeCycleActionViolationImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-04-14 (15:31)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceLifeCycleActionViolationImplTest {

    @Mock
    private Thesaurus thesaurus;

    @Test
    public void getMicroCheck() {
        MicroCheck expectedMicroCheck = MicroCheck.ALL_ISSUES_AND_ALARMS_ARE_CLOSED;
        DeviceLifeCycleActionViolationImpl violation = this.getTestInstance(expectedMicroCheck);

        // Business method
        MicroCheck microCheck = violation.getCheck();

        // Asserts
        assertThat(microCheck).isEqualTo(expectedMicroCheck);
    }

    @Test
    public void getLocalizedMessage() {
        NlsMessageFormat nlsMessageFormat = mock(NlsMessageFormat.class);
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(nlsMessageFormat);
        DeviceLifeCycleActionViolationImpl violation = this.getTestInstance(MicroCheck.AT_LEAST_ONE_SCHEDULED_COMMUNICATION_TASK_AVAILABLE);

        // Business method
        violation.getLocalizedMessage();

        // Asserts
        verify(this.thesaurus).getFormat(MessageSeeds.MULTIPLE_MICRO_CHECKS_FAILED);
        verify(nlsMessageFormat).format(anyVararg());
    }

    private DeviceLifeCycleActionViolationImpl getTestInstance(MicroCheck microCheck) {
        return new DeviceLifeCycleActionViolationImpl(this.thesaurus, MessageSeeds.MULTIPLE_MICRO_CHECKS_FAILED, microCheck);
    }

}