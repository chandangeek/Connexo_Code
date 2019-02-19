/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.nls.Thesaurus;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.EvaluableMicroCheckViolation;

import java.time.Instant;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetrologyConfigInCorrectStateTest {

    private MetrologyConfigurationInCorrectStateIfAny checkObject;

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Device device;
    @Mock
    private State state;
    @Mock
    private Stage stage;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private UsagePoint usagePoint;


    @Before
    public void setUp() {
        checkObject = new MetrologyConfigurationInCorrectStateIfAny();
        checkObject.setThesaurus(thesaurus);

        device = mockDevice();
        usagePoint = mockUsagePoint();
        meterActivation = mockMeterActivation();
    }


    @Test(expected = IllegalArgumentException.class)
    public void callEvaluateWithoutStateArgument() {
        checkObject.evaluate(device, Instant.EPOCH);
    }

    @Test
    public void stageIsNotPresent() {
        when(state.getStage()).thenReturn(Optional.empty());

        Optional<EvaluableMicroCheckViolation> result = checkObject.evaluate(device, Instant.EPOCH, state);

        assertFalse(result.isPresent());
    }

    @Test
    public void stageIsOperational() {
        when(stage.getName()).thenReturn(EndDeviceStage.OPERATIONAL.getKey());

        Optional<EvaluableMicroCheckViolation> result = checkObject.evaluate(device, Instant.EPOCH, state);

        assertFalse(result.isPresent());
    }

    @Test
    public void noMeterActivation() {
        when(device.getMeterActivation(Instant.EPOCH)).thenReturn(Optional.empty());

        Optional<EvaluableMicroCheckViolation> result = checkObject.evaluate(device, Instant.EPOCH, state);

        assertFalse(result.isPresent());
    }

    @Test
    public void noUsagePoint() {
        when(meterActivation.getUsagePoint()).thenReturn(Optional.empty());

        Optional<EvaluableMicroCheckViolation> result = checkObject.evaluate(device, Instant.EPOCH, state);

        assertFalse(result.isPresent());
    }

    @Test
    public void noActiveMetrologyConfiguration() {
        when(usagePoint.getEffectiveMetrologyConfiguration(Instant.EPOCH)).thenReturn(Optional.empty());

        Optional<EvaluableMicroCheckViolation> result = checkObject.evaluate(device, Instant.EPOCH, state);

        assertFalse(result.isPresent());
    }


    @Test
    public void usagePointStageIsPostOperational() {
        Stage usagePointStage = mock(Stage.class);
        State usagePointState = mock(State.class);
        when(usagePoint.getState()).thenReturn(usagePointState);
        when(usagePointState.getStage()).thenReturn(Optional.of(usagePointStage));
        when(usagePointStage.getName()).thenReturn(UsagePointStage.POST_OPERATIONAL.getKey());

        Optional<EvaluableMicroCheckViolation> result = checkObject.evaluate(device, Instant.EPOCH, state);

        assertFalse(result.isPresent());
    }

    @Test
    public void actionViolationConditionsAreMet() {
        Optional<EvaluableMicroCheckViolation> result = checkObject.evaluate(device, Instant.EPOCH, state);

        assertTrue(result.isPresent());
    }

    private MeterActivation mockMeterActivation() {
        when(meterActivation.getUsagePoint()).thenReturn(Optional.of(usagePoint));
        return meterActivation;
    }

    private Device mockDevice() {
        doReturn(Optional.of(meterActivation)).when(device).getMeterActivation(Instant.EPOCH);
        when(state.getStage()).thenReturn(Optional.of(stage));
        when(stage.getName()).thenReturn(EndDeviceStage.POST_OPERATIONAL.getKey());
        return device;
    }

    private UsagePoint mockUsagePoint() {
        State usagePointState = mock(State.class);
        Stage usagePointStage = mock(Stage.class);
        when(usagePoint.getEffectiveMetrologyConfiguration(Instant.EPOCH)).thenReturn(Optional.of(mock(EffectiveMetrologyConfigurationOnUsagePoint.class)));
        when(usagePoint.getState()).thenReturn(usagePointState);
        when(usagePointState.getStage()).thenReturn(Optional.of(usagePointStage));
        when(usagePointStage.getName()).thenReturn(UsagePointStage.OPERATIONAL.getKey());
        return usagePoint;
    }
}