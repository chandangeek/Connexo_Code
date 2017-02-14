/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.validation.Validator;

import java.util.Objects;

interface DataQualityMetric {

}

enum PredefinedDataQualityMetric implements DataQualityMetric {

    SUSPECT(QualityCodeIndex.SUSPECT),
    INFORMATIVE,
    ADDED(QualityCodeIndex.ADDED),
    EDITED(QualityCodeIndex.EDITGENERIC),
    REMOVED(QualityCodeIndex.REJECTED),
    ESTIMATED(QualityCodeIndex.ESTIMATEGENERIC),
    CONFIRMED(QualityCodeIndex.ACCEPTED),
    UNKNOWN;

    private QualityCodeIndex qualityCodeIndex;

    PredefinedDataQualityMetric() {
    }

    PredefinedDataQualityMetric(QualityCodeIndex qualityCodeIndex) {
        this();
        this.qualityCodeIndex = qualityCodeIndex;
    }

    boolean accept(ReadingQualityType readingQualityType) {
        return this.qualityCodeIndex != null &&
                readingQualityType.qualityIndex().map(this.qualityCodeIndex::equals).orElse(false);
    }
}

class ValidatorDataQualityMetric implements DataQualityMetric {

    private final String validator;

    ValidatorDataQualityMetric(String validatorImplementation) {
        this.validator = validatorImplementation;
    }

    ValidatorDataQualityMetric(Validator validator) {
        this(validator.getClass().getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ValidatorDataQualityMetric that = (ValidatorDataQualityMetric) o;
        return Objects.equals(validator, that.validator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(validator);
    }
}

class EstimatorDataQualityMetric implements DataQualityMetric {

    private final String estimator;

    EstimatorDataQualityMetric(String estimatorImplementation) {
        this.estimator = estimatorImplementation;
    }

    EstimatorDataQualityMetric(Estimator estimator) {
        this(estimator.getClass().getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EstimatorDataQualityMetric that = (EstimatorDataQualityMetric) o;
        return Objects.equals(estimator, that.estimator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(estimator);
    }
}
