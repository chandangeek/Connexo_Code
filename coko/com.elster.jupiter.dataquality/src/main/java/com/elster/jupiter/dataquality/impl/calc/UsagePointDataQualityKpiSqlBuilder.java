/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl.calc;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@LiteralSql
public class UsagePointDataQualityKpiSqlBuilder implements DataQualityKpiSqlBuilder {

    private final UsagePointGroup usagePointGroup;
    private final MetrologyPurpose metrologyPurpose;

    private Instant startTime;
    private Instant endTime;

    private SqlBuilder sqlBuilder;

    UsagePointDataQualityKpiSqlBuilder(UsagePointGroup usagePointGroup, MetrologyPurpose metrologyPurpose) {
        this.usagePointGroup = usagePointGroup;
        this.metrologyPurpose = metrologyPurpose;
        this.sqlBuilder = new SqlBuilder();
    }

    @Override
    public UsagePointDataQualityKpiSqlBuilder init(Instant start, Instant end) {
        this.startTime = start;
        this.endTime = end;
        return this;
    }

    @Override
    public PreparedStatement prepare(Connection connection) throws SQLException {
        return buildSql().prepare(connection);
    }

    private SqlBuilder buildSql() {
        this.appendAllReadingQualitiesWithClause();
        this.appendActualQuery();
        return this.sqlBuilder;
    }

    private void appendAllReadingQualitiesWithClause() {
        sqlBuilder.append("WITH allReadingQualities (channelid, readingtimestamp, type, notsuspect) AS (");
        sqlBuilder.append(" SELECT q.channelid, utc2date(q.readingtimestamp, t.timezonename), q.type, CASE WHEN q2.channelid IS NULL THEN 'Y' ELSE 'N' END");
        sqlBuilder.append(" FROM MTR_READINGQUALITY q LEFT JOIN MTR_READINGQUALITY q2");
        sqlBuilder.append(" ON  q.readingtimestamp = q2.readingtimestamp");
        sqlBuilder.append(" AND q.channelid = q2.channelid");
        sqlBuilder.append(" AND q2.type = " + toSql(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT)));
        sqlBuilder.append(" AND q2.actual = 'Y'");
        sqlBuilder.append(" , IDS_TIMESERIES t");
        sqlBuilder.append(" WHERE EXISTS (SELECT id FROM MTR_CHANNEL c WHERE q.channelid  = c.id AND c.timeseriesid = t.id)");
        sqlBuilder.append(" AND q.channelid IN (");
        this.appendChannelsSubQuery();
        sqlBuilder.append(" )");
        sqlBuilder.append(" AND (q.type IN (");
        this.appendReadingQualityTypes();
        sqlBuilder.append(" ) OR q.type LIKE '3.6.%'");
        sqlBuilder.append("   OR q.type LIKE '3.8.%'");
        sqlBuilder.append(" ) AND q.actual = 'Y'");
        sqlBuilder.append("   AND q.readingtimestamp > ");
        sqlBuilder.addLong(this.startTime.toEpochMilli());
        sqlBuilder.append("   AND q.readingtimestamp <= ");
        sqlBuilder.addLong(this.endTime.toEpochMilli());
        sqlBuilder.append(")");
    }

    private void appendChannelsSubQuery() {
        sqlBuilder.append("SELECT ch.id FROM MTR_CHANNEL ch");
        sqlBuilder.append("    JOIN MTR_EFFECTIVE_CONTRACT efc ON efc.channels_container = ch.channel_container");
        sqlBuilder.append("    JOIN MTR_METROLOGY_CONTRACT mc ON mc.id = efc.metrology_contract");
        sqlBuilder.append("    JOIN MTR_USAGEPOINTMTRCONFIG upmc ON upmc.id = efc.effective_conf");
        sqlBuilder.append("    WHERE mc.metrology_purpose = ");
        sqlBuilder.addLong(metrologyPurpose.getId());
        sqlBuilder.append("      AND upmc.usagepoint IN (");
        sqlBuilder.add(this.usagePointGroup.toSubQuery("ID").toFragment());
        sqlBuilder.append(")");
    }

    private void appendReadingQualityTypes() {
        String fixedReadingQualityTypeCodes = Stream.of(
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT),
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.KNOWNMISSINGREAD),
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.EDITGENERIC),
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ADDED),
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.REJECTED),
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ESTIMATEGENERIC),
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ACCEPTED))
                .map(this::toSql)
                .collect(Collectors.joining(", "));
        sqlBuilder.append(fixedReadingQualityTypeCodes);
    }

    private String toSql(ReadingQualityType readingQualityType) {
        return "'" + readingQualityType.getCode() + "'";
    }

    private void appendActualQuery() {
        sqlBuilder.append("SELECT channelid, TRUNC(readingtimestamp, 'DDD'), type, notsuspect, COUNT(*) FROM allReadingQualities");
        sqlBuilder.append(" GROUP BY channelid, TRUNC(readingtimestamp, 'DDD'), type, notsuspect");
    }
}
