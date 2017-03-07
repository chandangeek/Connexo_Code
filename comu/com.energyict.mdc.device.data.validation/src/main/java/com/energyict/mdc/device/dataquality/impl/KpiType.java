/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.impl;

import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.validation.Validator;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class KpiType {

    static final KpiType SUSPECT = new KpiType("totalSuspectValues", "totalSuspectsKpi", "SUSPECT") {

        @Override
        void appendSelectTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(", max(");
            sqlBuilder.append(this.kpiTableName());
            sqlBuilder.append(".timestamp)");
            super.appendSelectTo(sqlBuilder);
        }
    };
    static final KpiType CHANNEL = new KpiType("channelSuspectValues", "channelSuspectsKpi", "CHANNEL");
    static final KpiType REGISTER = new KpiType("registerSuspectValues", "registerSuspectsKpi", "REGISTER");
    static final KpiType ADDED = new KpiType("addedValues", "addedKpi", "ADDED");
    static final KpiType EDITED = new KpiType("editedValues", "editedKpi", "EDITED");
    static final KpiType REMOVED = new KpiType("removedValues", "removedKpi", "REMOVED");
    static final KpiType ESTIMATED = new KpiType("estimatedValues", "estimatedKpi", "ESTIMATED");
    static final KpiType CONFIRMED = new KpiType("confirmedValues", "confirmedKpi", "CONFIRMED");
    static final KpiType INFORMATIVE = new KpiType("informativeValues", "informativesKpi", "INFORMATIVE");

    static final List<KpiType> predefinedKpiTypes = Arrays.asList(
            SUSPECT, CHANNEL, REGISTER, ADDED, EDITED, REMOVED, ESTIMATED, CONFIRMED, INFORMATIVE
    );

    static class ValidatorKpiType extends KpiType {
        ValidatorKpiType(Validator validator) {
            super(generateWithClauseAliasName(validator), name(validator) + "Kpi", name(validator).toUpperCase());
        }

        private static String name(Validator validator) {
            return validator.getClass().getSimpleName();
        }

        private static String generateWithClauseAliasName(Validator validator) {
            String className = validator.getClass().getName();
            return "validator_" + Math.abs(className.hashCode()) + "_Values";
        }
    }

    static class EstimatorKpiType extends KpiType {
        EstimatorKpiType(Estimator estimator) {
            super(generateWithClauseAlias(estimator), name(estimator) + "Kpi", name(estimator).toUpperCase());
        }

        private static String name(Estimator estimator) {
            return estimator.getClass().getSimpleName();
        }

        private static String generateWithClauseAlias(Estimator estimator) {
            String className = estimator.getClass().getName();
            return "estimator_" + Math.abs(className.hashCode()) + "_Values";
        }
    }

    private final String withClauseAliasName;
    private final String kpiTableName;
    private final String kpiType;

    KpiType(String withClauseAliasName, String kpiTableName, String kpiType) {
        if (withClauseAliasName.length() > 30) {
            throw new IllegalArgumentException("With clause name must be less then 30 characters");
        }
        this.withClauseAliasName = withClauseAliasName;
        this.kpiTableName = kpiTableName;
        this.kpiType = kpiType;
    }

    String withClauseAliasName() {
        return this.withClauseAliasName;
    }

    String kpiTableName() {
        return this.kpiTableName;
    }

    String getKpiType() {
        return this.kpiType;
    }

    void appendWithClauseTo(SqlBuilder sqlBuilder) {
        sqlBuilder.append(", ");
        sqlBuilder.append(this.withClauseAliasName());
        sqlBuilder.append(" (devicegroup, device, value, timestamp) as (select devicegroup, device, value, timestamp from allData where kpitype ='");
        sqlBuilder.append(this.getKpiType());
        sqlBuilder.append("' and latest = 'Y')");
    }

    void appendSelectTo(SqlBuilder sqlBuilder) {
        sqlBuilder.append(", ");
        sqlBuilder.append("nvl(max(");
        sqlBuilder.append(this.kpiTableName());
        sqlBuilder.append(".value)");
        sqlBuilder.append(", 0)");
        sqlBuilder.append(" as ");
        sqlBuilder.append(this.withClauseAliasName());
    }

    void appendJoinIfIncluded(SqlBuilder sqlBuilder, Set<KpiType> options) {
        if (!options.contains(this)) {
            sqlBuilder.append(" left");
        }
        this.appendJoinTo(sqlBuilder);
    }

    void appendJoinTo(SqlBuilder sqlBuilder) {
        sqlBuilder.append(" join ");
        sqlBuilder.append(this.withClauseAliasName());
        sqlBuilder.append(" ");
        sqlBuilder.append(this.kpiTableName());
        sqlBuilder.append(" on ");
        sqlBuilder.append(this.kpiTableName());
        sqlBuilder.append(".device = dev.meterid and ");
        sqlBuilder.append(this.kpiTableName());
        sqlBuilder.append(".devicegroup = allKpi.devicegroup");
    }

    void appendHavingTo(SqlBuilder sqlBuilder, MetricValueRange range) {
        range.appendHavingTo(sqlBuilder, "max(nvl(" + this.kpiTableName() + ".value, 0))");
    }

    static void appendKpisSumHavingTo(SqlBuilder sqlBuilder, MetricValueRange range, KpiType... kpiTypes) {
        String sumOfKpiTypes = Stream.of(kpiTypes).map(kpiType -> "max(nvl(" + kpiType.kpiTableName() + ".value, 0))").collect(Collectors.joining(" + "));
        range.appendHavingTo(sqlBuilder, "(" + sumOfKpiTypes + ")");
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KpiType)) {
            return false;
        }
        KpiType kpiType1 = (KpiType) o;
        return Objects.equals(withClauseAliasName, kpiType1.withClauseAliasName) &&
                Objects.equals(kpiTableName, kpiType1.kpiTableName) &&
                Objects.equals(kpiType, kpiType1.kpiType);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(withClauseAliasName, kpiTableName, kpiType);
    }
}
