/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
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
    public void testEmptyRuleSetsForSimpleChannelContainer() {
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        MeterActivation meterActivation = mock(MeterActivation.class);
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        when(meterActivation.getChannelsContainer().getUsagePoint()).thenReturn(Optional.empty());
        List<EstimationRuleSet> estimationRuleSets = testInstance().resolve(meterActivation.getChannelsContainer());

        assertThat(estimationRuleSets).isEmpty();
    }

    @Test
    public void testCorrectRuleSetsForMetrologyContractChannelsContainer() {
        MetrologyContract metrologyContract = mock(MetrologyContract.class);
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        UsagePoint usagePoint = mock(UsagePoint.class);
        UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        MeterActivation meterActivation = mock(MeterActivation.class);
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        when(channelsContainer.getUsagePoint()).thenReturn(Optional.of(usagePoint));
        doReturn(Optional.of(metrologyConfiguration)).when(usagePoint).getEffectiveMetrologyConfiguration(any());
        when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(effectiveMetrologyConfigurationOnUsagePoint.getUsagePoint()).thenReturn(usagePoint);
        when(effectiveMetrologyConfigurationOnUsagePoint.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfigurationOnUsagePoint));
        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));

        List<EstimationRuleSet> estimationRuleSets = Collections.singletonList(mock(EstimationRuleSet.class));
        when(usagePointConfigurationService.getEstimationRuleSets(metrologyContract)).thenReturn(estimationRuleSets);
        List<EstimationRuleSet> resolvedEstimationRuleSets = testInstance().resolve(meterActivation.getChannelsContainer());

        assertThat(resolvedEstimationRuleSets).isNotEmpty();
    }
}
