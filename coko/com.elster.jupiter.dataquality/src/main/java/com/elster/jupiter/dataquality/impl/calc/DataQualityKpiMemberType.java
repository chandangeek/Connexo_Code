/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl.calc;

import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.validation.Validator;

import java.util.Objects;

public interface DataQualityKpiMemberType {

    String getName();

    enum PredefinedKpiMemberType implements DataQualityKpiMemberType {
        CHANNEL,
        REGISTER,
        SUSPECT,
        INFORMATIVE,
        ADDED,
        EDITED,
        REMOVED,
        ESTIMATED,
        CONFIRMED,
        PROJECTED;

        public String getName() {
            return name().toUpperCase();
        }
    }

    class NamedDataQualityKpiMemberType implements DataQualityKpiMemberType {

        private String name;

        private NamedDataQualityKpiMemberType(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name.toUpperCase();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            NamedDataQualityKpiMemberType that = (NamedDataQualityKpiMemberType) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return "KpiMemberType: " + this.name;
        }
    }

    class ValidatorKpiMemberType extends NamedDataQualityKpiMemberType {
        public ValidatorKpiMemberType(Validator validator) {
            super(validator.getClass().getSimpleName());
        }
    }

    class EstimatorKpiMemberType extends NamedDataQualityKpiMemberType {
        public EstimatorKpiMemberType(Estimator estimator) {
            super(estimator.getClass().getSimpleName());
        }
    }
}
