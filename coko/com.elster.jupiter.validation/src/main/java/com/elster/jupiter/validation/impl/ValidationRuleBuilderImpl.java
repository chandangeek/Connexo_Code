/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class ValidationRuleBuilderImpl implements ValidationRuleBuilder {

    private final ValidationRuleSetVersionImpl validationRuleSetVersion;
    private final ValidationAction action;
    private final String name;
    private final String implementation;
    private boolean active;
    private final Set<ReadingType> readingTypes = new HashSet<>();
    private final Set<String> readingTypeMRIDs = new HashSet<>();
    private final Map<String, Object> properties = new HashMap<>();

    ValidationRuleBuilderImpl(ValidationRuleSetVersionImpl validationRuleSetVersion, ValidationAction action, String implementation, String name) {
        this.validationRuleSetVersion = validationRuleSetVersion;
        this.action = action;
        this.implementation = implementation;
        this.name = name;
    }

    @Override
    public ValidationRuleBuilder active(boolean active) {
        this.active = active;
        return this;
    }

    @Override
    public ValidationRuleBuilder withReadingType(ReadingType... readingType) {
        Arrays.stream(readingType)
                .forEach(readingTypes::add);
        return this;
    }

    @Override
    public ValidationRuleBuilder withReadingType(String... readingTypeMRID) {
        Arrays.stream(readingTypeMRID)
                .forEach(readingTypeMRIDs::add);
        return this;
    }

    @Override
    public ValidationRuleBuilder withReadingTypes(Collection<ReadingType> readingTypes) {
        this.readingTypes.addAll(readingTypes);
        return this;
    }

    @Override
    public ValidationRuleBuilder withProperties(Map<String, Object> properties) {
        this.properties.putAll(properties);
        return this;
    }

    @Override
    public PropertyBuilder havingProperty(String property) {
        return value -> {
            properties.put(property, value);
            return ValidationRuleBuilderImpl.this;
        };
    }

    @Override
    public ValidationRule create() {
        ValidationRule rule = validationRuleSetVersion.newRule(action, implementation, name);
        readingTypes.forEach(rule::addReadingType);
        readingTypeMRIDs.forEach(rule::addReadingType);
        properties.forEach(rule::addProperty);
        if (active) {
            rule.activate();
        }
        validationRuleSetVersion.save();
        return rule;
    }
}
