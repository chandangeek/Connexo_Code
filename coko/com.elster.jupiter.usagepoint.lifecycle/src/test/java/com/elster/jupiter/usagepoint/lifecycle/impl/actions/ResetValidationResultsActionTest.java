/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategory;
import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategoryTranslationKeys;
import com.elster.jupiter.usagepoint.lifecycle.impl.UsagePointLifeCycleServiceImpl;
import com.elster.jupiter.validation.ValidationService;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResetValidationResultsActionTest {
    @Mock
    private ValidationService validationService;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private State state;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMC1, effectiveMC2;
    @Mock
    private UsagePointMetrologyConfiguration mc1, mc2;
    @Mock
    private MetrologyContract contract1, contract21, contract22;
    @Mock
    private ChannelsContainer container1, container2;

    private TranslatableAction action;

    @Before
    public void setUp() {
        when(usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(Arrays.asList(effectiveMC1, effectiveMC2));
        when(effectiveMC1.getMetrologyConfiguration()).thenReturn(mc1);
        when(effectiveMC2.getMetrologyConfiguration()).thenReturn(mc2);
        when(mc1.getContracts()).thenReturn(Collections.singletonList(contract1));
        when(mc2.getContracts()).thenReturn(Arrays.asList(contract21, contract22));
        when(effectiveMC1.getChannelsContainer(contract1)).thenReturn(Optional.of(container1));
        when(effectiveMC2.getChannelsContainer(contract21)).thenReturn(Optional.empty());
        when(effectiveMC2.getChannelsContainer(contract22)).thenReturn(Optional.of(container2));

        action = new ResetValidationResultsAction(validationService);
        action.setThesaurus(NlsModule.SimpleThesaurus.from(new UsagePointLifeCycleServiceImpl().getKeys()));
    }

    @Test
    public void testInfo() {
        assertThat(action.getKey()).isEqualTo(ResetValidationResultsAction.class.getSimpleName());
        assertThat(action.getName()).isEqualTo(MicroActionTranslationKeys.RESET_VALIDATION_RESULTS_NAME.getDefaultFormat());
        assertThat(action.getDescription()).isEqualTo(MicroActionTranslationKeys.RESET_VALIDATION_RESULTS_DESCRIPTION.getDefaultFormat());
    }

    @Test
    public void testCategory() {
        assertThat(action.getCategory()).isEqualTo(MicroCategory.VALIDATION.name());
        assertThat(action.getCategoryName()).isEqualTo(MicroCategoryTranslationKeys.VALIDATION_NAME.getDefaultFormat());
    }

    @Test
    public void testMandatory() {
        assertThat(action.isMandatoryForTransition(state, state)).isTrue();
    }

    @Test
    public void testProperties() {
        assertThat(action.getPropertySpecs()).isEmpty();
    }

    @Test
    public void testExecution() {
        Instant now = Instant.now();

        action.execute(usagePoint, now, Collections.emptyMap());

        verify(validationService).moveLastCheckedBefore(container1, now.plusMillis(1));
        verify(validationService).moveLastCheckedBefore(container2, now.plusMillis(1));
        verifyNoMoreInteractions(validationService);
    }
}
