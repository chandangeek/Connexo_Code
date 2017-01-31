/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.StorerStats;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.IntStream;

@LiteralSql
public class TimeSeriesDataStorerImpl implements TimeSeriesDataStorer {
    private final UpdateBehaviour updateBehaviour;
    private final Map<RecordSpecInVault, SlaveTimeSeriesDataStorer> storerMap = new HashMap<>();
    private final StorerStatsImpl stats = new StorerStatsImpl();
    private final DataModel dataModel;
    private final Clock clock;
    private final Thesaurus thesaurus;

    private TimeSeriesDataStorerImpl(DataModel dataModel, Clock clock, Thesaurus thesaurus, UpdateBehaviour updateBehaviour) {
        this.dataModel = dataModel;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.updateBehaviour = updateBehaviour;
    }

    static TimeSeriesDataStorer createOverrulingStorer(DataModel dataModel, Clock clock, Thesaurus thesaurus) {
        return new TimeSeriesDataStorerImpl(dataModel, clock, thesaurus, Behaviours.OVERRULE);
    }

    static TimeSeriesDataStorer createUpdatingStorer(DataModel dataModel, Clock clock, Thesaurus thesaurus) {
        return new TimeSeriesDataStorerImpl(dataModel, clock, thesaurus, Behaviours.UPDATE);
    }

    static TimeSeriesDataStorer createNonOverrulingStorer(DataModel dataModel, Clock clock, Thesaurus thesaurus) {
        return new TimeSeriesDataStorerImpl(dataModel, clock, thesaurus, Behaviours.INSERT_ONLY);
    }

    private interface UpdateBehaviour {
        boolean overrules();

        Object doNotUpdateMarker();
    }

    private enum Behaviours implements UpdateBehaviour {
        INSERT_ONLY {
            @Override
            public boolean overrules() {
                return false;
            }

            @Override
            public Object doNotUpdateMarker() {
                return null;
            }
        },

        OVERRULE {
            @Override
            public boolean overrules() {
                return true;
            }

            @Override
            public Object doNotUpdateMarker() {
                return null;
            }
        },

        UPDATE {
            @Override
            public boolean overrules() {
                return true;
            }

            @Override
            public Object doNotUpdateMarker() {
                return DoNotUpdateMarker.INSTANCE;
            }
        };
    }

    @Override
    public boolean overrules() {
        return updateBehaviour.overrules();
    }

    @Override
    public void add(TimeSeries timeSeries, Instant timeStamp, Object... values) {
        if (!timeSeries.isValidInstant(timeStamp)) {
            throw new MeasurementTimeIsNotValidException(this.thesaurus);
        }
        if (values.length != timeSeries.getRecordSpec().getFieldSpecs().size()) {
            throw new IllegalArgumentException();
        }
        TimeSeriesImpl timeSeriesImpl = (TimeSeriesImpl) timeSeries;
        TimeSeriesEntryImpl entry = new TimeSeriesEntryImpl(timeSeriesImpl, timeStamp, values);
        RecordSpecInVault recordSpecInVault = new RecordSpecInVault(timeSeriesImpl);
        SlaveTimeSeriesDataStorer slaveStorer = storerMap.get(recordSpecInVault);
        if (slaveStorer == null) {
            slaveStorer = new SlaveTimeSeriesDataStorer(dataModel, clock, entry);
            storerMap.put(recordSpecInVault, slaveStorer);
        } else {
            slaveStorer.add(entry);
        }
        stats.add(entry);
    }

    @Override
    public StorerStats execute() {
        try {
            stats.start();
            doExecute();
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        } finally {
            stats.stop();
        }
        return stats;
    }

    private void doExecute() throws SQLException {
        // lock all timeseries objects, but make sure to do this in id order to avoid deadlocks between concurrent storers
        storerMap.values().stream()
                .flatMap(slaveStorer -> slaveStorer.getAllTimeSeries().stream())
                .sorted(Comparator.comparing(TimeSeriesImpl::getId))
                .forEach(TimeSeriesImpl::lock);
        for (SlaveTimeSeriesDataStorer storer : storerMap.values()) {
            storer.execute(stats, updateBehaviour);
        }
    }

    @Override
    public boolean processed(TimeSeries timeSeries, Instant instant) {
        RecordSpecInVault recordSpecInVault = new RecordSpecInVault((TimeSeriesImpl) timeSeries);
        SlaveTimeSeriesDataStorer storer = storerMap.get(recordSpecInVault);
        if (storer == null) {
            return false;
        } else {
            return storer.processed(timeSeries, instant, updateBehaviour);
        }
    }

    @Override
    public Object doNotUpdateMarker() {
        return updateBehaviour.doNotUpdateMarker();
    }

    private enum DoNotUpdateMarker {
        INSTANCE;
    }

    private static final class RecordSpecInVault {
        private final Vault vault;
        private final RecordSpec recordSpec;

        RecordSpecInVault(TimeSeriesImpl timeSeries) {
            this.vault = timeSeries.getVault();
            this.recordSpec = timeSeries.getRecordSpec();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof RecordSpecInVault)) {
                return false;
            }
            RecordSpecInVault o = (RecordSpecInVault) other;
            return this.vault.equals(o.vault) && this.recordSpec.equals(o.recordSpec);
        }

        @Override
        public int hashCode() {
            return Objects.hash(vault, recordSpec);
        }
    }

    private static class SlaveTimeSeriesDataStorer {
        private final Map<Long, SingleTimeSeriesStorer> storerMap = new HashMap<>();
        private final VaultImpl vault;
        private final RecordSpecImpl recordSpec;
        private final DataModel dataModel;
        private final Clock clock;
        private final ThreadPrincipalService threadPrincipalService;

        SlaveTimeSeriesDataStorer(DataModel dataModel, Clock clock, TimeSeriesEntryImpl entry) {
            this.dataModel = dataModel;
            this.clock = clock;
            SingleTimeSeriesStorer storer = new SingleTimeSeriesStorer(entry);
            storerMap.put(entry.getTimeSeries().getId(), storer);
            vault = (VaultImpl) entry.getTimeSeries().getVault();
            recordSpec = entry.getTimeSeries().getRecordSpec();
            threadPrincipalService = dataModel.getInstance(ThreadPrincipalService.class);
        }

        List<TimeSeriesImpl> getAllTimeSeries() {
            List<TimeSeriesImpl> result = new ArrayList<>();
            for (SingleTimeSeriesStorer each : storerMap.values()) {
                result.add(each.getTimeSeries());
            }
            return result;
        }

        void add(TimeSeriesEntryImpl entry) {
            Long key = entry.getTimeSeries().getId();
            SingleTimeSeriesStorer storer = storerMap.get(key);
            if (storer == null) {
                storer = new SingleTimeSeriesStorer(entry);
                storerMap.put(key, storer);
            } else {
                storer.add(entry);
            }
        }

        SqlBuilder selectSql(Collection<SingleTimeSeriesStorer> storers) {
            SqlBuilder builder = vault.selectSql(recordSpec);
            String separator = " ";
            for (SingleTimeSeriesStorer storer : storers) {
                builder.append(separator);
                storer.appendWhereClause(builder);
                separator = " or ";
            }
            builder.append(" ORDER BY TIMESERIESID, UTCSTAMP ");
            return builder;
        }

        String insertSql() {
            return vault.insertSql(recordSpec);
        }

        String updateSql() {
            return vault.updateSql(recordSpec);
        }

        String journalSql() {
            return vault.journalSql(recordSpec);
        }

        void setOldEntries(Connection connection) throws SQLException {
            Collection<SingleTimeSeriesStorer> storers = storerMap.values();
            try (PreparedStatement statement = this.selectSql(storers).prepare(connection)) {
                try (ResultSet rs = statement.executeQuery()) {
                    SingleTimeSeriesStorer storer = null;
                    while (rs.next()) {
                        long key = rs.getLong(1);
                        if (storer == null || storer.getTimeSeries().getId() != key) {
                            storer = storerMap.get(key);
                        }
                        storer.add(rs);
                    }
                }
            }
        }

        void addInserts(Connection connection, long now) throws SQLException {
            try (PreparedStatement statement = connection.prepareStatement(insertSql())) {
                for (SingleTimeSeriesStorer storer : storerMap.values()) {
                    storer.addInserts(statement, now);
                }
                statement.executeBatch();
            }
        }

        void addUnJournaledUpdates(Connection connection, long now) throws SQLException {
            try (PreparedStatement statement = connection.prepareStatement(updateSql())) {
                for (SingleTimeSeriesStorer storer : storerMap.values()) {
                    storer.addUpdates(statement, now, threadPrincipalService.getPrincipal());
                }
                statement.executeBatch();
            }
        }

        void addJournaledUpdates(Connection connection, long now, Principal principal) throws SQLException {
            try (PreparedStatement updateStatement = connection.prepareStatement(updateSql())) {
                try (PreparedStatement journalStatement = connection.prepareStatement(journalSql())) {
                    for (SingleTimeSeriesStorer storer : storerMap.values()) {
                        storer.addUpdates(updateStatement, journalStatement, now, principal);
                    }
                    journalStatement.executeBatch();
                    updateStatement.executeBatch();
                }
            }
        }

        void execute(StorerStatsImpl stats, UpdateBehaviour updateBehaviour) throws SQLException {
            try (Connection connection = dataModel.getConnection(true)) {
                setOldEntries(connection);
                long now = clock.millis();
                addInserts(connection, now);
                if (updateBehaviour.overrules()) {
                    if (vault.hasJournal()) {
                        addJournaledUpdates(connection, now, threadPrincipalService.getPrincipal());
                    } else {
                        addUnJournaledUpdates(connection, now);
                    }
                }
            }
            for (SingleTimeSeriesStorer storer : storerMap.values()) {
                storer.updateTimeSeries();
                stats.addCount(storer.insertCount, storer.updateCount);
            }
        }

        boolean processed(TimeSeries timeSeries, Instant instant, UpdateBehaviour updateBehaviour) {
            SingleTimeSeriesStorer storer = storerMap.get(timeSeries.getId());
            if (storer == null) {
                return false;
            } else {
                return storer.processed(instant, updateBehaviour);
            }
        }
    }

    private static class SingleTimeSeriesStorer {
        private final SortedMap<Instant, TimeSeriesEntryImpl> newEntries = new TreeMap<>();
        private final SortedMap<Instant, TimeSeriesEntryImpl> oldEntries = new TreeMap<>();
        private Instant minDate;
        private Instant maxDate;
        private int insertCount = 0;
        private int updateCount = 0;
        private TimeSeriesImpl timeSeries;

        SingleTimeSeriesStorer(TimeSeriesEntryImpl entry) {
            newEntries.put(entry.getTimeStamp(), entry);
            timeSeries = entry.getTimeSeries();
        }

        void add(TimeSeriesEntryImpl entry) {
            Instant instant = entry.getTimeStamp();
            if (newEntries.containsKey(instant)) {
                throw new IllegalArgumentException("Duplicate date in timeSeries " + entry.getTimeSeries());
            }
            newEntries.put(instant, entry);
        }

        TimeSeriesImpl getTimeSeries() {
            return timeSeries;
        }

        void appendWhereClause(SqlBuilder builder) {
            builder.append(" (TIMESERIESID =");
            builder.addLong(this.getTimeSeries().getId());
            builder.append("AND UTCSTAMP between");
            builder.addLong(this.newEntries.firstKey().toEpochMilli());
            builder.append("AND");
            builder.addLong(this.newEntries.lastKey().toEpochMilli());
            builder.append(")");
        }

        void add(ResultSet rs) throws SQLException {
            TimeSeriesEntryImpl oldEntry = new TimeSeriesEntryImpl(getTimeSeries(), rs);
            oldEntries.put(oldEntry.getTimeStamp(), oldEntry);
        }

        private boolean isInsert(TimeSeriesEntryImpl entry) {
            return !oldEntries.containsKey(entry.getTimeStamp());
        }

        private boolean isUpdate(TimeSeriesEntryImpl entry) {
            TimeSeriesEntryImpl oldEntry = oldEntries.get(entry.getTimeStamp());
            return oldEntry != null && !entry.matches(oldEntry);
        }

        void addInserts(PreparedStatement statement, long now) throws SQLException {
            for (TimeSeriesEntryImpl entry : newEntries.values()) {
                Object[] values = entry.getValues();
                if (isInsert(entry)) {
                    IntStream.range(0, values.length)
                            .filter(i -> values[i] == DoNotUpdateMarker.INSTANCE)
                            .forEach(i -> entry.set(i, null));
                    entry.insert(statement, now);
                    insertCount++;
                    statement.addBatch();
                }
                if (minDate == null || entry.getTimeStamp().isBefore(minDate)) {
                    minDate = entry.getTimeStamp();
                }
                if (maxDate == null || entry.getTimeStamp().isAfter(maxDate)) {
                    maxDate = entry.getTimeStamp();
                }
            }
        }

        void addUpdates(PreparedStatement statement, long now, Principal principal) throws SQLException {
            addUpdates(statement, null, now, principal);
        }

        void addUpdates(PreparedStatement updateStatement, PreparedStatement journalStatement, long now, Principal principal) throws SQLException {
            for (TimeSeriesEntryImpl entry : newEntries.values()) {
                if (isUpdate(entry)) {
                    Object[] values = entry.getValues();
                    IntStream.range(0, values.length)
                            .filter(i -> values[i] == DoNotUpdateMarker.INSTANCE)
                            .forEach(i -> {
                                TimeSeriesEntryImpl oldEntry = oldEntries.get(entry.getTimeStamp());
                                entry.set(i, oldEntry == null ? null : oldEntry.getValues()[i]);
                            });
                    if (journalStatement != null) {
                        entry.journal(journalStatement, now, principal);
                        journalStatement.addBatch();
                    }
                    entry.update(updateStatement, now);
                    updateCount++;
                    updateStatement.addBatch();
                }
            }
        }

        void updateTimeSeries() {
            if (insertCount + updateCount > 0) {
                getTimeSeries().updateRange(minDate, maxDate);
            }
        }

        boolean processed(Instant instant, UpdateBehaviour updateBehaviour) {
            TimeSeriesEntryImpl entry = newEntries.get(instant);
            if (entry == null) {
                return false;
            }
            TimeSeriesEntryImpl oldEntry = oldEntries.get(instant);
            if (oldEntry == null) {
                return true;
            }
            return updateBehaviour.overrules() && !entry.matches(oldEntry);
        }
    }

}
