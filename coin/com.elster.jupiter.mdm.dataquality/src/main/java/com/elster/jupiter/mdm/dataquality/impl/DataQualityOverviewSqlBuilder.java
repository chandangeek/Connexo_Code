/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.dataquality.impl;

import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Set;

import static com.elster.jupiter.util.streams.Currying.perform;

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
        this.sqlBuilder.append(" select up.name as usagePointName,");
        this.sqlBuilder.append("        up.servicekind as servicecategoryid,");
        this.sqlBuilder.append("        mc.id as metrologyConfigId,");
        this.sqlBuilder.append("        mc.name as metrologyConfigName,");
        this.sqlBuilder.append("        purpose.id as metrologyPurposeId");
        this.specification.getAvailableKpiTypes().forEach(perform(KpiType::appendSelectTo).with(this.sqlBuilder));
    }

    private void appendFromClause() {
        this.sqlBuilder.append(" from mtr_usagepoint up");
    }

    private void appendJoinClauses() {
        this.sqlBuilder.append(" join mtr_usagepointmtrconfig efmc on efmc.usagepoint = up.id");
        this.sqlBuilder.append(" join mtr_metrologyconfig mc on mc.id = efmc.metrologyconfig");
        if (!this.specification.getMetrologyConfigurations().isEmpty()) {
            this.sqlBuilder.append(" and mc.id in (");
            this.appendIds(this.specification.getMetrologyConfigurations());
            this.sqlBuilder.append(")");
        }
        this.sqlBuilder.append(" join mtr_effective_contract efc on efc.effective_conf = efmc.id");
        this.sqlBuilder.append(" join mtr_metrology_contract cont on cont.id = efc.metrology_contract");
        this.sqlBuilder.append(" join mtr_metrology_purpose purpose on purpose.id = cont.metrology_purpose");
        if (!this.specification.getMetrologyPurposes().isEmpty()) {
            this.sqlBuilder.append(" and purpose.id in (");
            this.appendIds(this.specification.getMetrologyPurposes());
            this.sqlBuilder.append(")");
        }
        this.sqlBuilder.append(" join allData allKpi on allKpi.usagepoint = up.id and efc.channels_container = allKpi.channelscontainer");

        Set<KpiType> enabledKpiTypes = this.specification.getEnabledKpiTypes();
        this.specification.getAvailableKpiTypes().forEach(kpiType -> kpiType.appendJoinIfIncluded(this.sqlBuilder, enabledKpiTypes));
    }

    private void appendGroupByClause() {
        this.sqlBuilder.append(" group by up.name, up.servicekind, mc.id, mc.name, purpose.id, efmc.starttime");
    }

    private void appendHavingClause() {
        this.sqlBuilder.append(" having ");
        KpiType.SUSPECT.appendHavingTo(this.sqlBuilder, this.specification.getAmountOfSuspects());
        this.sqlBuilder.append(" and ");
        KpiType.CONFIRMED.appendHavingTo(this.sqlBuilder, this.specification.getAmountOfConfirmed());
        this.sqlBuilder.append(" and ");
        KpiType.ESTIMATED.appendHavingTo(this.sqlBuilder, this.specification.getAmountOfEstimates());
        this.sqlBuilder.append(" and ");
        KpiType.INFORMATIVE.appendHavingTo(this.sqlBuilder, this.specification.getAmountOfInformatives());
        this.sqlBuilder.append(" and ");
        KpiType.appendKpisSumHavingTo(this.sqlBuilder, this.specification.getAmountOfEdited(), KpiType.ADDED, KpiType.EDITED, KpiType.REMOVED);
    }

    private void appendOrderByClause() {
        this.sqlBuilder.append(" order by upper(up.name), upper(mc.name), efmc.starttime");
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
        this.sqlBuilder.append("               case when kpivalues.recordtime = max(kpivalues.recordtime) over (partition by kpivalues.utcstamp) then 'Y' else 'N' end as latest");
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
        this.sqlBuilder.append("            and kpivalues.slot0 > 0");
        this.sqlBuilder.append("    ) where latest = 'Y'");
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