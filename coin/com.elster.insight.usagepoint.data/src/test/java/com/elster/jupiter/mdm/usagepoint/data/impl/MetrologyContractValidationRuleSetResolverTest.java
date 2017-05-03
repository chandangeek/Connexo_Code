/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeRequest;
import com.elster.jupiter.validation.ValidationContext;
import com.elster.jupiter.validation.ValidationRuleSet;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetrologyContractValidationRuleSetResolverTest {


    private final Instant stateTransition1 = Instant.ofEpochMilli(1491228423000L);
    private final Instant stateTransition2 = Instant.ofEpochMilli(1492697223000L);
    private final Instant stateTransition3 = Instant.ofEpochMilli(1494425223000L);

    @Mock
    private UsagePointConfigurationService usagePointConfigurationService;

    @Mock
    private UsagePointLifeCycleService usagePointLifeCycleService;

    private MetrologyContractValidationRuleSetResolver testInstance() {
        MetrologyContractValidationRuleSetResolver ruleSetResolver = new MetrologyContractValidationRuleSetResolver();
        ruleSetResolver.setUsagePointConfigurationService(usagePointConfigurationService);
        ruleSetResolver.setUsagePointLifeCycleService(usagePointLifeCycleService);
        return ruleSetResolver;
    }

    @Test
    public void testEmptyRuleSetsForSimpleChannelContainer() {
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        ValidationContext validationContext = mock(ValidationContext.class);
        when(validationContext.getChannelsContainer()).thenReturn(channelsContainer);
        when(validationContext.getMetrologyContract()).thenReturn(Optional.empty());
        when(validationContext.getChannelsContainer().getUsagePoint()).thenReturn(Optional.empty());
        Map<ValidationRuleSet, List<Range<Instant>>> validationRuleSets = testInstance().resolve(validationContext);

        assertThat(validationRuleSets).isEmpty();
    }

    @Test
    public void testCorrectRuleSetsForMetrologyContractChannelsContainer() {
        MetrologyContract metrologyContract = mock(MetrologyContract.class);
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        UsagePoint usagePoint = mock(UsagePoint.class);
        UsagePointStateChangeRequest stateChangeRequest1 = mock(UsagePointStateChangeRequest.class);
        when(stateChangeRequest1.getStatus()).thenReturn(UsagePointStateChangeRequest.Status.COMPLETED);
        when(stateChangeRequest1.getTransitionTime()).thenReturn(stateTransition1);
        UsagePointStateChangeRequest stateChangeRequest2 = mock(UsagePointStateChangeRequest.class);
        when(stateChangeRequest2.getStatus()).thenReturn(UsagePointStateChangeRequest.Status.COMPLETED);
        when(stateChangeRequest2.getTransitionTime()).thenReturn(stateTransition2);
        UsagePointStateChangeRequest stateChangeRequest3 = mock(UsagePointStateChangeRequest.class);
        when(stateChangeRequest3.getStatus()).thenReturn(UsagePointStateChangeRequest.Status.SCHEDULED);
        when(stateChangeRequest3.getTransitionTime()).thenReturn(stateTransition3);
        when(usagePointLifeCycleService.getHistory(usagePoint)).thenReturn(Arrays.asList(stateChangeRequest1, stateChangeRequest2));
        State state1 = mock(State.class);
        when(usagePoint.getState(stateTransition1)).thenReturn(state1);
        State state2 = mock(State.class);
        when(usagePoint.getState(stateTransition2)).thenReturn(state2);
        State state3 = mock(State.class);
        when(usagePoint.getState(stateTransition3)).thenReturn(state3);
        ValidationContext validationContext = mock(ValidationContext.class);
        when(validationContext.getChannelsContainer()).thenReturn(channelsContainer);
        when(validationContext.getMetrologyContract()).thenReturn(Optional.of(metrologyContract));
        when(validationContext.getUsagePoint()).thenReturn(Optional.of(usagePoint));

        ValidationRuleSet validationRuleSet1 = mock(ValidationRuleSet.class);

        ValidationRuleSet validationRuleSet2 = mock(ValidationRuleSet.class);


        List<ValidationRuleSet> validationRuleSets1 = Collections.singletonList(validationRuleSet1);
        when(usagePointConfigurationService.getValidationRuleSets(metrologyContract, state1)).thenReturn(validationRuleSets1);

        List<ValidationRuleSet> validationRuleSets2 = Collections.singletonList(validationRuleSet2);
        when(usagePointConfigurationService.getValidationRuleSets(metrologyContract, state2)).thenReturn(validationRuleSets2);

        Map<ValidationRuleSet, List<Range<Instant>>> resolvedValidationRuleSets = testInstance().resolve(validationContext);

        assertThat(resolvedValidationRuleSets).isNotEmpty();
        assertThat(resolvedValidationRuleSets).hasSize(2);
        assertThat(resolvedValidationRuleSets).containsKeys(validationRuleSet1, validationRuleSet2);
        assertThat(resolvedValidationRuleSets.get(validationRuleSet1)).containsExactlyElementsOf(Collections.singletonList(Range.openClosed(stateTransition1, stateTransition2)));
        assertThat(resolvedValidationRuleSets.get(validationRuleSet2)).containsExactlyElementsOf(Collections.singletonList(Range.atLeast(stateTransition2)));
    }
}
