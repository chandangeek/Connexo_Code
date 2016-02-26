package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;

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
            return usagePointConfigurationService
                    .findMetrologyConfigurationForUsagePoint(meterActivation.getUsagePoint().get())
                    .map(metrologyConfiguration -> this.usagePointConfigurationService.getValidationRuleSets(metrologyConfiguration))
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
