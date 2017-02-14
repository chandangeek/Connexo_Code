/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.data.validation.DataQualityOverviews;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
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
class ValidationOverviewSqlBuilder {
    private SqlBuilder sqlBuilder;
    private final List<EndDeviceGroup> deviceGroups;
    private final Range<Instant> range;
    private final DeviceDataQualityServiceImpl.SuspectsRange suspectsRange;
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
                sqlBuilder.append(", max(");
                sqlBuilder.append(this.kpiTableName());
                sqlBuilder.append(".timestamp)");
                super.appendSelectTo(sqlBuilder);
            }

            @Override
            public void appendHavingTo(SqlBuilder sqlBuilder, DeviceDataQualityServiceImpl.SuspectsRange suspectsRange) {
                suspectsRange.appendHavingTo(sqlBuilder, "sum(totalSuspectsKpi.value)");
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
        private final String aggregationFunction;

        KpiType(String withClauseAliasName, String kpiTableName, String kpiType) {
            this(withClauseAliasName, kpiTableName, kpiType, "max");
        }

        KpiType(String withClauseAliasName, String kpiTableName, String kpiType, String aggregationFunction) {
            this.withClauseAliasName = withClauseAliasName;
            this.kpiTableName = kpiTableName;
            this.kpiType = kpiType;
            this.aggregationFunction = aggregationFunction;
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

        private void appendWithTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(", ");
            sqlBuilder.append(this.withClauseAliasName);
            sqlBuilder.append(" (devicegroup, device, value, timestamp) as (select devicegroup, device, value, timestamp from allData where kpitype ='");
            sqlBuilder.append(kpiType);
            sqlBuilder.append("')");
        }

        protected void appendSelectTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(", ");
            sqlBuilder.append("NVL(");
            sqlBuilder.append(this.aggregationFunction);
            sqlBuilder.append("(");
            sqlBuilder.append(this.kpiTableName);
            sqlBuilder.append(".value)");
            sqlBuilder.append(",0)");
            sqlBuilder.append(" as ");
            sqlBuilder.append(this.withClauseAliasName);
        }

        private void appendJoinIfIncluded(Set<KpiType> options, SqlBuilder sqlBuilder) {
            if (!options.contains(this)) {
                sqlBuilder.append(" left");
            }
            this.appendJoinTo(sqlBuilder);
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

        public void appendHavingTo(SqlBuilder sqlBuilder, DeviceDataQualityServiceImpl.SuspectsRange suspectsRange) {
            throw new UnsupportedOperationException(this.name() + " does not support HAVING clause");
        }
    }

    private enum AllDataType {
        VALIDATED {
            @Override
            public void appendSelectValueTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append("'ALLDATAVALIDATED',");
            }

            @Override
            public void appendSelectSlot0To(SqlBuilder sqlBuilder) {
                sqlBuilder.append("            min(slot0),");
            }

            @Override
            String operator() {
                return "=";
            }

            @Override
            public void appendGroupByTo(SqlBuilder sqlBuilder) {
                // Nothing to append
            }

            @Override
            public void appendWhereSlot0LargerThanZeroTo(SqlBuilder sqlBuilder) {
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
            public void appendSelectSlot0To(SqlBuilder sqlBuilder) {
                sqlBuilder.append("            sum(slot0),");
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

            @Override
            public void appendWhereSlot0LargerThanZeroTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append("            and kpivalues.slot0 > 0");
            }

            private void appendExpressionTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append("substr(kpim.name, 1, instr(kpim.name, '_') - 1)");
            }
        };

        abstract String operator();

        public abstract void appendSelectValueTo(SqlBuilder sqlBuilder);

        public abstract void appendSelectSlot0To(SqlBuilder sqlBuilder);

        public abstract void appendGroupByTo(SqlBuilder sqlBuilder);

        public abstract void appendWhereSlot0LargerThanZeroTo(SqlBuilder sqlBuilder);
    }

    ValidationOverviewSqlBuilder(List<EndDeviceGroup> deviceGroups, Range<Instant> range, Set<KpiType> kpiTypes, DeviceDataQualityServiceImpl.SuspectsRange suspectsRange, int from, int to) {
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
        this.appendActualQuery();
        this.appendGroupByClause();
        this.appendHavingClause();
        this.appendOrderByClause();
        this.sqlBuilder = this.sqlBuilder.asPageBuilder(this.from, this.to);
    }

    private void appendActualQuery() {
        this.appendSelectClause();
        this.appendFromClause();
        this.appendJoinClauses();
    }

    private void appendSelectClause() {
        this.sqlBuilder.append(" select dev.name, dev.serialnumber, dt.name as devtypename, dc.name as devconfigname");
        Stream
                .of(KpiType.TOTAL, KpiType.CHANNEL, KpiType.REGISTER, KpiType.ALL_DATA_VALIDATED)
                .forEach(kpiType -> kpiType.appendSelectTo(this.sqlBuilder));
        Stream
                .of(KpiType.THRESHOLD, KpiType.MISSING_VALUES, KpiType.READING_QUALITIES, KpiType.REGISTER_INCREASE)
                .forEach(kpiType -> kpiType.appendSelectTo(this.sqlBuilder));
    }

    private void appendFromClause() {
        this.sqlBuilder.append(" from ddc_device dev");
    }

    private void appendJoinClauses() {
        this.sqlBuilder.append(" join dtc_devicetype dt on dev.devicetype = dt.id");
        this.sqlBuilder.append(" join dtc_deviceconfig dc on dev.deviceconfigid = dc.id");
        KpiType.TOTAL.appendJoinTo(this.sqlBuilder);
        Stream
                .of(KpiType.REGISTER, KpiType.CHANNEL)
                .forEach(kpiType -> kpiType.appendJoinIfIncluded(this.kpiTypes, this.sqlBuilder));
        this.sqlBuilder.append(" join allDataValidatedValues allDataValidatedKpi ");
        this.sqlBuilder.append("   on allDataValidatedKpi.device = dev.meterid");
        this.sqlBuilder.append("  and allDataValidatedKpi.devicegroup = ");
        this.sqlBuilder.append(KpiType.TOTAL.kpiTableName);
        this.sqlBuilder.append(".devicegroup");
        Stream
                .of(KpiType.THRESHOLD, KpiType.MISSING_VALUES, KpiType.READING_QUALITIES, KpiType.REGISTER_INCREASE)
                .forEach(kpiType -> kpiType.appendJoinIfIncluded(this.kpiTypes, this.sqlBuilder));
    }

    private void appendGroupByClause() {
        this.sqlBuilder.append(" group by dev.name, dev.serialnumber, dt.name, dc.name");
    }

    private void appendHavingClause() {
        this.sqlBuilder.append(" having ");
        KpiType.TOTAL.appendHavingTo(this.sqlBuilder, this.suspectsRange);
    }

    private void appendOrderByClause() {
        this.sqlBuilder.append(" order by dev.name");
    }

    private void appendKpiTypeWithClauses() {
        Stream
                .of(KpiType.TOTAL, KpiType.REGISTER, KpiType.CHANNEL, KpiType.THRESHOLD, KpiType.MISSING_VALUES, KpiType.READING_QUALITIES, KpiType.REGISTER_INCREASE)
                .forEach(perform(KpiType::appendWithTo).with(this.sqlBuilder));
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
        allDataType.appendSelectSlot0To(this.sqlBuilder);
        this.sqlBuilder.append("            max(kpivalues.UTCSTAMP)");
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
        allDataType.appendWhereSlot0LargerThanZeroTo(this.sqlBuilder);
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