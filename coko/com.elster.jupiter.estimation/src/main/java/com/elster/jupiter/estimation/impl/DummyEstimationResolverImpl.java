package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationResolver;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.orm.callback.InstallService;
import com.google.common.collect.ImmutableList;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

@Component(name = "com.elster.jupiter.estimation.dummyresolver", service = {InstallService.class, EstimationService.class}, property = "name=" + EstimationService.COMPONENTNAME, immediate = true)
public class DummyEstimationResolverImpl implements EstimationResolver {

    private volatile EstimationService estimationService;

    @Reference
    public void setEstimationService(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @Override
    public List<EstimationRuleSet> resolve(MeterActivation meterActivation) {
        return ImmutableList.copyOf(estimationService.getEstimationRuleSets());
    }

    @Override
    public boolean isInUse(EstimationRuleSet estimationRuleSet) {
        return false;
    }
}
