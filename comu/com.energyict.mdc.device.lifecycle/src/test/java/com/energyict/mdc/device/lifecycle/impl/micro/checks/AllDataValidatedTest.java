/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationService;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link AllDataValid} component.
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AllDataValidatedTest {
    @Mock
    private Thesaurus thesaurus;
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

    @Test
    public void deviceWithoutMeterActivation() {
        AllDataValidated microCheck = this.getTestInstance();
        doReturn(Optional.empty()).when(this.device).getCurrentMeterActivation();

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.ALL_DATA_VALIDATED);
    }

    @Test
    public void validationNotEnabledOnMeterActivation() {
        AllDataValidated microCheck = this.getTestInstance();
        doReturn(Optional.of(meterActivation)).when(this.device).getCurrentMeterActivation();
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(validationService.validationEnabled(meter)).thenReturn(false);

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void deviceWithMeterActivationWithAllDataValidated(){
        AllDataValidated microCheck = this.getTestInstance();
        doReturn(Optional.of(meterActivation)).when(this.device).getCurrentMeterActivation();
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        when(validationService.validationEnabled(meter)).thenReturn(true);
        when(validationService.getEvaluator()).thenReturn(validationEvaluator);
        when(validationEvaluator.isAllDataValidated(channelsContainer)).thenReturn(true);
        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void deviceWithMeterActivationWithNotAllDataValidated(){
        AllDataValidated microCheck = this.getTestInstance();
        doReturn(Optional.of(meterActivation)).when(this.device).getCurrentMeterActivation();
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        when(validationService.validationEnabled(meter)).thenReturn(true);
        when(validationService.getEvaluator()).thenReturn(validationEvaluator);
        when(validationEvaluator.isAllDataValidated(channelsContainer)).thenReturn(false);
        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.ALL_DATA_VALIDATED);
    }



    private AllDataValidated getTestInstance() {
        return new AllDataValidated(this.validationService, this.thesaurus);
    }
    

}
