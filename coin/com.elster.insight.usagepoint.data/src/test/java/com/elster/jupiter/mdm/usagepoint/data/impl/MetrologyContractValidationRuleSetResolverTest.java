package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyContractChannelsContainer;
import com.elster.jupiter.validation.ValidationRuleSet;

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

        List<ValidationRuleSet> validationRuleSets = testInstance().resolve(channelsContainer);

        assertThat(validationRuleSets).isEmpty();
    }

    @Test
    public void testCorrectRuleSetsForMetrologyContractChannelsContainer() {
        MetrologyContract metrologyContract = mock(MetrologyContract.class);
        MetrologyContractChannelsContainer channelsContainer = mock(MetrologyContractChannelsContainer.class);
        when(channelsContainer.getMetrologyContract()).thenReturn(metrologyContract);
        List<ValidationRuleSet> validationRuleSets = Collections.singletonList(mock(ValidationRuleSet.class));
        when(usagePointConfigurationService.getValidationRuleSets(metrologyContract)).thenReturn(validationRuleSets);

        List<ValidationRuleSet> resolvedValidationRuleSets = testInstance().resolve(channelsContainer);

        assertThat(resolvedValidationRuleSets).isNotEmpty();
    }
}
