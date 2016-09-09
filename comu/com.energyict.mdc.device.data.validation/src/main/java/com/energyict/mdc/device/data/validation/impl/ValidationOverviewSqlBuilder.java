/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Builds the custom sql that supports the {@link com.energyict.mdc.device.data.validation.ValidationOverviews}.
 * The sql uses "WITH" clauses which will make it impossible to unit test this against H2 :-(
 * A with clause is generated for every with clause
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-08 (13:00)
 */
@LiteralSql
class ValidationOverviewSqlBuilder {
    private SqlBuilder sqlBuilder;
    private final List<EndDeviceGroup> deviceGroups;
    private final Range<Instant> range;
    private final DeviceDataValidationServiceImpl.SuspectsRange suspectsRange;
    private final Set<KpiType> kpiTypes;
    private final int from;
    private final int to;

    enum KpiType {
        TOTAL("totalSuspectValues", "totalSuspectsKpi", "SUSPECT") {
            @Override
            protected void appendJoinTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" join ");
                sqlBuilder.append(this.withClauseAliasName());
                sqlBuilder.append(" ");
                sqlBuilder.append(this.kpiTableName());
                sqlBuilder.append(" on ");
                sqlBuilder.append(this.kpiTableName());
                sqlBuilder.append(".device = dev.meterid");
            }

            @Override
            protected void appendSelectTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append(", ");
                sqlBuilder.append(this.kpiTableName());
                sqlBuilder.append(".timestamp");
                super.appendSelectTo(sqlBuilder);
            }
        },
        ALL_DATA_VALIDATED("allDataValidatedValues", "allDataValidatedKpi", "ALLDATAVALIDATED"),
        REGISTER("registerSuspectValues", "registerSuspectsKpi", "REGISTER"),
        CHANNEL("channelSuspectValues", "channelSuspectsKpi", "CHANNEL"),
        THRESHOLD("thresholdValues", "thresholdKpi", "THRESHOLDVALIDATOR"),
        MISSING_VALUES("missingValues", "missingsKpi", "MISSINGVALUESVALIDATOR"),
        READING_QUALITIES("readingQualitiesValues", "readingQualitiesKpi", "READINGQUALITIESVALIDATOR"),
        REGISTER_INCREASE("registerIncreaseValues", "registerIncreaseKpi", "REGISTERINCREASEVALIDATOR");

        private final String withClauseAliasName;
        private final String kpiTableName;
        private final String kpiType;

        KpiType(String withClauseAliasName, String kpiTableName, String kpiType) {
            this.withClauseAliasName = withClauseAliasName;
            this.kpiTableName = kpiTableName;
            this.kpiType = kpiType;
        }

        protected String withClauseAliasName() {
            return withClauseAliasName;
        }

        protected String kpiTableName() {
            return kpiTableName;
        }

        protected String kpiType() {
            return kpiType;
        }

        private void appendWithIfIncluded(Set<KpiType> options, SqlBuilder sqlBuilder) {
            if (options.contains(this)) {
                this.appendWithTo(sqlBuilder);
            }
        }

        private void appendWithTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(", ");
            sqlBuilder.append(this.withClauseAliasName);
            sqlBuilder.append(" (devicegroup, device, value, timestamp) as (select devicegroup, device, value, timestamp from allData where kpitype ='");
            sqlBuilder.append(kpiType);
            sqlBuilder.append("')");
        }

        private void appendSelectIfIncluded (Set<KpiType> options, SqlBuilder sqlBuilder) {
            if (options.contains(this)) {
                this.appendSelectTo(sqlBuilder);
            } else {
                sqlBuilder.append(", -1 as ");
                sqlBuilder.append(this.withClauseAliasName);
            }
        }

        protected void appendSelectTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(", ");
            sqlBuilder.append(this.kpiTableName);
            sqlBuilder.append(".value as ");
            sqlBuilder.append(this.withClauseAliasName);
        }

        private void appendJoinIfIncluded(Set<KpiType> options, SqlBuilder sqlBuilder, DeviceDataValidationServiceImpl.SuspectsRange suspectsRange) {
            if (options.contains(this)) {
                this.appendJoinTo(sqlBuilder, suspectsRange);
            }
        }

        protected void appendJoinTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" join ");
            sqlBuilder.append(this.withClauseAliasName);
            sqlBuilder.append(" ");
            sqlBuilder.append(this.kpiTableName);
            sqlBuilder.append(" on ");
            sqlBuilder.append(this.kpiTableName);
            sqlBuilder.append(".device = dev.meterid and ");
            sqlBuilder.append(this.kpiTableName);
            sqlBuilder.append(".devicegroup = ");
            sqlBuilder.append(TOTAL.kpiTableName);
            sqlBuilder.append(".devicegroup");
        }

        private void appendJoinTo(SqlBuilder sqlBuilder, DeviceDataValidationServiceImpl.SuspectsRange suspectsRange) {
            this.appendJoinTo(sqlBuilder);
            suspectsRange.appendJoinTo(sqlBuilder, this.kpiTableName);
        }
    }

    private enum AllDataType {
        VALIDATED {
            @Override
            public void appendSelectValueTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append("'ALLDATAVALIDATED',");
            }

            @Override
            String operator() {
                return "=";
            }

            @Override
            public void appendGroupByTo(SqlBuilder sqlBuilder) {
                // Nothing to append
            }
        },

        OTHER {
            @Override
            public void appendSelectValueTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append("substr(kpim.name, 1, instr(kpim.name, '_') - 1)");
                sqlBuilder.append(",");
            }

            @Override
            String operator() {
                return "<>";
            }

            @Override
            public void appendGroupByTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append(",");
                this.appendExpressionTo(sqlBuilder);
            }

            private void appendExpressionTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append("substr(kpim.name, 1, instr(kpim.name, '_') - 1)");
            }
        };

        abstract String operator();

        public abstract void appendSelectValueTo(SqlBuilder sqlBuilder);

        public abstract void appendGroupByTo(SqlBuilder sqlBuilder);
    }

    ValidationOverviewSqlBuilder(List<EndDeviceGroup> deviceGroups, Range<Instant> range, Set<KpiType> kpiTypes, DeviceDataValidationServiceImpl.SuspectsRange suspectsRange, int from, int to) {
        this.deviceGroups = deviceGroups;
        this.range = range;
        this.kpiTypes = kpiTypes;
        this.suspectsRange = suspectsRange;
        this.from = from;
        this.to = to;
    }

    PreparedStatement prepare(Connection connection) throws SQLException {
        this.buildSql();
        return this.sqlBuilder.prepare(connection);
    }

    private void buildSql() {
        this.sqlBuilder = new SqlBuilder("WITH ");
        this.appendAllDataWithClause();
        this.sqlBuilder.append(", ");
        this.appendAllValidatedWithClause();
        this.appendKpiTypeWithClauses();
        if (!this.kpiTypes.isEmpty()) {
            this.appendOptionalKpiTypeWithClauses();
        }
        this.appendActualQuery();
        this.appendOrderByClause();
        this.sqlBuilder = this.sqlBuilder.asPageBuilder(this.from, this.to);
    }

    private void appendActualQuery() {
        this.appendSelectClause();
        this.appendFromClause();
        this.appendJoinClauses();
    }

    private void appendSelectClause() {
        this.sqlBuilder.append(" select dev.mRID, totalSuspectsKpi.devicegroup, dev.serialnumber, dt.name as devtypename, dc.name as devconfigname");
        Stream
            .of(KpiType.TOTAL, KpiType.CHANNEL, KpiType.REGISTER, KpiType.ALL_DATA_VALIDATED)
            .forEach(kpiType -> kpiType.appendSelectTo(this.sqlBuilder));
        Stream
            .of(KpiType.THRESHOLD, KpiType.MISSING_VALUES, KpiType.READING_QUALITIES, KpiType.REGISTER_INCREASE)
            .forEach(kpiType -> kpiType.appendSelectIfIncluded(this.kpiTypes, this.sqlBuilder));
    }

    private void appendFromClause() {
        this.sqlBuilder.append(" from ddc_device dev");
    }

    private void appendJoinClauses() {
        this.sqlBuilder.append(" join dtc_devicetype dt on dev.devicetype = dt.id");
        this.sqlBuilder.append(" join dtc_deviceconfig dc on dev.deviceconfigid = dc.id");
        KpiType.TOTAL.appendJoinTo(this.sqlBuilder);
        this.sqlBuilder.append(" join registerSuspectValues registerSuspectsKpi ");
        this.sqlBuilder.append("   on registerSuspectsKpi.device = dev.meterid");
        this.sqlBuilder.append("  and registerSuspectsKpi.devicegroup = ");
        this.sqlBuilder.append(KpiType.TOTAL.kpiTableName);
        this.sqlBuilder.append(".devicegroup");
        this.sqlBuilder.append(" join channelSuspectValues channelSuspectsKpi");
        this.sqlBuilder.append("   on channelSuspectsKpi.device = dev.meterid");
        this.sqlBuilder.append("  and channelSuspectsKpi.devicegroup = ");
        this.sqlBuilder.append(KpiType.TOTAL.kpiTableName);
        this.sqlBuilder.append(".devicegroup");
        this.sqlBuilder.append(" join allDataValidatedValues allDataValidatedKpi ");
        this.sqlBuilder.append("   on allDataValidatedKpi.device = dev.meterid");
        this.sqlBuilder.append("  and allDataValidatedKpi.devicegroup = ");
        this.sqlBuilder.append(KpiType.TOTAL.kpiTableName);
        this.sqlBuilder.append(".devicegroup");
        this.appendOptionalJoinClauses();
    }

    private void appendOptionalJoinClauses() {
        Stream
            .of(KpiType.THRESHOLD, KpiType.MISSING_VALUES, KpiType.READING_QUALITIES, KpiType.REGISTER_INCREASE)
            .forEach(kpiType -> kpiType.appendJoinIfIncluded(this.kpiTypes, this.sqlBuilder, this.suspectsRange));
    }

    private void appendOrderByClause() {
        this.sqlBuilder.append(" order by dev.mRID");
    }

    private void appendKpiTypeWithClauses() {
        KpiType.TOTAL.appendWithTo(this.sqlBuilder);
        KpiType.REGISTER.appendWithTo(this.sqlBuilder);
        KpiType.CHANNEL.appendWithTo(this.sqlBuilder);
    }

    private void appendAllDataWithClause() {
        this.appendAllWithClause("allData", AllDataType.OTHER);
    }

    private void appendAllValidatedWithClause() {
        this.appendAllWithClause("allDataValidatedValues", AllDataType.VALIDATED);
    }

    private void appendAllWithClause(String withClauseAliasName, AllDataType allDataType) {
        this.sqlBuilder.append(withClauseAliasName);
        this.sqlBuilder.append(" (devicegroup, device, kpitype, value, timestamp) as (");
        this.sqlBuilder.append("     select dvkpi.enddevicegroup,");
        this.sqlBuilder.append("            to_number(substr(kpim.name, instr(kpim.name, '_') + 1)),");
        allDataType.appendSelectValueTo(this.sqlBuilder);
        this.sqlBuilder.append("            max(slot0),");
        this.sqlBuilder.append("            max(UTCSTAMP)");
        this.sqlBuilder.append("       from VAL_DATA_VALIDATION_KPI dvkpi,");
        this.sqlBuilder.append("            VAL_DATAVALIDATIONKPICHILDREN dvkpim,");
        this.sqlBuilder.append("            kpi_kpimember kpim,");
        this.sqlBuilder.append("            ids_vault_kpi_1 kpivalues");
        this.sqlBuilder.append("      where dvkpim.datavalidationkpi = dvkpi.id");
        this.sqlBuilder.append("        and kpim.kpi = dvkpim.childkpi");
        this.sqlBuilder.append("        and kpim.timeseries = kpivalues.timeseriesid");
        this.appendRange("kpivalues");
        if (!this.deviceGroups.isEmpty()) {
            this.sqlBuilder.append("        and dvkpi.enddevicegroup in (");
            this.appendDeviceGroupIds();
            this.sqlBuilder.append(")");
        }
        this.sqlBuilder.append("        and substr(kpim.name, 1, instr(kpim.name, '_') - 1) ");
        this.sqlBuilder.append(allDataType.operator());
        this.sqlBuilder.append("'ALLDATAVALIDATED'");
        this.sqlBuilder.append("      group by dvkpi.enddevicegroup, to_number(substr(kpim.name, instr(kpim.name, '_') + 1))");
        allDataType.appendGroupByTo(this.sqlBuilder);
        this.sqlBuilder.append(")");
    }

    private void appendDeviceGroupIds() {
        IdAppender idAppender = new IdAppender(this.sqlBuilder);
        this.deviceGroups
                .stream()
                .map(EndDeviceGroup::getId)
                .forEach(idAppender::append);
    }

    private void appendRange(String aliasName) {
        this.sqlBuilder.append(" and ");
        this.sqlBuilder.append(aliasName);
        this.sqlBuilder.append(".UTCSTAMP >");
        if (this.range.hasLowerBound() && this.range.lowerBoundType() == BoundType.CLOSED) {
            this.sqlBuilder.append("=");
        }
        this.sqlBuilder.addLong(this.range.hasLowerBound() ? this.range.lowerEndpoint().toEpochMilli() : Long.MIN_VALUE);
        this.sqlBuilder.append("AND ");
        this.sqlBuilder.append(aliasName);
        this.sqlBuilder.append(".UTCSTAMP <");
        if (this.range.hasUpperBound() && this.range.upperBoundType() == BoundType.CLOSED) {
            this.sqlBuilder.append("=");
        }
        this.sqlBuilder.addLong(this.range.hasUpperBound() ? this.range.upperEndpoint().toEpochMilli() : Long.MAX_VALUE);
    }

    private void appendOptionalKpiTypeWithClauses() {
        KpiType.THRESHOLD.appendWithIfIncluded(this.kpiTypes, this.sqlBuilder);
        KpiType.MISSING_VALUES.appendWithIfIncluded(this.kpiTypes, this.sqlBuilder);
        KpiType.READING_QUALITIES.appendWithIfIncluded(this.kpiTypes, this.sqlBuilder);
        KpiType.REGISTER_INCREASE.appendWithIfIncluded(this.kpiTypes, this.sqlBuilder);
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