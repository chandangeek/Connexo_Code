/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.builders;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.time.TimeService;

import javax.inject.Inject;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

public class EstimationRuleSetBuilder extends NamedBuilder<EstimationRuleSet, EstimationRuleSetBuilder> {

    private final EstimationService estimationService;
    private final TimeService timeService;
    private String description;

    @Inject
    public EstimationRuleSetBuilder(EstimationService estimationService, TimeService timeService) {
        super(EstimationRuleSetBuilder.class);
        this.estimationService = estimationService;
        this.timeService = timeService;
    }

    public EstimationRuleSetBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public Optional<EstimationRuleSet> find() {
        return estimationService.getEstimationRuleSetQuery()
                .select(where("name").isEqualTo(getName()))
                .stream().map(EstimationRuleSet.class::cast)
                .findFirst();
    }

    @Override
    public EstimationRuleSet create() {
        EstimationRuleSet ruleSet = estimationService.createEstimationRuleSet(getName(), QualityCodeSystem.MDC, description);
        ruleSet.save();
        applyPostBuilders(ruleSet);
        return ruleSet;
    }
}
