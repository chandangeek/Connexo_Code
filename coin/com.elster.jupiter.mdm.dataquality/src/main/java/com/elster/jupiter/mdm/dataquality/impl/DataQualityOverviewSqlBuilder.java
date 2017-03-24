/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.dataquality.impl;

import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.streams.Currying.perform;

@LiteralSql
class DataQualityOverviewSqlBuilder {

    private final DataQualityOverviewSpecificationImpl specification;
    private final Clock clock;

    private SqlBuilder sqlBuilder;

    DataQualityOverviewSqlBuilder(DataQualityOverviewSpecificationImpl specification, Clock clock) {
        this.specification = specification;
        this.clock = clock;
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
        this.sqlBuilder.append(" select up.name as usagePointName,");
        this.sqlBuilder.append("        up.servicekind as servicecategoryid,");
        this.sqlBuilder.append("        mc.id as metrologyConfigId,");
        this.sqlBuilder.append("        mc.name as metrologyConfigName,");
        this.sqlBuilder.append("        purpose.id as metrologyPurposeId,");
        this.sqlBuilder.append("        cont.id as metrologyContractId, ");
        this.appendIsEffectiveCaseClause();
        this.specification.getAvailableKpiTypes().forEach(perform(KpiType::appendSelectTo).with(this.sqlBuilder));
    }

    private void appendIsEffectiveCaseClause() {
        Instant now = clock.instant();
        this.sqlBuilder.append(" case when efmc.starttime <=");
        this.sqlBuilder.addLong(now.toEpochMilli());
        this.sqlBuilder.append(" and efmc.endtime >");
        this.sqlBuilder.addLong(now.toEpochMilli());
        this.sqlBuilder.append(" then 'Y' else 'N' end");
    }

    private void appendFromClause() {
        this.sqlBuilder.append(" from mtr_usagepoint up");
    }

    private void appendJoinClauses() {
        this.sqlBuilder.append(" join mtr_usagepointmtrconfig efmc on efmc.usagepoint = up.id and efmc.endtime > efmc.starttime");
        this.sqlBuilder.append(" join mtr_metrologyconfig mc on mc.id = efmc.metrologyconfig");
        if (!this.specification.getMetrologyConfigurations().isEmpty()) {
            this.sqlBuilder.append(" and mc.id in (");
            this.appendIds(this.specification.getMetrologyConfigurations());
            this.sqlBuilder.append(")");
        }
        this.sqlBuilder.append(" join mtr_effective_contract efc on efc.effective_conf = efmc.id and efc.endtime >= efmc.endtime");
        this.sqlBuilder.append(" join mtr_metrology_contract cont on cont.id = efc.metrology_contract");
        this.sqlBuilder.append(" join mtr_metrology_purpose purpose on purpose.id = cont.metrology_purpose");
        if (!this.specification.getMetrologyPurposes().isEmpty()) {
            this.sqlBuilder.append(" and purpose.id in (");
            this.appendIds(this.specification.getMetrologyPurposes());
            this.sqlBuilder.append(")");
        }
        this.sqlBuilder.append(" join allData allKpi on allKpi.usagepoint = up.id and efc.channels_container = allKpi.channelscontainer");

        this.specification.getAvailableKpiTypes().forEach(kpiType -> kpiType.appendJoinTo(this.sqlBuilder));
    }

    private void appendGroupByClause() {
        this.sqlBuilder.append(" group by up.name, up.servicekind, mc.id, mc.name, purpose.id, cont.id, efmc.starttime, efmc.endtime");
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
                .collect(Collectors.toMap(Pair::getFirst, pair -> pair.getLast().get()));
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
        this.sqlBuilder.append(" order by upper(up.name), upper(mc.name), efmc.starttime desc, purpose.id");
    }

    private void appendKpiTypeWithClauses() {
        this.specification.getAvailableKpiTypes().forEach(perform(KpiType::appendWithClauseTo).with(this.sqlBuilder));
    }

    private void appendAllDataWithClause() {
        this.sqlBuilder.append("allData (usagepoint, channelscontainer, kpitype, value, latest) as (");
        this.sqlBuilder.append("    select usagepoint, channelscontainer, kpitype, sum(value), max(timestamp) from (");
        this.sqlBuilder.append("        select to_number(regexp_replace(kpim.name, '([A-Z]+)_(\\d+):(\\d+)', '\\2')) as usagepoint,");
        this.sqlBuilder.append("               to_number(regexp_replace(kpim.name, '([A-Z]+)_(\\d+):(\\d+)', '\\3')) as channelscontainer,");
        this.sqlBuilder.append("                         regexp_replace(kpim.name, '([A-Z]+)_(\\d+):(\\d+)', '\\1') as kpitype,");
        this.sqlBuilder.append("               kpivalues.slot0 as value,");
        this.sqlBuilder.append("               kpivalues.utcstamp as timestamp,");
        this.sqlBuilder.append("               case when kpivalues.recordtime = max(kpivalues.recordtime)");
        this.sqlBuilder.append("                            over (partition by kpivalues.utcstamp,");
        this.sqlBuilder.append("                                               to_number(regexp_replace(kpim.name, '([A-Z]+)_(\\d+):(\\d+)', '\\2')),"); // usage point id
        this.sqlBuilder.append("                                               to_number(regexp_replace(kpim.name, '([A-Z]+)_(\\d+):(\\d+)', '\\3')),"); // channelscontainer id
        this.sqlBuilder.append("                                               regexp_replace(kpim.name, '([A-Z]+)_(\\d+):(\\d+)', '\\1'))");            // kpitype
        this.sqlBuilder.append("               then 'Y' else 'N' end as latest");
        this.sqlBuilder.append("        from DQK_DATAQUALITYKPI dqkpi");
        this.sqlBuilder.append("        join DQK_DATAQUALITYKPIMEMBER dqkpim on dqkpim.dataqualitykpi = dqkpi.id and dqkpi.discriminator = 'UPDQ'");
        this.sqlBuilder.append("        join KPI_KPIMEMBER kpim on kpim.kpi = dqkpim.childkpi");
        this.sqlBuilder.append("        join IDS_VAULT_KPI_1 kpivalues on kpim.timeseries = kpivalues.timeseriesid");
        this.sqlBuilder.append("        where ");
        this.appendPeriod("kpivalues");
        if (!this.specification.getUsagePointGroups().isEmpty()) {
            this.sqlBuilder.append("        and dqkpi.usagepointgroup in (");
            this.appendIds(this.specification.getUsagePointGroups());
            this.sqlBuilder.append(")");
        }
        this.sqlBuilder.append("    ) where latest = 'Y' and value > 0");
        this.sqlBuilder.append("    group by usagepoint, channelscontainer, kpitype");
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