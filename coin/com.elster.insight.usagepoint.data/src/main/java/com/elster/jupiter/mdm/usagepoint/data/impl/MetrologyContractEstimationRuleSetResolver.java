package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.estimation.EstimationResolver;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.Priority;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.util.streams.DecoratedStream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.mdm.usagepoint.data.MetrologyContractEstimationRuleSetResolver", service = EstimationResolver.class, immediate = true)
public class MetrologyContractEstimationRuleSetResolver implements EstimationResolver {
    private volatile UsagePointConfigurationService usagePointConfigurationService;

    @Reference
    public void setUsagePointConfigurationService(UsagePointConfigurationService usagePointConfigurationService) {
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    @Override
    public boolean isEstimationActive(Meter meter) {
        return false;
    }

    @Override
    public List<EstimationRuleSet> resolve(MeterActivation meterActivation) {
        if (meterActivation.getChannelsContainer().getUsagePoint().isPresent()) {
            Optional<UsagePointMetrologyConfiguration> metrologyConfiguration = meterActivation.getChannelsContainer()
                    .getUsagePoint()
                    .get()
                    .getCurrentEffectiveMetrologyConfiguration()
                    .map(EffectiveMetrologyConfigurationOnUsagePoint::getMetrologyConfiguration);
            if (metrologyConfiguration.isPresent()) {
                return DecoratedStream.decorate(metrologyConfiguration.get().getContracts().stream())
                        .flatMap(contract -> this.usagePointConfigurationService.getEstimationRuleSets(contract)
                                .stream())
                        .distinct(EstimationRuleSet::getId)
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();

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
