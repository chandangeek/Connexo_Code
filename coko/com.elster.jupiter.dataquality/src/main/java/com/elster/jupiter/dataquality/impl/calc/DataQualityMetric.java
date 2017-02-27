/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl.calc;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.Validator;

import java.util.Objects;

interface DataQualityMetric {

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

    class NamedDataQualityMetric implements DataQualityMetric {

        private String name;

        private NamedDataQualityMetric(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            NamedDataQualityMetric that = (NamedDataQualityMetric) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    class ValidatorDataQualityMetric extends NamedDataQualityMetric {

        ValidatorDataQualityMetric(ValidationRule validationRule) {
            super(validationRule.getImplementation());
        }

        ValidatorDataQualityMetric(Validator validator) {
            super(validator.getClass().getName());
        }
    }

    class EstimatorDataQualityMetric extends NamedDataQualityMetric {

        EstimatorDataQualityMetric(EstimationRule estimationRule) {
            super(estimationRule.getImplementation());
        }

        EstimatorDataQualityMetric(Estimator estimator) {
            super(estimator.getClass().getName());
        }
    }
}