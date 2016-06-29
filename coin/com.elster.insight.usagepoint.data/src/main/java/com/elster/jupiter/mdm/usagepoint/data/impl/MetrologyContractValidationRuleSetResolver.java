package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyContractChannelsContainer;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;

@Component(name = "com.elster.jupiter.mdm.usagepoint.data.MetrologyContractValidationRuleSetResolver", service = ValidationRuleSetResolver.class, immediate = true)
public class MetrologyContractValidationRuleSetResolver implements ValidationRuleSetResolver {
    private volatile UsagePointConfigurationService usagePointConfigurationService;

    @Reference
    public void setUsagePointConfigurationService(UsagePointConfigurationService usagePointConfigurationService) {
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    @Override
    public List<ValidationRuleSet> resolve(ChannelsContainer channelsContainer) {
        if (channelsContainer instanceof MetrologyContractChannelsContainer) {
            MetrologyContract metrologyContract = ((MetrologyContractChannelsContainer) channelsContainer).getMetrologyContract();
            // TODO provide a list of validation rulesets assigned to that metrology contract.
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isValidationRuleSetInUse(ValidationRuleSet ruleset) {
        return false;
    }
}
