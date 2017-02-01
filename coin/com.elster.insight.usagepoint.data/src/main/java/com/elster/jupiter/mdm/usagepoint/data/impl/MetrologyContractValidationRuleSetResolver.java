/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.util.streams.DecoratedStream;
import com.elster.jupiter.validation.ValidationContext;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        if (validationContext.getChannelsContainer().getUsagePoint().isPresent()) {
            Optional<UsagePointMetrologyConfiguration> metrologyConfiguration = validationContext.getChannelsContainer().getUsagePoint().get().getCurrentEffectiveMetrologyConfiguration()
                    .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration);
            if (metrologyConfiguration.isPresent()){
                return DecoratedStream.decorate(metrologyConfiguration.get().getContracts().stream())
                        .flatMap(contract -> this.usagePointConfigurationService.getValidationRuleSets(contract).stream())
                        .distinct(ValidationRuleSet::getId)
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isValidationRuleSetInUse(ValidationRuleSet ruleset) {
        return this.usagePointConfigurationService.isValidationRuleSetInUse(ruleset);
    }
}
