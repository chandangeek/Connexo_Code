/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.ids.TimeSeriesJournalEntry;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.inject.Provider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@LiteralSql
public final class VaultImpl implements IVault {

    //persistent fields
    private String componentName;
    private long id;
    private String description;
    private Instant minTime;
    private Instant maxTime;
    private int slotCount;
    private int textSlotCount;
    private boolean localTime;
    private boolean regular;
    private boolean journal;
    private boolean partitioned;
    private boolean active;
    private int retentionDays;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private final DataModel dataModel;
    private final Clock clock;
    private final ThreadPrincipalService threadPrincipalService;
    private final Provider<TimeSeriesImpl> timeSeriesProvider;

    @Inject
    VaultImpl(DataModel dataModel, Clock clock, ThreadPrincipalService threadPrincipalService, Provider<TimeSeriesImpl> timeSeriesProvider) {
        this.dataModel = dataModel;
        this.clock = clock;
        this.threadPrincipalService = threadPrincipalService;
        this.timeSeriesProvider = timeSeriesProvider;
    }

    VaultImpl init(String componentName, long id, String description, int slotCount, int textSlotCount, boolean regular) {
        this.componentName = componentName;
        this.id = id;
        this.description = description;
        this.slotCount = slotCount;
        this.textSlotCount = textSlotCount;
        this.regular = regular;
        this.localTime = regular;
        this.journal = true;
        this.partitioned = dataModel.getSqlDialect().hasPartitioning();
        this.active = false;
        this.minTime = Instant.ofEpochMilli(0);
        return this;
    }

    @Override
    public String getComponentName() {
        return componentName;
    }

    @Override
    public long getId() {
        return id;
    }

    public Instant getCreateDate() {
        return createTime;
    }

    public Instant getModDate() {
        return modTime;
    }

    @Override
    public String toString() {
        return "Vault " + id + ": " + description + "(version: " + version + " created: " + getCreateDate() + " modified: " + getModDate() + ")";
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
        dataModel.update(this, "description");
    }


    @Override
    public Instant getMinDate() {
        return minTime;
    }

    @Override
    public Instant getMaxDate() {
        return maxTime;
    }

    @Override
    public int getSlotCount() {
        return slotCount;
    }

    @Override
    public int getTextSlotCount() {
        return textSlotCount;
    }

    @Override
    public boolean hasJournal() {
        return journal;
    }

    @Override
    public boolean isRegular() {
        return regular;
    }

    @Override
    public boolean hasLocalTime() {
        return localTime;
    }

    @Override
    public boolean isPartitioned() {
        return partitioned;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void activate(Instant to) {
        try {
            doActivate(Objects.requireNonNull(to));
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
        this.active = true;
        this.maxTime = to;
        dataModel.update(this, "active", "maxTime");
    }

    private void doActivate(Instant to) throws SQLException {
        try (Connection connection = getConnection(true)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(createTableDdl(to, false));
                if (hasJournal()) {
                    statement.execute(createTableDdl(to, true));
                }
            }
        }
    }

    public Instant extendTo(Instant to, Logger logger) {
        if (!isActive()) {
            throw new IllegalStateException("Vault " + getTableName() + " is not active");
        }
        if (!to.isAfter(getMaxDate())) {
            return getMaxDate();
        }
        this.maxTime = to;
        if (isPartitioned()) {
            this.maxTime = dataModel.partitionCreator(getTableName(), logger).create(to);
            if (hasJournal()) {
                dataModel.partitionCreator(getJournalTableName(), logger).create(to);
            }
        }
        dataModel.update(this, "maxTime");
        return maxTime;
    }

    private String createTableDdl(Instant to, boolean journal) {
        StringBuilder builder = new StringBuilder("create table ");
        builder.append(getTableName(journal));
        builder.append("(TIMESERIESID NUMBER NOT NULL,UTCSTAMP NUMBER NOT NULL,VERSIONCOUNT NUMBER NOT NULL,RECORDTIME NUMBER NOT NULL");
        if (journal) {
            builder.append(",JOURNALTIME NUMBER NOT NULL");
            builder.append(",USERNAME VARCHAR(80) NOT NULL");
        }
        if (hasLocalTime()) {
            builder.append(",LOCALDATE DATE NOT NULL");
        }
        for (int i = 0; i < getSlotCount(); i++) {
            builder.append(",SLOT");
            builder.append(i);
            builder.append(" NUMBER");
        }
        for (int i = 0; i < getTextSlotCount(); i++) {
            builder.append(",TEXTSLOT");
            builder.append(i);
            builder.append(" VARCHAR2(4000)");
        }
        builder.append(",CONSTRAINT ");
        builder.append(getTablePrimaryKeyConstraintName(journal));
        builder.append(" PRIMARY KEY(TIMESERIESID, UTCSTAMP");
        if (journal) {
            builder.append(", JOURNALTIME, VERSIONCOUNT");
        }
        builder.append("))");
        if (dataModel.getSqlDialect().hasIndexOrganizedTables()) {
            builder.append(" ORGANIZATION INDEX COMPRESS 1 ");
            if (textSlotCount > 0) {
                builder.append(" OVERFLOW ");
            }
        }
        if (isPartitioned()) {
            builder.append("PARTITION BY RANGE(utcstamp) (PARTITION ");
            builder.append(getPartitionName(to));
            builder.append(" VALUES LESS THAN (");
            builder.append(to.toEpochMilli() + 1L);
            builder.append("))");
        }
        return builder.toString();
    }

    private String getTableName() {
        return getTableName(false);
    }

    private String getJournalTableName() {
        return getTableName(true);
    }

    private String getBaseName(boolean journal) {
        return journal ? "VAULTJRNL" : "VAULT";
    }

    private String getTableName(boolean journal) {
        return "IDS_" + getBaseName(journal) + "_" + getComponentName() + "_" + getId();
    }

    private String getTablePrimaryKeyConstraintName(boolean journal) {
        return "IDS_PK" + getBaseName(journal) + "_" + getComponentName() + "_" + getId();
    }


    private String getPartitionName(Instant to) {
        return "P" + to.toString().replaceAll("-", "").replaceAll(":", "");
    }

    @Override
    public TimeSeriesImpl createRegularTimeSeries(RecordSpec spec, ZoneId zoneId, TemporalAmount interval, int hourOffset) {
        TimeSeriesImpl timeSeries = timeSeriesProvider.get().init(this, spec, zoneId, interval, hourOffset);
        timeSeries.persist();
        return timeSeries;
    }

    @Override
    public TimeSeriesImpl createIrregularTimeSeries(RecordSpec spec, ZoneId zoneId) {
        TimeSeriesImpl timeSeries = timeSeriesProvider.get().init(this, spec, zoneId);
        timeSeries.persist();
        return timeSeries;
    }

    @Override
    public boolean isValidInstant(Instant instant) {
        return isActive() && minTime.isBefore(instant) && !maxTime.isBefore(instant);
    }

    private SqlBuilder selectSql(TimeSeriesImpl timeSeries) {
        SqlBuilder builder = selectSql(timeSeries.getRecordSpec());
        builder.append(" TIMESERIESID =");
        builder.addLong(timeSeries.getId());
        return builder;
    }

    private SqlBuilder selectSql(TimeSeriesImpl timeSeries, List<Pair<String, String>> limitedRecordSpecFieldAndAliasNames) {
        List<String> limitedRecordSpecFieldNames = limitedRecordSpecFieldAndAliasNames.stream().map(Pair::getFirst).collect(Collectors.toList());
        RecordSpecImpl recordSpec = timeSeries.getRecordSpec();
        List<String> columnNames =
                recordSpec
                        .getFieldSpecs()
                        .stream()
                        .filter(each -> limitedRecordSpecFieldNames.contains(each.getName()))
                        .map(fieldSpec -> this.toColumnNameWithAlias(recordSpec, fieldSpec, limitedRecordSpecFieldAndAliasNames))
                        .collect(Collectors.toList());
        if (this.hasLocalTime() && limitedRecordSpecFieldNames.contains("LOCALDATE")) {
            columnNames.add("LOCALDATE" + this.localDateAliasNameIfAny(limitedRecordSpecFieldAndAliasNames));
        }
        SqlBuilder builder = selectSql(columnNames);
        builder.append(" TIMESERIESID =");
        builder.addLong(timeSeries.getId());
        return builder;
    }

    private String toColumnNameWithAlias(RecordSpecImpl recordSpec, FieldSpec fieldSpec, List<Pair<String, String>> limitedRecordSpecFieldAndAliasNames) {
        return recordSpec.columnName(fieldSpec) + limitedRecordSpecFieldAndAliasNames
                .stream()
                .filter(pair -> pair.getFirst().equals(fieldSpec.getName()))
                .map(pairToAliasName())
                .findFirst()
                .orElse("");
    }

    private String localDateAliasNameIfAny(List<Pair<String, String>> recordSpecFieldAndAliasNames) {
        return recordSpecFieldAndAliasNames
                .stream()
                .filter(pair -> "LOCALDATE".equals(pair.getFirst()))
                .findFirst()
                .map(pairToAliasName())
                .orElse("");
    }

    private Function<Pair<String, String>, String> pairToAliasName() {
        return pair -> " AS " + pair.getLast();
    }

    private SqlBuilder selectJournalSql(TimeSeriesImpl timeSeries) {
        SqlBuilder builder = selectJournalSql(timeSeries.getRecordSpec());
        builder.append(" TIMESERIESID =");
        builder.addLong(timeSeries.getId());
        return builder;
    }

    private StringBuilder baseJournalSql(RecordSpecImpl recordSpec) {
        StringBuilder builder = new StringBuilder("insert into ");
        builder.append(getJournalTableName());
        builder.append("(TIMESERIESID,UTCSTAMP,VERSIONCOUNT,RECORDTIME,JOURNALTIME,USERNAME");
        if (hasLocalTime()) {
            builder.append(",LOCALDATE");
        }
        recordSpec.columnNames().forEach(column -> builder.append(", " + column));
        builder.append(") (SELECT TIMESERIESID,UTCSTAMP,VERSIONCOUNT,RECORDTIME,?,?");
        if (hasLocalTime()) {
            builder.append(",LOCALDATE");
        }
        recordSpec.columnNames().forEach(column -> builder.append(", " + column));
        builder.append(" FROM ");
        builder.append(getTableName());
        return builder;
    }

    String journalSql(RecordSpecImpl recordSpec) {
        StringBuilder builder = baseJournalSql(recordSpec);
        builder.append(" WHERE TIMESERIESID = ? AND UTCSTAMP = ?)");
        return builder.toString();
    }

    SqlBuilder journalSql(TimeSeriesImpl timeSeries, Range<Instant> range) {
        SqlBuilder builder = new SqlBuilder(baseJournalSql(timeSeries.getRecordSpec()).toString());
        builder.add(new SqlFragment() {
            @Override
            public String getText() {
                return "";
            }

            @Override
            public int bind(PreparedStatement statement, int position) throws SQLException {
                statement.setLong(position++, System.currentTimeMillis());
                return position;
            }
        });
        builder.add(new SqlFragment() {
            @Override
            public String getText() {
                return "";
            }

            @Override
            public int bind(PreparedStatement statement, int position) throws SQLException {
                statement.setString(position++, threadPrincipalService.getPrincipal().getName());
                return position;
            }
        });
        builder.append(" WHERE TIMESERIESID = ");
        builder.addLong(timeSeries.getId());
        builder.add("UTCSTAMP", range, "AND");
        builder.append(")");
        return builder;
    }

    @Override
    public List<TimeSeriesEntry> getEntries(TimeSeriesImpl timeSeries, Range<Instant> interval) {
        try {
            return doGetEntries(timeSeries, interval);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    @Override
    public List<TimeSeriesJournalEntry> getJournalEntries(TimeSeriesImpl timeSeries, Range<Instant> interval, Range<Instant> changed) {
        try {
            return doGetJournalEntries(timeSeries, interval, changed);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    @Override
    public SqlFragment getRawValuesSql(TimeSeriesImpl timeSeries, Range<Instant> interval, Pair<String, String>... fieldSpecAndAliasNames) {
        return this.rangeSql(timeSeries, interval, Arrays.asList(fieldSpecAndAliasNames));
    }

    @Override
    public List<TimeSeriesEntry> getEntriesUpdatedSince(TimeSeriesImpl timeSeries, Range<Instant> interval, Instant since) {
        try {
            return doGetEntriesSince(timeSeries, interval, since);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    @Override
    public Optional<TimeSeriesEntry> getEntry(TimeSeriesImpl timeSeries, Instant when) {
        try {
            return Optional.ofNullable(doGetEntry(timeSeries, when));
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    @Override
    public Optional<TimeSeriesEntry> getJournaledEntry(TimeSeriesImpl timeSeries, Instant when, Instant at) {
        try {
            return Optional.ofNullable(doGetJournalEntry(timeSeries, when, at));
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    private SqlBuilder rangeSql(TimeSeriesImpl timeSeries, Range<Instant> range) {
        SqlBuilder builder = selectSql(timeSeries);
        this.appendRange(range, builder);
        builder.append(" order by UTCSTAMP");
        return builder;
    }

    private SqlBuilder rangeJournalSql(TimeSeriesImpl timeSeries, Range<Instant> interval, Range<Instant> changed) {
        SqlBuilder builder = selectJournalSql(timeSeries, interval, changed);
        return builder;
    }

    private SqlBuilder rangeSql(TimeSeriesImpl timeSeries, Range<Instant> range, List<Pair<String, String>> recordSpecFieldNames) {
        SqlBuilder builder = selectSql(timeSeries, recordSpecFieldNames);
        this.appendRange(range, builder);
        return builder;
    }

    private SqlBuilder rangeUpdatedSinceSql(TimeSeriesImpl timeSeries, Range<Instant> range, Instant since) {
        SqlBuilder builder = selectSql(timeSeries);
        appendRange(range, builder);
        builder.append(" and RECORDTIME >");
        builder.addLong(since.toEpochMilli());
        builder.append(" order by UTCSTAMP");
        return builder;
    }

    private void appendRange(Range<Instant> interval, SqlBuilder builder) {
        builder.append(" AND UTCSTAMP >");
        if (interval.hasLowerBound() && interval.lowerBoundType() == BoundType.CLOSED) {
            builder.append("=");
        }
        builder.addLong(interval.hasLowerBound() ? interval.lowerEndpoint().toEpochMilli() : Long.MIN_VALUE);
        builder.append("AND UTCSTAMP <");
        if (interval.hasUpperBound() && interval.upperBoundType() == BoundType.CLOSED) {
            builder.append("=");
        }
        builder.addLong(interval.hasUpperBound() ? interval.upperEndpoint().toEpochMilli() : Long.MAX_VALUE);
    }

    private void appendChanged(Range<Instant> changed, SqlBuilder builder) {
        builder.append(" AND ((JOURNALTIME >");
        if (changed.hasLowerBound() && changed.lowerBoundType() == BoundType.CLOSED) {
            builder.append("=");
        }
        builder.addLong(changed.hasLowerBound() ? changed.lowerEndpoint().toEpochMilli() : Long.MIN_VALUE);
        builder.append("AND JOURNALTIME <");
        if (changed.hasUpperBound() && changed.upperBoundType() == BoundType.CLOSED) {
            builder.append("=");
        }
        builder.addLong(changed.hasUpperBound() ? changed.upperEndpoint().toEpochMilli() : Long.MAX_VALUE);
        builder.append(") OR JOURNALTIME IS NULL)");
    }

    private SqlBuilder entrySql(TimeSeriesImpl timeSeries, Instant when) {
        SqlBuilder builder = selectSql(timeSeries);
        builder.append(" AND UTCSTAMP =");
        builder.addLong(when.toEpochMilli());
        return builder;
    }

    private SqlBuilder entryJournalSql(TimeSeriesImpl timeSeries, Instant when) {
        SqlBuilder builder = selectJournalSql(timeSeries);
        builder.append(" AND UTCSTAMP =");
        builder.addLong(when.toEpochMilli());
        return builder;
    }

    private List<TimeSeriesEntry> doGetEntries(TimeSeriesImpl timeSeries, Range<Instant> interval) throws SQLException {
        List<TimeSeriesEntry> result = new ArrayList<>();
        try (Connection connection = getConnection(false)) {
            try (PreparedStatement statement = rangeSql(timeSeries, interval).prepare(connection)) {
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        result.add(new TimeSeriesEntryImpl(timeSeries, rs));
                    }
                }
            }
        }
        return result;
    }

    private List<TimeSeriesJournalEntry> doGetJournalEntries(TimeSeriesImpl timeSeries, Range<Instant> interval, Range<Instant> changed) throws SQLException {
        List<TimeSeriesJournalEntry> result = new ArrayList<>();
        try (Connection connection = getConnection(false)) {
            try (PreparedStatement statement = rangeJournalSql(timeSeries, interval, changed).prepare(connection)) {
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        result.add(new TimeSeriesJournalEntryImpl(timeSeries, rs));
                    }
                }
            }
        }
        return result;
    }

    private List<TimeSeriesEntry> doGetEntriesSince(TimeSeriesImpl timeSeries, Range<Instant> interval, Instant since) throws SQLException {
        List<TimeSeriesEntry> result = new ArrayList<>();
        try (Connection connection = getConnection(false)) {
            try (PreparedStatement statement = rangeUpdatedSinceSql(timeSeries, interval, since).prepare(connection)) {
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        result.add(new TimeSeriesEntryImpl(timeSeries, rs));
                    }
                }
            }
        }
        return result;
    }

    private TimeSeriesEntry doGetEntry(TimeSeriesImpl timeSeries, Instant when) throws SQLException {
        try (Connection connection = getConnection(false)) {
            SqlBuilder sqlBuilder = entrySql(timeSeries, when);
            try (PreparedStatement statement = sqlBuilder.prepare(connection)) {
                try (ResultSet rs = statement.executeQuery()) {
                    return rs.next() ? new TimeSeriesEntryImpl(timeSeries, rs) : null;
                }
            }
        }
    }

    private TimeSeriesEntry doGetJournalEntry(TimeSeriesImpl timeSeries, Instant when, Instant at) throws SQLException {
        try (Connection connection = getConnection(false)) {
            SqlBuilder journalSql = entryJournalSql(timeSeries, when);
            journalSql.append(" and RECORDTIME <=");
            journalSql.addLong(at.toEpochMilli());
            journalSql.append("and JOURNALTIME >");
            journalSql.addLong(at.toEpochMilli());
            try (PreparedStatement statement = journalSql.prepare(connection)) {
                try (ResultSet rs = statement.executeQuery()) {
                    return rs.next() ? new TimeSeriesEntryImpl(timeSeries, rs) : null;
                }
            }
        }
    }

    SqlBuilder selectSql(RecordSpecImpl recordSpec) {
        return this.selectSql(recordSpec.columnNames());
    }

    SqlBuilder selectSql(List<String> recordSpecColumnNames) {
        SqlBuilder builder = new SqlBuilder("select timeseriesid , utcstamp , versioncount , recordtime ");
        for (String column : recordSpecColumnNames) {
            builder.append(",");
            builder.append(column);
        }
        builder.append(" FROM ");
        builder.append(getTableName());
        builder.append(" WHERE");
        return builder;
    }

    SqlBuilder selectJournalSql(RecordSpecImpl recordSpec) {
        SqlBuilder builder = new SqlBuilder("select timeseriesid , utcstamp , versioncount , recordtime ");
        for (String column : recordSpec.columnNames()) {
            builder.append(",");
            builder.append(column);
        }
        builder.append(" FROM ");
        builder.append(getJournalTableName());
        builder.append(" WHERE");
        return builder;
    }

    SqlBuilder selectJournalSql(TimeSeriesImpl timeSeries, Range<Instant> interval, Range<Instant> changed) {
        String columnNames = "";
        for (String column : timeSeries.getRecordSpec().columnNames()) {
            columnNames += ", " + column;
        }

        SqlBuilder builder = new SqlBuilder("select TIMESERIESID, UTCSTAMP, VERSIONCOUNT, RECORDTIME " + columnNames + ", JOURNALTIME, USERNAME, ISACTIVE  from ");
        builder.append(" ( ");

        builder.append("select R1.TIMESERIESID, R1.UTCSTAMP, R1.VERSIONCOUNT, R1.RECORDTIME, R1.ISACTIVE, R1.JOURNALTIME, R2.USERNAME ");
        for (String column : timeSeries.getRecordSpec().columnNames()) {
            builder.append(", R1.");
            builder.append(column);
        }
        builder.append(" FROM ");
        builder.append(" ( ");
        builder.append("    select TIMESERIESID, UTCSTAMP, VERSIONCOUNT, RECORDTIME, JOURNALTIME, USERNAME, ISACTIVE " + columnNames + "  from ");
        builder.append("    ( ");
        builder.append("        select TIMESERIESID, UTCSTAMP, VERSIONCOUNT, RECORDTIME, 0 JOURNALTIME, '' USERNAME, 1 ISACTIVE " + columnNames + "  from " + getTableName());
        builder.append(" WHERE TIMESERIESID =" + timeSeries.getId());
        builder.append("        union all ");
        builder.append("        select TIMESERIESID, UTCSTAMP, VERSIONCOUNT, RECORDTIME, JOURNALTIME, USERNAME, 0 ISACTIVE " + columnNames + "  from " + getJournalTableName());
        builder.append(" WHERE TIMESERIESID =" + timeSeries.getId());
        builder.append("    ) ");
        builder.append(" ) R1 ");
        builder.append(" LEFT JOIN ");
        builder.append(" ( ");
        builder.append("    select TIMESERIESID, UTCSTAMP, VERSIONCOUNT, RECORDTIME,  JOURNALTIME, USERNAME, ISACTIVE " + columnNames + "  from ");
        builder.append("    ( ");
        builder.append("        select TIMESERIESID, UTCSTAMP, VERSIONCOUNT, RECORDTIME, 0 JOURNALTIME, '' USERNAME, 1 ISACTIVE " + columnNames + "  from " + getTableName());
        builder.append(" WHERE TIMESERIESID =" + timeSeries.getId());
        builder.append("        union all ");
        builder.append("        select TIMESERIESID, UTCSTAMP, VERSIONCOUNT, RECORDTIME, JOURNALTIME, USERNAME, 0 ISACTIVE " + columnNames + "  from " + getJournalTableName());
        builder.append(" WHERE TIMESERIESID =" + timeSeries.getId());
        builder.append("    ) ");
        builder.append(" ) R2 ");
        builder.append(" ON R1.VERSIONCOUNT = R2.VERSIONCOUNT+1 and R1.UTCSTAMP = R2.UTCSTAMP ");
        builder.append("    ) ");
        builder.append(" WHERE 1=1 ");
        this.appendRange(interval, builder);
        this.appendChanged(changed, builder);
        builder.append(" order by UTCSTAMP DESC, VERSIONCOUNT desc");
        return builder;
    }

    String insertSql(RecordSpecImpl recordSpec) {
        StringBuilder builder = new StringBuilder("insert into ");
        builder.append(getTableName());
        builder.append(" (");
        builder.append("TIMESERIESID, UTCSTAMP, VERSIONCOUNT, RECORDTIME, ");
        if (hasLocalTime()) {
            builder.append("LOCALDATE, ");
        }
        builder.append(String.join(", ", recordSpec.columnNames()));
        builder.append(") VALUES (?,?,1,?");
        if (hasLocalTime()) {
            builder.append(",?");
        }
        recordSpec.getFieldSpecs().forEach(fieldSpec -> builder.append(",?"));
        builder.append(")");
        return builder.toString();
    }

    String updateSql(RecordSpecImpl recordSpec) {
        StringBuilder builder = new StringBuilder("update ");
        builder.append(getTableName());
        builder.append(" SET VERSIONCOUNT = VERSIONCOUNT + 1, RECORDTIME = ?");
        for (String column : recordSpec.columnNames()) {
            builder.append(",");
            builder.append(column);
            builder.append(" = ?");
        }
        builder.append(" WHERE TIMESERIESID = ? and UTCSTAMP = ?");
        return builder.toString();
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof VaultImpl)) {
            return false;
        }
        VaultImpl o = (VaultImpl) other;
        return this.componentName.equals(o.componentName) && this.id == o.id;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(componentName, id);
    }

    private Connection getConnection(boolean transactionRequired) throws SQLException {
        return dataModel.getConnection(transactionRequired);
    }

    void persist() {
        dataModel.persist(this);
    }

    @Override
    public List<TimeSeriesEntry> getEntriesBefore(TimeSeriesImpl timeSeries, Instant when, int entryCount, boolean includeBoundary) {
        try {
            return doGetEntriesBefore(timeSeries, when, entryCount, includeBoundary);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    private List<TimeSeriesEntry> doGetEntriesBefore(TimeSeriesImpl timeSeries, Instant when, int entryCount, boolean includeBoundary) throws SQLException {
        List<TimeSeriesEntry> result = new ArrayList<>();
        try (Connection connection = getConnection(false)) {
            try (PreparedStatement statement = entriesBeforeSql(timeSeries, includeBoundary, when, entryCount).prepare(connection)) {
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        result.add(new TimeSeriesEntryImpl(timeSeries, rs));
                    }
                }
            }
        }
        return result;
    }

    private SqlBuilder entriesBeforeSql(TimeSeriesImpl timeSeries, boolean includeBoundary, Instant when, int entryCount) {
        SqlBuilder base = selectSql(timeSeries);
        base.append(" AND UTCSTAMP ");
        base.append(includeBoundary ? "<= " : "<");
        base.addLong(when.toEpochMilli());
        base.append("ORDER BY UTCSTAMP DESC ");
        SqlBuilder builder = new SqlBuilder("SELECT * from (");
        builder.add(base);
        builder.append(") where ROWNUM <=");
        builder.addInt(entryCount);
        return builder;
    }

    @Override
    public void removeEntries(TimeSeriesImpl timeSeries, Range<Instant> range) {
        try (Connection connection = getConnection(true)) {
            if (journal) {
                try (PreparedStatement statement = journalSql(timeSeries, range).prepare(connection)) {
                    statement.executeUpdate();
                }
            }
            try (PreparedStatement statement = deleteSql(timeSeries, range).prepare(connection)) {
                statement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    SqlBuilder deleteSql(TimeSeriesImpl timeSeries, Range<Instant> range) {
        SqlBuilder builder = new SqlBuilder("DELETE FROM ");
        builder.append(getTableName());
        builder.append(" WHERE TIMESERIESID = ");
        builder.addLong(timeSeries.getId());
        builder.add("UTCSTAMP", range, "AND");
        return builder;
    }

    @Override
    public void purge(Logger logger) {
        if (retentionDays == 0) {
            return;
        }
        Instant instant = clock.instant().minus(retentionDays, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        if (isPartitioned()) {
            logger.info("Removing data up to " + instant + " for time series vault " + getTableName());
            dataModel.dataDropper(getTableName(), logger).drop(instant);
            if (hasJournal()) {
                dataModel.dataDropper(getJournalTableName(), logger).drop(instant);
            }
        }
        if (instant.isAfter(minTime)) {
            this.minTime = instant;
            dataModel.update(this, "minTime");
        }
    }

    @Override
    public Period getRetention() {
        return Period.ofDays(retentionDays == 0 ? 360 * 99 : retentionDays);
    }

    @Override
    public void setRetentionDays(int numberOfDays) {
        if (numberOfDays <= 0) {
            throw new IllegalArgumentException();
        }
        this.retentionDays = numberOfDays;
        dataModel.update(this, "retentionDays");
    }
}
