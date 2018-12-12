/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.metering.config.MetrologyContract;

import java.util.Collections;
import java.util.List;

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

    private MetrologyContractEstimationRuleSetResolver testInstance() {
        MetrologyContractEstimationRuleSetResolver ruleSetResolver = new MetrologyContractEstimationRuleSetResolver();
        ruleSetResolver.setUsagePointConfigurationService(usagePointConfigurationService);
        return ruleSetResolver;
    }

    @Test
    public void testEmptyRuleSetsForMeterActivationChannelsContainer() {
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);

        List<EstimationRuleSet> estimationRuleSets = testInstance().resolve(channelsContainer);

        assertThat(estimationRuleSets).isEmpty();
    }

    @Test
    public void testEmptyRuleSetsForMetrologyContractChannelsContainer() {
        MetrologyContract metrologyContract = mock(MetrologyContract.class);
        MetrologyContractChannelsContainer channelsContainer = mock(MetrologyContractChannelsContainer.class);
        when(channelsContainer.getMetrologyContract()).thenReturn(metrologyContract);
        when(usagePointConfigurationService.getEstimationRuleSets(metrologyContract)).thenReturn(Collections.emptyList());

        List<EstimationRuleSet> estimationRuleSets = testInstance().resolve(channelsContainer);

        assertThat(estimationRuleSets).isEmpty();
    }

    @Test
    public void testCorrectRuleSetsForMetrologyContractChannelsContainer() {
        MetrologyContract metrologyContract = mock(MetrologyContract.class);
        MetrologyContractChannelsContainer channelsContainer = mock(MetrologyContractChannelsContainer.class);
        when(channelsContainer.getMetrologyContract()).thenReturn(metrologyContract);
        EstimationRuleSet estimationRuleSet = mock(EstimationRuleSet.class);
        when(usagePointConfigurationService.getEstimationRuleSets(metrologyContract)).thenReturn(Collections.singletonList(estimationRuleSet));

        List<EstimationRuleSet> resolvedEstimationRuleSets = testInstance().resolve(channelsContainer);

        assertThat(resolvedEstimationRuleSets).containsExactly(estimationRuleSet);
    }
}
