package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.validation.ValidationContext;
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
    public List<ValidationRuleSet> resolve(ValidationContext validationContext) {
        if (validationContext.getMetrologyContract().isPresent()) {
            return this.usagePointConfigurationService.getValidationRuleSets(validationContext.getMetrologyContract().get());
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isValidationRuleSetInUse(ValidationRuleSet ruleset) {
        return this.usagePointConfigurationService.isValidationRuleSetInUse(ruleset);
    }
}
