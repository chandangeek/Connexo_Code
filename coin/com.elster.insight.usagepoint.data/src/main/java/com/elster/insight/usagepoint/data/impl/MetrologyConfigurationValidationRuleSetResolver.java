package com.elster.insight.usagepoint.data.impl;

import java.util.Collections;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;

/**
 * Copyrights EnergyICT
 * Date: 24/06/2014
 * Time: 17:56
 */
@Component(name = "com.elster.insight.udagepoint.data.validationruleSetResolver", service = ValidationRuleSetResolver.class)
public class MetrologyConfigurationValidationRuleSetResolver implements ValidationRuleSetResolver {

    private volatile UsagePointConfigurationService usagePointConfigurationService;

    @Reference
    public void setUsagePointConfigurationService(UsagePointConfigurationService usagePointConfigurationService) {
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    @Override
    public List<ValidationRuleSet> resolve(MeterActivation meterActivation) {
        if (meterActivation.getUsagePoint().isPresent()) {
            return usagePointConfigurationService.findMetrologyConfigurationForUsagePoint(meterActivation.getUsagePoint().get())
            .map(metrologyConfiguration -> metrologyConfiguration.getValidationRuleSets())
            .orElse(Collections.emptyList());
        }
        else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isValidationRuleSetInUse(ValidationRuleSet ruleset) {
        return !usagePointConfigurationService.findMetrologyConfigurationsForValidationRuleSet(ruleset).isEmpty();
    }

}
