/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;


import com.elster.jupiter.demo.impl.builders.EstimationRuleSetBuilder;
import com.elster.jupiter.estimation.EstimationRuleSet;


public enum EstimationRuleSetTpl implements Template<EstimationRuleSet, EstimationRuleSetBuilder> {

    RESIDENTIAL_CUSTOMERS("Residential customers", "Set with rules regarding residential customers"),
    RESIDENTIAL_CUSTOMERS_STRICT("Residential customers strict", "Set with strict rules regarding residential customers");

    private String name;
    private String description;

    EstimationRuleSetTpl(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public Class<EstimationRuleSetBuilder> getBuilderClass() {
        return EstimationRuleSetBuilder.class;
    }

    @Override
    public EstimationRuleSetBuilder get(EstimationRuleSetBuilder builder) {
        return builder.withName(this.name).withDescription(this.description);
    }

    public String getName() {
        return name;
    }
}
