/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl.micro.checks;

import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheckViolation;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetrologyConfigInCorrectStateTest {
    private static final String INSIGHT_LICENSE = "INS";
    private MetrologyConfigurationInCorrectStateIfAny checkObject;

    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
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
    @Mock
    private LicenseService licenseService;

    @Before
    public void setUp() {
        when(licenseService.getLicensedApplicationKeys()).thenReturn(Collections.singletonList(INSIGHT_LICENSE));
        checkObject = new MetrologyConfigurationInCorrectStateIfAny(licenseService);
        checkObject.setThesaurus(thesaurus);

        device = mockDevice();
        usagePoint = mockUsagePoint();
        meterActivation = mockMeterActivation();
    }

    @Test
    public void stageIsNotPresent() {
        when(state.getStage()).thenReturn(Optional.empty());

        Optional<ExecutableMicroCheckViolation> result = checkObject.execute(device, Instant.EPOCH, state);

        assertThat(result).isEmpty();
    }

    @Test
    public void stageIsOperational() {
        when(stage.getName()).thenReturn(EndDeviceStage.OPERATIONAL.getKey());

        Optional<ExecutableMicroCheckViolation> result = checkObject.execute(device, Instant.EPOCH, state);

        assertThat(result).isEmpty();
    }

    @Test
    public void noMeterActivation() {
        when(device.getMeterActivation(Instant.EPOCH)).thenReturn(Optional.empty());

        Optional<ExecutableMicroCheckViolation> result = checkObject.execute(device, Instant.EPOCH, state);

        assertThat(result).isEmpty();
    }

    @Test
    public void noUsagePoint() {
        when(meterActivation.getUsagePoint()).thenReturn(Optional.empty());

        Optional<ExecutableMicroCheckViolation> result = checkObject.execute(device, Instant.EPOCH, state);

        assertThat(result).isEmpty();
    }

    @Test
    public void noActiveMetrologyConfiguration() {
        when(usagePoint.getEffectiveMetrologyConfiguration(Instant.EPOCH)).thenReturn(Optional.empty());

        Optional<ExecutableMicroCheckViolation> result = checkObject.execute(device, Instant.EPOCH, state);

        assertThat(result).isEmpty();
    }


    @Test
    public void usagePointStageIsPostOperational() {
        Stage usagePointStage = mock(Stage.class);
        State usagePointState = mock(State.class);
        when(usagePoint.getState()).thenReturn(usagePointState);
        when(usagePointState.getStage()).thenReturn(Optional.of(usagePointStage));
        when(usagePointStage.getName()).thenReturn(UsagePointStage.POST_OPERATIONAL.getKey());

        Optional<ExecutableMicroCheckViolation> result = checkObject.execute(device, Instant.EPOCH, state);

        assertThat(result).isEmpty();
    }

    @Test
    public void actionViolationConditionsAreMet() {
        Optional<ExecutableMicroCheckViolation> result = checkObject.execute(device, Instant.EPOCH, state);

        assertThat(result).isPresent();
    }

    @Test
    public void noInsightLicense() {
        when(licenseService.getLicensedApplicationKeys()).thenReturn(Collections.emptyList());

        Optional<ExecutableMicroCheckViolation> result = checkObject.execute(device, Instant.EPOCH, state);

        assertThat(result).isEmpty();
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
