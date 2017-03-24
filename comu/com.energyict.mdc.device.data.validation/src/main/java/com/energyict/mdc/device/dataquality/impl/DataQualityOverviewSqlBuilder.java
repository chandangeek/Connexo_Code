/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.impl;

import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.device.dataquality.DataQualityOverviews;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.Currying.perform;

/**
 * Builds the custom sql that supports the {@link DataQualityOverviews}.
 * The sql uses "WITH" clauses which will make it impossible to unit test this against H2 :-(
 * A with clause is generated for every with clause
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-08 (13:00)
 */
@LiteralSql
class DataQualityOverviewSqlBuilder {

    private final DataQualityOverviewSpecificationImpl specification;

    private SqlBuilder sqlBuilder;

    DataQualityOverviewSqlBuilder(DataQualityOverviewSpecificationImpl specification) {
        this.specification = specification;
    }

    PreparedStatement prepare(Connection connection) throws SQLException {
        this.buildSql();
        return this.sqlBuilder.prepare(connection);
    }

    private void buildSql() {
        this.sqlBuilder = new SqlBuilder("with ");
        this.appendAllDataWithClause();
        this.appendKpiTypeWithClauses();
        this.appendActualQuery();
        this.appendGroupByClause();
        this.appendHavingClause();
        this.appendOrderByClause();
        this.sqlBuilder = this.sqlBuilder.asPageBuilder(this.specification.getFrom(), this.specification.getTo());
    }

    private void appendActualQuery() {
        this.appendSelectClause();
        this.appendFromClause();
        this.appendJoinClauses();
    }

    private void appendSelectClause() {
        this.sqlBuilder.append(" select dev.name as deviceName,");
        this.sqlBuilder.append("        dev.serialnumber,");
        this.sqlBuilder.append("        dt.id as deviceTypeId,");
        this.sqlBuilder.append("        dt.name as deviceTypeName,");
        this.sqlBuilder.append("        dc.id as deviceConfigId,");
        this.sqlBuilder.append("        dc.name as deviceConfigName");
        this.specification.getAvailableKpiTypes().forEach(perform(KpiType::appendSelectTo).with(this.sqlBuilder));
    }

    private void appendFromClause() {
        this.sqlBuilder.append(" from ddc_device dev");
    }

    private void appendJoinClauses() {
        this.sqlBuilder.append(" join dtc_devicetype dt on dev.devicetype = dt.id");
        if (!this.specification.getDeviceTypes().isEmpty()) {
            this.sqlBuilder.append(" and dt.id in (");
            this.appendIds(this.specification.getDeviceTypes());
            this.sqlBuilder.append(")");
        }
        this.sqlBuilder.append(" join dtc_deviceconfig dc on dev.deviceconfigid = dc.id");
        this.sqlBuilder.append(" join allData allKpi on allKpi.device = dev.meterid");

        this.specification.getAvailableKpiTypes().forEach(kpiType -> kpiType.appendJoinTo(this.sqlBuilder));
    }

    private void appendGroupByClause() {
        this.sqlBuilder.append(" group by dev.name, dev.serialnumber, dt.id, dt.name, dc.id, dc.name");
    }

    private void appendHavingClause() {
        // collect conditions from spec
        Map<KpiType, MetricValueRange> conditionsByAmount = Stream.of(
                Pair.of(KpiType.SUSPECT, this.specification.getAmountOfSuspects()),
                Pair.of(KpiType.INFORMATIVE, this.specification.getAmountOfInformatives()),
                Pair.of(KpiType.ESTIMATED, this.specification.getAmountOfEstimates()),
                Pair.of(KpiType.TOTAL_EDITED, this.specification.getAmountOfEdited()),
                Pair.of(KpiType.CONFIRMED, this.specification.getAmountOfConfirmed()))
                .filter(pair -> pair.getLast().isPresent())
                .map(pair -> Pair.of(pair.getFirst(), pair.getLast().get()))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getLast));
        Map<KpiType, MetricValueRange> conditionsByReadingQualities = this.specification.getReadingQualityTypes().stream()
                .collect(Collectors.toMap(Function.identity(), rqt -> MetricValueRange.AT_LEAST_ONE));
        Map<KpiType, MetricValueRange> conditionsByValidators = this.specification.getValidators().stream()
                .collect(Collectors.toMap(Function.identity(), rqt -> MetricValueRange.AT_LEAST_ONE));
        Map<KpiType, MetricValueRange> conditionsByEstimators = this.specification.getEstimators().stream()
                .collect(Collectors.toMap(Function.identity(), rqt -> MetricValueRange.AT_LEAST_ONE));

        // build sql fragments
        SqlBuilder havingClauseConditions = new SqlBuilder();
        buildSqlConditionWithOperator(conditionsByAmount, SqlOperator.AND)
                .ifPresent(fragment -> this.appendFragment(havingClauseConditions, SqlOperator.AND, fragment));
        buildSqlConditionWithOperator(conditionsByReadingQualities, SqlOperator.OR)
                .ifPresent(fragment -> this.appendFragment(havingClauseConditions, SqlOperator.AND, fragment));
        buildSqlConditionWithOperator(conditionsByValidators, SqlOperator.OR)
                .ifPresent(fragment -> this.appendFragment(havingClauseConditions, SqlOperator.AND, fragment));
        buildSqlConditionWithOperator(conditionsByEstimators, SqlOperator.OR)
                .ifPresent(fragment -> this.appendFragment(havingClauseConditions, SqlOperator.AND, fragment));

        // build having clause
        if (havingClauseConditions.getBuffer().length() != 0) {
            this.sqlBuilder.append(" having ");
            this.sqlBuilder.add(havingClauseConditions);
        }
    }

    private void appendFragment(SqlBuilder sqlBuilder, SqlOperator operator, SqlFragment sqlFragment) {
        if (sqlBuilder.getBuffer().length() != 0) {
            sqlBuilder.append(operator.getOperator());
        }
        sqlBuilder.add(sqlFragment);
    }

    enum SqlOperator {
        AND("and"),
        OR("or");

        private String operator;

        SqlOperator(String operator) {
            this.operator = operator;
        }

        String getOperator() {
            return " " + this.operator + " ";
        }
    }

    private Optional<SqlFragment> buildSqlConditionWithOperator(Map<KpiType, MetricValueRange> conditions, SqlOperator operator) {
        if (!conditions.isEmpty()) {
            SqlBuilder sqlBuilder = new SqlBuilder();
            sqlBuilder.openBracket();
            Iterator<Map.Entry<KpiType, MetricValueRange>> conditionIterator = conditions.entrySet().iterator();
            while (conditionIterator.hasNext()) {
                Map.Entry<KpiType, MetricValueRange> condition = conditionIterator.next();
                condition.getKey().appendHavingTo(sqlBuilder, condition.getValue());
                if (conditionIterator.hasNext()) {
                    sqlBuilder.append(operator.getOperator());
                }
            }
            sqlBuilder.closeBracket();
            return Optional.of(sqlBuilder);
        }
        return Optional.empty();
    }

    private void appendOrderByClause() {
        this.sqlBuilder.append(" order by upper(dev.name)");
    }

    private void appendKpiTypeWithClauses() {
        this.specification.getAvailableKpiTypes().forEach(perform(KpiType::appendWithClauseTo).with(this.sqlBuilder));
    }

    private void appendAllDataWithClause() {
        this.sqlBuilder.append("allData (device, kpitype, value, latest) as (");
        this.sqlBuilder.append("    select device, kpitype, sum(value), max(timestamp) from (");
        this.sqlBuilder.append("        select to_number(substr(kpim.name, instr(kpim.name, '_') + 1)) as device,");
        this.sqlBuilder.append("               substr(kpim.name, 1, instr(kpim.name, '_') - 1) as kpitype,");
        this.sqlBuilder.append("               kpivalues.slot0 as value,");
        this.sqlBuilder.append("               kpivalues.utcstamp as timestamp,");
        this.sqlBuilder.append("               case when kpivalues.recordtime = max(kpivalues.recordtime)");
        this.sqlBuilder.append("                            over (partition by kpivalues.utcstamp, to_number(substr(kpim.name, instr(kpim.name, '_') + 1)), substr(kpim.name, 1, instr(kpim.name, '_') - 1))");
        this.sqlBuilder.append("               then 'Y' else 'N' end as latest");
        this.sqlBuilder.append("        from DQK_DATAQUALITYKPI dqkpi");
        this.sqlBuilder.append("        join DQK_DATAQUALITYKPIMEMBER dqkpim on dqkpim.dataqualitykpi = dqkpi.id and dqkpi.discriminator = 'EDDQ'");
        this.sqlBuilder.append("        join KPI_KPIMEMBER kpim on kpim.kpi = dqkpim.childkpi");
        this.sqlBuilder.append("        join IDS_VAULT_KPI_1 kpivalues on kpim.timeseries = kpivalues.timeseriesid");
        this.sqlBuilder.append("        where ");
        this.appendPeriod("kpivalues");
        if (!this.specification.getDeviceGroups().isEmpty()) {
            this.sqlBuilder.append("        and dqkpi.enddevicegroup in (");
            this.appendIds(this.specification.getDeviceGroups());
            this.sqlBuilder.append(")");
        }
        this.sqlBuilder.append("    ) where latest = 'Y' and value > 0");
        this.sqlBuilder.append("    group by device, kpitype");
        this.sqlBuilder.append(")");
    }

    private void appendIds(Set<? extends HasId> hasIds) {
        IdAppender idAppender = new IdAppender(this.sqlBuilder);
        hasIds.stream().map(HasId::getId).forEach(idAppender::append);
    }

    private void appendPeriod(String aliasName) {
        Range<Instant> period = this.specification.getPeriod();
        this.sqlBuilder.append(aliasName);
        this.sqlBuilder.append(".utcstamp >");
        if (period.hasLowerBound() && period.lowerBoundType() == BoundType.CLOSED) {
            this.sqlBuilder.append("=");
        }
        this.sqlBuilder.addLong(period.hasLowerBound() ? period.lowerEndpoint().toEpochMilli() : Long.MIN_VALUE);
        this.sqlBuilder.append("and ");
        this.sqlBuilder.append(aliasName);
        this.sqlBuilder.append(".utcstamp <");
        if (period.hasUpperBound() && period.upperBoundType() == BoundType.CLOSED) {
            this.sqlBuilder.append("=");
        }
        this.sqlBuilder.addLong(period.hasUpperBound() ? period.upperEndpoint().toEpochMilli() : Long.MAX_VALUE);
    }

    private static class IdAppender {

        private boolean notFirst = false;
        private final SqlBuilder sqlBuilder;

        private IdAppender(SqlBuilder sqlBuilder) {
            this.sqlBuilder = sqlBuilder;
        }

        private void append(long id) {
            if (this.notFirst) {
                this.sqlBuilder.append(",");
            }
            this.sqlBuilder.addLong(id);
            this.notFirst = true;
        }
    }
}