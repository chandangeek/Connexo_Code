/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl.calc;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class  DataQualityKpiSqlBuilder {

    enum ResultSetColumn {
        CHANNELID(1),
        READINGTIMESTAMP(2),
        READINGQUALITYTYPE(3),
        NOTSUSPECTFLAG(4),
        COUNTER(5);

        private int index;

        ResultSetColumn(int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }

    private Instant startTime;
    private Instant endTime;

    protected SqlBuilder sqlBuilder;

    DataQualityKpiSqlBuilder() {
        this.sqlBuilder = new SqlBuilder();
    }

    DataQualityKpiSqlBuilder init(Instant start, Instant end) {
        this.startTime = start;
        this.endTime = end;
        return this;
    }

    PreparedStatement prepare(Connection connection) throws SQLException {
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
        sqlBuilder.append(" AND q2.type = " + toSql(ReadingQualityType.of(getQualityCodeSystem(), QualityCodeIndex.SUSPECT)));
        sqlBuilder.append(" AND q2.actual = 'Y'");
        sqlBuilder.append(" , IDS_TIMESERIES t");
        sqlBuilder.append(" WHERE EXISTS (SELECT id FROM MTR_CHANNEL c WHERE q.channelid  = c.id AND c.timeseriesid = t.id)");
        sqlBuilder.append(" AND q.channelid IN (");
        this.appendChannelsSubQuery();
        sqlBuilder.append(" )");
        sqlBuilder.append(" AND (q.type IN (");
        this.appendReadingQualityTypes();
        sqlBuilder.append(" ) OR q.type LIKE " + anyReadingQualityTypeOfCategory(QualityCodeCategory.VALIDATION));
        sqlBuilder.append("   OR q.type LIKE " + anyReadingQualityTypeOfCategory(QualityCodeCategory.ESTIMATED));
        sqlBuilder.append(" ) AND q.actual = 'Y'");
        sqlBuilder.append("   AND q.readingtimestamp > ");
        sqlBuilder.addLong(this.startTime.toEpochMilli());
        sqlBuilder.append("   AND q.readingtimestamp <= ");
        sqlBuilder.addLong(this.endTime.toEpochMilli());
        sqlBuilder.append(")");
    }

    abstract QualityCodeSystem getQualityCodeSystem();

    abstract void appendChannelsSubQuery();

    private void appendReadingQualityTypes() {
        String fixedReadingQualityTypeCodes = Stream.of(
                ReadingQualityType.of(getQualityCodeSystem(), QualityCodeIndex.SUSPECT),
                ReadingQualityType.of(getQualityCodeSystem(), QualityCodeIndex.KNOWNMISSINGREAD),
                ReadingQualityType.of(getQualityCodeSystem(), QualityCodeIndex.EDITGENERIC),
                ReadingQualityType.of(getQualityCodeSystem(), QualityCodeIndex.ADDED),
                ReadingQualityType.of(getQualityCodeSystem(), QualityCodeIndex.REJECTED),
                ReadingQualityType.of(getQualityCodeSystem(), QualityCodeIndex.ESTIMATEGENERIC),
                ReadingQualityType.of(getQualityCodeSystem(), QualityCodeIndex.PROJECTEDGENERIC),
                ReadingQualityType.of(getQualityCodeSystem(), QualityCodeIndex.ACCEPTED))
                .map(this::toSql)
                .collect(Collectors.joining(", "));
        sqlBuilder.append(fixedReadingQualityTypeCodes);
    }

    private String toSql(ReadingQualityType readingQualityType) {
        return "'" + readingQualityType.getCode() + "'";
    }

    private String anyReadingQualityTypeOfCategory(QualityCodeCategory category) {
        return "'" + getQualityCodeSystem().ordinal() + "." + category.ordinal() + ".%'";
    }

    private void appendActualQuery() {
        sqlBuilder.append("SELECT channelid, TRUNC(readingtimestamp, 'DDD'), type, notsuspect, COUNT(*) FROM allReadingQualities");
        sqlBuilder.append(" GROUP BY channelid, TRUNC(readingtimestamp, 'DDD'), type, notsuspect");
    }
}
