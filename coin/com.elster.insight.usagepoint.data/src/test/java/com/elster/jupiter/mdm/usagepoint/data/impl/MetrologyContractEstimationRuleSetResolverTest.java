/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.config.MetrologyContract;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetrologyContractEstimationRuleSetResolverTest {

    @Mock
    private UsagePointConfigurationService usagePointConfigurationService;
    @Mock
    private EstimationService estimationService;
    @Mock
    private MetrologyContract metrologyContract;
    @Mock
    private MetrologyContractChannelsContainer channelsContainer;

    @Before
    public void setUp(){
        when(channelsContainer.getMetrologyContract()).thenReturn(metrologyContract);
        when(estimationService.isEstimationActive(channelsContainer)).thenReturn(true);
        when(estimationService.activeRuleSets(channelsContainer)).thenReturn(Collections.emptyList());
        when(usagePointConfigurationService.getEstimationRuleSets(metrologyContract)).thenReturn(Collections.emptyList());
    }

    private MetrologyContractEstimationRuleSetResolver testInstance() {
        MetrologyContractEstimationRuleSetResolver ruleSetResolver = new MetrologyContractEstimationRuleSetResolver();
        ruleSetResolver.setUsagePointConfigurationService(usagePointConfigurationService);
        ruleSetResolver.setEstimationService(estimationService);
        return ruleSetResolver;
    }

    @Test
    public void whenMeterActivationChannels_thenEmptyRuleSets() {
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);

        List<EstimationRuleSet> estimationRuleSets = testInstance().resolve(channelsContainer);

        assertThat(estimationRuleSets).isEmpty();
    }

    @Test
    public void whenInactiveEstimation_thenEmptyRuleSets() {
        when(estimationService.isEstimationActive(channelsContainer)).thenReturn(false);

        List<EstimationRuleSet> estimationRuleSets = testInstance().resolve(channelsContainer);

        assertThat(estimationRuleSets).isEmpty();
    }

    @Test
    public void whenInactiveRuleSets_thenEmptyRuleSets() {
        List<EstimationRuleSet> estimationRuleSets = testInstance().resolve(channelsContainer);

        assertThat(estimationRuleSets).isEmpty();
    }


    @Test
    public void whenActiveEstimationAndRuleSets_thenReturnUniqueRuleSets() {
        EstimationRuleSet estimationRuleSet = mock(EstimationRuleSet.class);
        when(usagePointConfigurationService.getEstimationRuleSets(metrologyContract)).thenReturn(Collections.singletonList(estimationRuleSet));
        when(estimationService.activeRuleSets(channelsContainer)).thenReturn(Arrays.asList(estimationRuleSet, estimationRuleSet));

        List<EstimationRuleSet> resolvedEstimationRuleSets = testInstance().resolve(channelsContainer);

        assertThat(resolvedEstimationRuleSets).containsExactly(estimationRuleSet);
    }
}
