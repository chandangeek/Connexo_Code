/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;


import com.elster.jupiter.demo.impl.builders.EstimationRuleSetBuilder;
import com.elster.jupiter.estimation.EstimationRuleSet;


public enum EstimationRuleSetTpl implements Template<EstimationRuleSet, EstimationRuleSetBuilder> {

    RESIDENTIAL_CUSTOMERS("Residential electricity", "Set with rules regarding residential electricity customers"),
    RESIDENTIAL_GAS("Residential gas", "Set with rules regarding residential gas customers"),
    RESIDENTIAL_WATER("Residential water", "Set with rules regarding residential water customers");

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
