/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link AllDataValid} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-13 (09:48)
 */
@RunWith(MockitoJUnitRunner.class)
public class AllDataValidTest {

    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    @Mock
    private ValidationService validationService;
    @Mock
    private ValidationEvaluator validationEvaluator;
    @Mock
    private Device device;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private Meter meter;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private State state;

    @Test
    public void deviceWithoutMeterActivation() {
        AllDataValid microCheck = this.getTestInstance();
        doReturn(Optional.empty()).when(this.device).getCurrentMeterActivation();

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    @Test
    public void validationNotEnabledOnMeterActivation() {
        AllDataValid microCheck = this.getTestInstance();
        doReturn(Optional.of(meterActivation)).when(this.device).getCurrentMeterActivation();
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(validationService.validationEnabled(meter)).thenReturn(false);

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void deviceWithMeterActivationWithAllDataValid() {
        AllDataValid microCheck = this.getTestInstance();
        doReturn(Optional.of(meterActivation)).when(this.device).getCurrentMeterActivation();
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(validationService.validationEnabled(meter)).thenReturn(true);
        when(validationService.getEvaluator()).thenReturn(validationEvaluator);
        when(validationEvaluator.areSuspectsPresent(Collections.singleton(QualityCodeSystem.MDC), channelsContainer)).thenReturn(false);

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void deviceWithMeterActivationWithNotAllDataValid() {
        AllDataValid microCheck = this.getTestInstance();
        doReturn(Optional.of(meterActivation)).when(this.device).getCurrentMeterActivation();
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(validationService.validationEnabled(meter)).thenReturn(true);
        when(validationService.getEvaluator()).thenReturn(validationEvaluator);
        when(validationEvaluator.areSuspectsPresent(Collections.singleton(QualityCodeSystem.MDC), channelsContainer)).thenReturn(true);

        // Business method
        Optional<ExecutableMicroCheckViolation> violation = microCheck.execute(this.device, Instant.now(), state);

        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(microCheck);
    }

    private AllDataValid getTestInstance() {
        AllDataValid allDataValid = new AllDataValid();
        allDataValid.setThesaurus(this.thesaurus);
        allDataValid.setValidationService(this.validationService);
        return allDataValid;
    }
}
