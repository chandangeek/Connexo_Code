package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleActionViolation;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;

import com.elster.jupiter.nls.Thesaurus;

import java.time.Instant;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;

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

    @Test
    public void deviceWithoutMeterActivation() {
        AllDataValid microCheck = this.getTestInstance();
        doReturn(Optional.empty()).when(this.device).getCurrentMeterActivation();

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.ALL_DATA_VALID);
    }

    @Test
    public void validationNotEnabledOnMeterActivation() {
        AllDataValid microCheck = this.getTestInstance();
        doReturn(Optional.of(meterActivation)).when(this.device).getCurrentMeterActivation();
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(validationService.validationEnabled(meter)).thenReturn(false);

        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void deviceWithMeterActivationWithAllDataValid(){
        AllDataValid microCheck = this.getTestInstance();
        doReturn(Optional.of(meterActivation)).when(this.device).getCurrentMeterActivation();
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(validationService.validationEnabled(meter)).thenReturn(true);
        when(validationService.getEvaluator()).thenReturn(validationEvaluator);
        when(validationEvaluator.isAllDataValid(meterActivation)).thenReturn(true);
        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        // Asserts
        assertThat(violation).isEmpty();
    }

    @Test
    public void deviceWithMeterActivationWithNotAllDataValid(){
        AllDataValid microCheck = this.getTestInstance();
        doReturn(Optional.of(meterActivation)).when(this.device).getCurrentMeterActivation();
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(validationService.validationEnabled(meter)).thenReturn(true);
        when(validationService.getEvaluator()).thenReturn(validationEvaluator);
        when(validationEvaluator.isAllDataValid(meterActivation)).thenReturn(false);
        // Business method
        Optional<DeviceLifeCycleActionViolation> violation = microCheck.evaluate(this.device, Instant.now());

        assertThat(violation).isPresent();
        assertThat(violation.get().getCheck()).isEqualTo(MicroCheck.ALL_DATA_VALID);
    }

    private AllDataValid getTestInstance() {
        return new AllDataValid(this.validationService, this.thesaurus);
    }

}