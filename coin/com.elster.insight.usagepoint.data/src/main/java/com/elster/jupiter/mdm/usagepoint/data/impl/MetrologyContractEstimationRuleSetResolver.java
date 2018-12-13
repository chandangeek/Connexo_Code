/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.estimation.EstimationResolver;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Priority;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.elster.jupiter.mdm.usagepoint.data.MetrologyContractEstimationRuleSetResolver", service = EstimationResolver.class, immediate = true)
public class MetrologyContractEstimationRuleSetResolver implements EstimationResolver {
    private volatile UsagePointConfigurationService usagePointConfigurationService;
    private volatile EstimationService estimationService;

    @Reference
    public void setUsagePointConfigurationService(UsagePointConfigurationService usagePointConfigurationService) {
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    @Reference
    public void setEstimationService(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @Override
    public boolean isEstimationActive(Meter meter) {
        return true;
    }


    @Override
    public List<EstimationRuleSet> resolve(ChannelsContainer channelsContainer) {
        if (estimationService.isEstimationActive(channelsContainer)) {
            List<EstimationRuleSet> activeRuleSets = estimationService.activeRuleSets(channelsContainer);
            return this.getLinkedRuleSets(channelsContainer)
                    .stream()
                    .filter(activeRuleSets::contains)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private List<EstimationRuleSet> getLinkedRuleSets(ChannelsContainer channelsContainer){
        return Stream.of(channelsContainer)
                .filter(container -> container instanceof MetrologyContractChannelsContainer)
                .map(MetrologyContractChannelsContainer.class::cast)
                .map(MetrologyContractChannelsContainer::getMetrologyContract)
                .map(usagePointConfigurationService::getEstimationRuleSets)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public boolean isInUse(EstimationRuleSet ruleset) {
        return this.usagePointConfigurationService.isEstimationRuleSetInUse(ruleset);
    }

    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }

}
