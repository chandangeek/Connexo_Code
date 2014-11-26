package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import javax.inject.Provider;
import javax.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

@LiteralSql
public class VaultImpl implements Vault {

    private static final int ID_COLUMN_INDEX = 1;
    private static final int FROM_COLUMN_INDEX = 2;
    private static final int TO_COLUMN_INDEX = 3;
    private static final int WHEN_COLUMN_INDEX = 2;

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
    private boolean partition;
    private boolean active;
    private long version;
    private Instant createTime;
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private final DataModel dataModel;
    private final Provider<TimeSeriesImpl> timeSeriesProvider;

    @Inject
    VaultImpl(DataModel dataModel, Provider<TimeSeriesImpl> timeSeriesProvider) {
        this.dataModel = dataModel;
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
        this.partition = dataModel.getSqlDialect().hasPartitioning();
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
        return partition;
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

    private String createTableDdl(Instant to, boolean journal) {
        StringBuilder builder = new StringBuilder("create table ");
        builder.append(getTableName(journal));
        builder.append("(TIMESERIESID NUMBER NOT NULL,UTCSTAMP NUMBER NOT NULL,VERSIONCOUNT NUMBER NOT NULL,RECORDTIME NUMBER NOT NULL");
        if (journal) {
            builder.append(",JOURNALTIME NUMBER NOT NULL");
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
        return "P" + to.toString().replaceAll("-","").replaceAll(":","");
    }

    @Override
    public void addPartition(Instant to) {
        if (!isActive() || !Objects.requireNonNull(to).isAfter(getMaxDate())) {
            throw new IllegalArgumentException();
        }
        if (isPartitioned()) {
            try {
                doAddPartition(to);
            } catch (SQLException ex) {
                throw new UnderlyingSQLFailedException(ex);
            }
        }
        this.maxTime = to;
        dataModel.update(this, "maxTime");
    }

    private void doAddPartition(Instant to) throws SQLException {
        try (Connection connection = getConnection(true)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(addTablePartitionDdl(to, getTableName()));
                if (hasJournal()) {
                    statement.execute(addTablePartitionDdl(to, getJournalTableName()));
                }
            }
        }
    }


    private String addTablePartitionDdl(Instant to, String table) {
        StringBuilder builder = new StringBuilder("alter table ");
        builder.append(table);
        builder.append(" add partition ");
        builder.append(getPartitionName(to));
        builder.append(" values less than (");
        builder.append(to.toEpochMilli() + 1L);
        builder.append(")");
        return builder.toString();
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

    private StringBuilder selectSql(TimeSeriesImpl timeSeries) {
        StringBuilder builder = selectSql(timeSeries.getRecordSpec());
        builder.append(" TIMESERIESID = ?");
        return builder;
    }

    private StringBuilder baseJournalSql(RecordSpecImpl recordSpec) {
        StringBuilder builder = new StringBuilder("insert into ");
        builder.append(getJournalTableName());
        builder.append("(TIMESERIESID,UTCSTAMP,VERSIONCOUNT,RECORDTIME,JOURNALTIME");
        if (hasLocalTime()) {
            builder.append(",LOCALDATE");
        }
        recordSpec.columnNames().forEach(column -> builder.append(", " + column));
        builder.append(") (SELECT TIMESERIESID,UTCSTAMP,VERSIONCOUNT,RECORDTIME,?");
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
        builder.append(" WHERE TIMESERIESID = ");
        builder.addLong(timeSeries.getId());
        builder.add("UTCSTAMP", range, "AND");
        builder.append(")");
        return builder;
    }

    List<TimeSeriesEntry> getEntries(TimeSeriesImpl timeSeries, Range<Instant> interval) {
        try {
            return doGetEntries(timeSeries, interval);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    Optional<TimeSeriesEntry> getEntry(TimeSeriesImpl timeSeries, Instant when) {
        try {
            return Optional.ofNullable(doGetEntry(timeSeries, when));
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    private String rangeSql(TimeSeriesImpl timeSeries, Range<Instant> range) {
        StringBuilder builder = selectSql(timeSeries);
        builder.append(" AND UTCSTAMP >");
        if (range.hasLowerBound() && range.lowerBoundType() == BoundType.CLOSED) {
        	builder.append("=");
        }
        builder.append(" ? and UTCSTAMP <");
        if (range.hasUpperBound() && range.upperBoundType() == BoundType.CLOSED) {
        	builder.append("=");
        }
        builder.append(" ? order by UTCSTAMP");
        return builder.toString();
    }

    private String entrySql(TimeSeriesImpl timeSeries) {
        StringBuilder builder = selectSql(timeSeries);
        builder.append(" AND UTCSTAMP = ? ");
        return builder.toString();
    }

    private List<TimeSeriesEntry> doGetEntries(TimeSeriesImpl timeSeries, Range<Instant> interval) throws SQLException {
        List<TimeSeriesEntry> result = new ArrayList<>();
        try (Connection connection = getConnection(false)) {
            try (PreparedStatement statement = connection.prepareStatement(rangeSql(timeSeries,interval))) {
            	statement.setLong(ID_COLUMN_INDEX, timeSeries.getId());
                statement.setLong(FROM_COLUMN_INDEX, interval.hasLowerBound() ? interval.lowerEndpoint().toEpochMilli() : Long.MIN_VALUE);
                statement.setLong(TO_COLUMN_INDEX, interval.hasUpperBound() ? interval.upperEndpoint().toEpochMilli() : Long.MAX_VALUE);
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
            try (PreparedStatement statement = connection.prepareStatement(entrySql(timeSeries))) {
                statement.setLong(ID_COLUMN_INDEX, timeSeries.getId());
                statement.setLong(WHEN_COLUMN_INDEX, when.toEpochMilli());
                try (ResultSet rs = statement.executeQuery()) {
                    return rs.next() ? new TimeSeriesEntryImpl(timeSeries, rs) : null;
                }
            }
        }
    }


    StringBuilder selectSql(RecordSpecImpl recordSpec) {
        StringBuilder builder = new StringBuilder("select timeseriesid , utcstamp , versioncount , recordtime ");
        for (String column : recordSpec.columnNames()) {
            builder.append(",");
            builder.append(column);
        }
        builder.append(" FROM ");
        builder.append(getTableName());
        builder.append(" WHERE");
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

    public void persist() {
        dataModel.persist(this);
    }

    List<TimeSeriesEntry> getEntriesBefore(TimeSeriesImpl timeSeries, Instant when, int entryCount, boolean includeBoundary) {
        try {
            return doGetEntriesBefore(timeSeries, when, entryCount, includeBoundary);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    private List<TimeSeriesEntry> doGetEntriesBefore(TimeSeriesImpl timeSeries, Instant when, int entryCount, boolean includeBoundary) throws SQLException {
        List<TimeSeriesEntry> result = new ArrayList<>();
        try (Connection connection = getConnection(false)) {
            try (PreparedStatement statement = connection.prepareStatement(entriesBeforeSql(timeSeries, includeBoundary))) {
                statement.setLong(ID_COLUMN_INDEX, timeSeries.getId());
                statement.setLong(WHEN_COLUMN_INDEX, when.toEpochMilli());
                statement.setInt(3, entryCount);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        result.add(new TimeSeriesEntryImpl(timeSeries, rs));
                    }
                }
            }
        }
        return result;
    }

    private String entriesBeforeSql(TimeSeriesImpl timeSeries, boolean includeBoundary) {
        StringBuilder base = selectSql(timeSeries);
        base.append(" AND UTCSTAMP ");
        base.append(includeBoundary ? "<= " : "<");
        base.append(" ? ORDER BY UTCSTAMP DESC ");
        StringBuilder builder = new StringBuilder("SELECT * from (");
        builder.append(base.toString());
        builder.append(") where ROWNUM <= ? ");
        return builder.toString();
    }

    void removeEntries(TimeSeriesImpl timeSeries, Range<Instant> range) {
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
    public void purge(Instant instant, Logger logger) {
    	if (isPartitioned()) {
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
 }
