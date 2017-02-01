/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleBuilder;
import com.elster.jupiter.metering.ReadingType;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class EstimationRuleBuilderImpl implements EstimationRuleBuilder {

    private final EstimationRuleSetImpl ruleSet;
    private final String implementation;
    private final String name;
    private boolean active = true;
    private Set<ReadingType> readingTypes = new HashSet<>();
    private Set<String> readingTypeMRIDs = new HashSet<>();
    private Map<String, Object> properties = new HashMap<>();

    EstimationRuleBuilderImpl(EstimationRuleSetImpl ruleSet, String implementation, String name) {
        this.ruleSet = ruleSet;
        this.implementation = implementation;
        this.name = name;
    }

    @Override
    public EstimationRuleBuilder active(boolean active) {
        this.active = active;
        return this;
    }

    @Override
    public EstimationRuleBuilder withReadingType(ReadingType... readingType) {
        Arrays.stream(readingType)
                .forEach(readingTypes::add);
        return this;
    }

    @Override
    public EstimationRuleBuilder withReadingType(String... readingTypeMRID) {
        Arrays.stream(readingTypeMRID)
                .forEach(readingTypeMRIDs::add);
        return this;
    }

    @Override
    public EstimationRuleBuilder withReadingTypes(Collection<ReadingType> readingTypes) {
        this.readingTypes.addAll(readingTypes);
        return this;
    }

    @Override
    public EstimationRuleBuilder withProperties(Map<String, Object> properties) {
        this.properties.putAll(properties);
        return this;
    }

    @Override
    public PropertyBuilder havingProperty(String property) {
        return value -> {
            properties.put(property, value);
            return EstimationRuleBuilderImpl.this;
        };
    }

    @Override
    public EstimationRule create() {
        EstimationRule rule = ruleSet.newRule(implementation, name);
        readingTypes.forEach(rule::addReadingType);
        readingTypeMRIDs.forEach(rule::addReadingType);
        properties.forEach(rule::addProperty);
        if (active) {
            rule.activate();
        }
        ruleSet.save();
        return rule;
    }

}
