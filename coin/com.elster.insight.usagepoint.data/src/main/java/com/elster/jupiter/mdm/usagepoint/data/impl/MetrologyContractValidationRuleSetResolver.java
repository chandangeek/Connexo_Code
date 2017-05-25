/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointStateChangeRequest;
import com.elster.jupiter.validation.ValidationContext;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.mdm.usagepoint.data.MetrologyContractValidationRuleSetResolver", service = ValidationRuleSetResolver.class, immediate = true)
public class MetrologyContractValidationRuleSetResolver implements ValidationRuleSetResolver {
    private volatile UsagePointConfigurationService usagePointConfigurationService;
    private volatile UsagePointLifeCycleService usagePointLifeCycleService;

    @Reference
    public void setUsagePointConfigurationService(UsagePointConfigurationService usagePointConfigurationService) {
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    @Reference
    public void setUsagePointLifeCycleService(UsagePointLifeCycleService usagePointLifeCycleService) {
        this.usagePointLifeCycleService = usagePointLifeCycleService;
    }

    @Override
    public Map<ValidationRuleSet, RangeSet<Instant>> resolve(ValidationContext validationContext) {
        if (validationContext.getMetrologyContract().isPresent() && validationContext.getUsagePoint().isPresent()) {
            return getRuleSets(validationContext.getUsagePoint().get(), Collections.singletonList(validationContext.getMetrologyContract().get()));
        }
        if (validationContext.getChannelsContainer().getUsagePoint().isPresent()) {
            Optional<UsagePointMetrologyConfiguration> metrologyConfiguration = validationContext.getChannelsContainer()
                    .getUsagePoint()
                    .get()
                    .getCurrentEffectiveMetrologyConfiguration()
                    .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration);
            if (metrologyConfiguration.isPresent()) {
                return getRuleSets(validationContext.getChannelsContainer().getUsagePoint().get(), metrologyConfiguration.get().getContracts());
            }
        }
        return Collections.emptyMap();
    }

    private Map<ValidationRuleSet, RangeSet<Instant>> getRuleSets(UsagePoint usagePoint, List<MetrologyContract> metrologyContracts) {
        Map<ValidationRuleSet, RangeSet<Instant>> result = new HashMap<>();
        List<Instant> stateChanges = getUsagePointStateChangeRequests(usagePoint).stream()
                .map(UsagePointStateChangeRequest::getTransitionTime)
                .sorted(Instant::compareTo)
                .collect(Collectors.toList());
        for (int i = 0; i < stateChanges.size(); i++) {
            Range<Instant> range;
            if (i + 1 < stateChanges.size()) {
                range = Range.openClosed(stateChanges.get(i), stateChanges.get(i + 1));
            } else {
                range = Range.greaterThan(stateChanges.get(i));
            }
            metrologyContracts.stream().forEach(metrologyContract ->
                    this.usagePointConfigurationService.getValidationRuleSets(metrologyContract,
                            usagePoint.getState(range.lowerEndpoint()))
                            .stream()
                            .forEach(e -> {
                                if (result.containsKey(e)) {
                                    result.get(e).add(range);
                                } else {
                                    result.put(e, TreeRangeSet.create());
                                    result.get(e).add(range);
                                }
                            })
            );
        }
        return result;
    }

    private List<UsagePointStateChangeRequest> getUsagePointStateChangeRequests(UsagePoint usagePoint) {
        return usagePointLifeCycleService.getHistory(usagePoint)
                .stream()
                .filter(usagePointStateChangeRequest -> usagePointStateChangeRequest.getStatus().equals(UsagePointStateChangeRequest.Status.COMPLETED))
                .sorted(Comparator.comparing(UsagePointStateChangeRequest::getTransitionTime))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isValidationRuleSetInUse(ValidationRuleSet ruleset) {
        return this.usagePointConfigurationService.isValidationRuleSetInUse(ruleset);
    }
}
