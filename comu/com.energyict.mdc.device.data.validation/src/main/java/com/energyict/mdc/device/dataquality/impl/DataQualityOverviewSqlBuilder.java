/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.impl;

import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.dataquality.DataQualityOverviews;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Set;

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

        Set<KpiType> enabledKpiTypes = this.specification.getEnabledKpiTypes();
        this.specification.getAvailableKpiTypes().forEach(kpiType -> kpiType.appendJoinIfIncluded(this.sqlBuilder, enabledKpiTypes));
    }

    private void appendGroupByClause() {
        this.sqlBuilder.append(" group by dev.name, dev.serialnumber, dt.id, dt.name, dc.id, dc.name");
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
        this.sqlBuilder.append(" order by dev.name");
    }

    private void appendKpiTypeWithClauses() {
        this.specification.getAvailableKpiTypes().forEach(perform(KpiType::appendWithClauseTo).with(this.sqlBuilder));
    }

    private void appendAllDataWithClause() {
        this.sqlBuilder.append("allData (devicegroup, device, kpitype, value, timestamp, latest) as (");
        this.sqlBuilder.append("     select dqkpi.enddevicegroup,");
        this.sqlBuilder.append("            to_number(substr(kpim.name, instr(kpim.name, '_') + 1)),");
        this.sqlBuilder.append("            substr(kpim.name, 1, instr(kpim.name, '_') - 1),");
        this.sqlBuilder.append("            sum(slot0),");
        this.sqlBuilder.append("            max(kpivalues.utcstamp),");
        this.sqlBuilder.append("            case when max(kpivalues.recordtime) = max(kpivalues.recordtime) over (partition by max(kpivalues.utcstamp)) then 'Y' else 'N' end");
        this.sqlBuilder.append("     from DQK_DATAQUALITYKPI dqkpi");
        this.sqlBuilder.append("     join DQK_DATAQUALITYKPIMEMBER dqkpim on dqkpim.dataqualitykpi = dqkpi.id");
        this.sqlBuilder.append("     join KPI_KPIMEMBER kpim on kpim.kpi = dqkpim.childkpi");
        this.sqlBuilder.append("     join IDS_VAULT_KPI_1 kpivalues on kpim.timeseries = kpivalues.timeseriesid");
        this.sqlBuilder.append("     where ");
        this.appendPeriod("kpivalues");
        if (!this.specification.getDeviceGroups().isEmpty()) {
            this.sqlBuilder.append("        and dqkpi.enddevicegroup in (");
            this.appendIds(this.specification.getDeviceGroups());
            this.sqlBuilder.append(")");
        }
        this.sqlBuilder.append("            and kpivalues.slot0 > 0");
        this.sqlBuilder.append("      group by dqkpi.enddevicegroup,");
        this.sqlBuilder.append("               to_number(substr(kpim.name, instr(kpim.name, '_') + 1)),");
        this.sqlBuilder.append("               substr(kpim.name, 1, instr(kpim.name, '_') - 1),");
        this.sqlBuilder.append("               kpivalues.recordtime");
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