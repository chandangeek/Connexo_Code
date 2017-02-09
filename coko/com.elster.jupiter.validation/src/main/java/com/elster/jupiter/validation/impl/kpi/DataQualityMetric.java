/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.util.HasName;

import java.util.Objects;

interface DataQualityMetric extends HasName {

}

enum DefaultDataQualityMetric implements DataQualityMetric {
    SUSPECT,
    MISSING,
    INFORMATIVE,
    ADDED,
    EDITED,
    REMOVED,
    ESTIMATED,
    CONFIRMED,
    UNKNOWN;

    @Override
    public String getName() {
        return name();
    }
}

class ValidatorType implements DataQualityMetric {

    private final String validator;

    ValidatorType(String validator) {
        this.validator = validator;
    }

    @Override
    public String getName() {
        return validator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ValidatorType that = (ValidatorType) o;
        return Objects.equals(validator, that.validator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(validator);
    }
}

class EstimatorType implements DataQualityMetric {

    private final String estimator;

    EstimatorType(String estimator) {
        this.estimator = estimator;
    }

    @Override
    public String getName() {
        return estimator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EstimatorType that = (EstimatorType) o;
        return Objects.equals(estimator, that.estimator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(estimator);
    }
}
