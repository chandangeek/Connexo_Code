package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.StorerStats;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataWriter;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;

import java.math.BigDecimal;
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

@LiteralSql
public class TimeSeriesDataWriterImpl implements TimeSeriesDataWriter {
    private final boolean overrules;
    private final Map<RecordSpecInVault, SlaveTimeSeriesDataStorer> storerMap = new HashMap<>();
    private final StorerStatsImpl stats = new StorerStatsImpl();
    private final DataModel dataModel;
    private final Clock clock;
    private final Thesaurus thesaurus;

    public TimeSeriesDataWriterImpl(DataModel dataModel, Clock clock, Thesaurus thesaurus, boolean overrules) {
        this.dataModel = dataModel;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.overrules = overrules;
    }

    @Override
    public boolean overrules() {
        return overrules;
    }

    @Override
    public void add(TimeSeries timeSeries, Instant timeStamp, Object... values) {
        if (!timeSeries.isValidInstant(timeStamp)) {
            throw new MeasurementTimeIsNotValidExcecption(this.thesaurus);
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
        	.forEach(timeSeries -> timeSeries.lock());                
        for (SlaveTimeSeriesDataStorer storer : storerMap.values()) {
            storer.execute(stats, overrules());
        }
    }

    @Override
    public boolean processed(TimeSeries timeSeries, Instant instant) {
        RecordSpecInVault recordSpecInVault = new RecordSpecInVault((TimeSeriesImpl) timeSeries);
        SlaveTimeSeriesDataStorer storer = storerMap.get(recordSpecInVault);
        if (storer == null) {
            return false;
        } else {
            return storer.processed(timeSeries, instant, overrules());
        }
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

        SlaveTimeSeriesDataStorer(DataModel dataModel, Clock clock, TimeSeriesEntryImpl entry) {
            this.dataModel = dataModel;
            this.clock = clock;
            SingleTimeSeriesStorer storer = new SingleTimeSeriesStorer(entry);
            storerMap.put(entry.getTimeSeries().getId(), storer);
            vault = (VaultImpl) entry.getTimeSeries().getVault();
            recordSpec = entry.getTimeSeries().getRecordSpec();

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

        String selectSql(Collection<SingleTimeSeriesStorer> storers) {
            StringBuilder builder = vault.selectSql(recordSpec);
            String separator = " ";
            for (SingleTimeSeriesStorer storer : storers) {
                builder.append(separator);
                storer.appendWhereClause(builder);
                separator = " or ";
            }
            builder.append(" ORDER BY TIMESERIESID , UTCSTAMP ");
            return builder.toString();
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
            try (PreparedStatement statement = connection.prepareStatement(selectSql(storers))) {
                int offset = 1;
                for (SingleTimeSeriesStorer storer : storers) {
                    offset = storer.bindWhere(statement, offset);
                }
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
                storers.forEach(SingleTimeSeriesStorer::prepare);
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
                    storer.addUpdates(statement, now);
                }
                statement.executeBatch();
            }
        }

        void addJournaledUpdates(Connection connection, long now) throws SQLException {
            try (PreparedStatement updateStatement = connection.prepareStatement(updateSql())) {
                try (PreparedStatement journalStatement = connection.prepareStatement(journalSql())) {
                    for (SingleTimeSeriesStorer storer : storerMap.values()) {
                        storer.addUpdates(updateStatement, journalStatement, now);
                    }
                    journalStatement.executeBatch();
                    updateStatement.executeBatch();
                }
            }
        }

        void execute(StorerStatsImpl stats, boolean overrules) throws SQLException {
            try (Connection connection = dataModel.getConnection(true)) {
                setOldEntries(connection);
                long now = clock.millis();
                addInserts(connection, now);
                if (overrules) {
                    if (vault.hasJournal()) {
                        addJournaledUpdates(connection, now);
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

        boolean processed(TimeSeries timeSeries, Instant instant, boolean overrules) {
            SingleTimeSeriesStorer storer = storerMap.get(timeSeries.getId());
            if (storer == null) {
                return false;
            } else {
                return storer.processed(instant, overrules);
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
            timeSeries = (TimeSeriesImpl) entry.getTimeSeries();
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

        void appendWhereClause(StringBuilder builder) {
            builder.append(" (TIMESERIESID = ? AND UTCSTAMP between ? and ?)");
        }

        int bindWhere(PreparedStatement statement, int offset) throws SQLException {
            Instant first = newEntries.firstKey();
            Instant last = newEntries.lastKey();
            if (timeSeries.isRegular() && timeSeries.getRecordSpec().derivedFieldCount() > 0) {
                first = timeSeries.next(first, -1);
                last = timeSeries.next(last, 1);
            }
            statement.setLong(offset++, getTimeSeries().getId());
            statement.setLong(offset++, first.toEpochMilli());
            statement.setLong(offset++, last.toEpochMilli());
            return offset;
        }

        void add(ResultSet rs) throws SQLException {
            TimeSeriesEntryImpl oldEntry = new TimeSeriesEntryImpl(getTimeSeries(), rs);
            oldEntries.put(oldEntry.getTimeStamp(), oldEntry);
        }

        void prepare() {
            if (!timeSeries.isRegular() || timeSeries.getRecordSpec().derivedFieldCount() == 0) {
                return;
            }
            newEntries.values().stream().reduce(
                    null,
                    (guess, current) -> {
                        TimeSeriesEntryImpl previous = previous(current, guess);
                        if (previous != null) {
                            updateFromPrevious(current, previous);
                        }
                        return current;
                    });
            for (TimeSeriesEntryImpl entry : oldEntries.values()) {
                if (!newEntries.containsKey(entry.getTimeStamp())) {
                    TimeSeriesEntryImpl previous = previous(entry, null);
                    if (previous != null) {
                        TimeSeriesEntryImpl current = entry.copy();
                        if (updateFromPrevious(current, previous)) {
                            newEntries.put(current.getTimeStamp(), current);
                        }
                    }
                }
            }
        }

        private boolean updateFromPrevious(TimeSeriesEntryImpl current, TimeSeriesEntryImpl previous) {
            boolean result = false;
            for (int i = 0; i < timeSeries.getRecordSpec().getFieldSpecs().size(); i++) {
                FieldSpec fieldSpec = timeSeries.getRecordSpec().getFieldSpecs().get(i);
                if (fieldSpec.isDerived() && current.getBigDecimal(i) == null) {
                    BigDecimal currentValue = current.getBigDecimal(i + 1);
                    if (currentValue != null) {
                        BigDecimal previousValue = previous.getBigDecimal(i + 1);
                        if (previousValue != null) {
                            current.set(i, currentValue.subtract(previousValue));
                            result = true;
                        }
                    }
                }
            }
            return result;
        }

        private TimeSeriesEntryImpl previous(TimeSeriesEntryImpl current, TimeSeriesEntryImpl guess) {
            Instant when = timeSeries.next(current.getTimeStamp(), -1);
            if (guess != null && guess.getTimeStamp().equals(when)) {
                return guess;
            }
            TimeSeriesEntryImpl result = newEntries.get(when);
            if (result == null) {
                result = oldEntries.get(when);
            }
            return result;
        }

        boolean isInsert(TimeSeriesEntryImpl entry) {
            return !oldEntries.containsKey(entry.getTimeStamp());
        }

        boolean isUpdate(TimeSeriesEntryImpl entry) {
            TimeSeriesEntryImpl oldEntry = oldEntries.get(entry.getTimeStamp());
            if (oldEntry != null && timeSeries.getRecordSpec().derivedFieldCount() > 0) {
                for (int i = 0; i < timeSeries.getRecordSpec().getFieldSpecs().size(); i++) {
                    if (entry.getValues()[i] == null) {
                        FieldSpec fieldSpec = timeSeries.getRecordSpec().getFieldSpecs().get(i);
                        if (fieldSpec.isDerived() && entry.getBigDecimal(i + 1) == null) {
                            entry.set(i + 1, oldEntry.getBigDecimal(i + 1));
                        }
                    }
                }
            }
            return oldEntry != null && !entry.matches(oldEntry);
        }

        void addInserts(PreparedStatement statement, long now) throws SQLException {
            for (TimeSeriesEntryImpl entry : newEntries.values()) {
                if (isInsert(entry)) {
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

        void addUpdates(PreparedStatement statement, long now) throws SQLException {
            addUpdates(statement, null, now);
        }

        void addUpdates(PreparedStatement updateStatement, PreparedStatement journalStatement, long now) throws SQLException {
            for (TimeSeriesEntryImpl entry : newEntries.values()) {
                if (isUpdate(entry)) {
                    if (journalStatement != null) {
                        entry.journal(journalStatement, now);
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
                ((TimeSeriesImpl) getTimeSeries()).updateRange(minDate, maxDate);
            }
        }

        boolean processed(Instant instant, boolean overrules) {
            TimeSeriesEntryImpl entry = newEntries.get(instant);
            if (entry == null) {
                return false;
            }
            TimeSeriesEntryImpl oldEntry = oldEntries.get(instant);
            if (oldEntry == null) {
                return true;
            }
            return overrules && !entry.matches(oldEntry);
        }
    }

}	
