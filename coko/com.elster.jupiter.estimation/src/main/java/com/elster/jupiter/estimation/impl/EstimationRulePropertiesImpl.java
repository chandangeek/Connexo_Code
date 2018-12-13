/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleProperties;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

class EstimationRulePropertiesImpl implements EstimationRuleProperties {

    private String name;
    private String stringValue;
    private transient PropertySpec propertySpec;

    private Reference<EstimationRule> rule = ValueReference.absent();
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;

    @Inject
    EstimationRulePropertiesImpl() {
    }

    EstimationRulePropertiesImpl init(EstimationRuleImpl rule, String name, Object value) {
        return init(rule, rule.getPropertySpec(name), value);
    }

    EstimationRulePropertiesImpl init(EstimationRule rule, PropertySpec propertySpec, Object value) {
        this.rule.set(rule);
        this.name = propertySpec.getName();
        this.propertySpec = propertySpec;
        setValue(value);
        return this;
    }

    PropertySpec getPropertySpec() {
        if (propertySpec == null) {
            propertySpec = ((IEstimationRule) rule.get()).getPropertySpec(name);
        }
        return propertySpec;
    }

    @Override
    public EstimationRule getRule() {
        return rule.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return ((IEstimationRule) getRule()).getPropertySpec(name).getDisplayName();
    }

    @Override
    public String getDescription() {
        return ((IEstimationRule) getRule()).getPropertySpec(name).getDescription();
    }

    @Override
    public Object getValue() {
        return getPropertySpec().getValueFactory().fromStringValue(stringValue);
    }

    @Override
    public void setValue(Object value) {
        if (BigDecimal.class.equals(getPropertySpec().getValueFactory().getValueType())) {
            this.stringValue = toStringValue(new BigDecimal(value.toString()));
            return;
        }
        this.stringValue = toStringValue(value);
    }

    @SuppressWarnings("unchecked")
    private String toStringValue(Object object) {
        return getPropertySpec().getValueFactory().toStringValue(object);
    }

    @Override
    public String toString() {
        return getName() + ": " + getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EstimationRulePropertiesImpl that = (EstimationRulePropertiesImpl) o;

        return getRule().getId() == that.getRule().getId() && name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
