/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.validation.ValidationContext;
import com.elster.jupiter.validation.ValidationRuleSet;

import java.util.Collections;
import java.util.List;
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

    @Mock
    private UsagePointConfigurationService usagePointConfigurationService;

    private MetrologyContractValidationRuleSetResolver testInstance() {
        MetrologyContractValidationRuleSetResolver ruleSetResolver = new MetrologyContractValidationRuleSetResolver();
        ruleSetResolver.setUsagePointConfigurationService(usagePointConfigurationService);
        return ruleSetResolver;
    }

    @Test
    public void testEmptyRuleSetsForSimpleChannelContainer() {
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        ValidationContext validationContext = mock(ValidationContext.class);
        when(validationContext.getChannelsContainer()).thenReturn(channelsContainer);
        when(validationContext.getMetrologyContract()).thenReturn(Optional.empty());
        when(validationContext.getChannelsContainer().getUsagePoint()).thenReturn(Optional.empty());
        List<ValidationRuleSet> validationRuleSets = testInstance().resolve(validationContext);

        assertThat(validationRuleSets).isEmpty();
    }

    @Test
    public void testCorrectRuleSetsForMetrologyContractChannelsContainer() {
        MetrologyContract metrologyContract = mock(MetrologyContract.class);
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        ValidationContext validationContext = mock(ValidationContext.class);
        when(validationContext.getChannelsContainer()).thenReturn(channelsContainer);
        when(validationContext.getMetrologyContract()).thenReturn(Optional.of(metrologyContract));

        List<ValidationRuleSet> validationRuleSets = Collections.singletonList(mock(ValidationRuleSet.class));
        when(usagePointConfigurationService.getValidationRuleSets(metrologyContract)).thenReturn(validationRuleSets);

        List<ValidationRuleSet> resolvedValidationRuleSets = testInstance().resolve(validationContext);

        assertThat(resolvedValidationRuleSets).isNotEmpty();
    }
}
