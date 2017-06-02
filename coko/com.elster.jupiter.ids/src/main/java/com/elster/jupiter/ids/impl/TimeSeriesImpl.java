/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.StorerStats;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.ids.TimeSeriesJournalEntry;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OptimisticLockException;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.sql.SqlFragment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static com.elster.jupiter.ids.impl.IntervalLengthUnit.MINUTE;
import static com.elster.jupiter.ids.impl.IntervalLengthUnit.MONTH;

public final class TimeSeriesImpl implements TimeSeries {

    // persistent fields
    private long id;
    private Instant firstTime;
    private Instant lastTime;
    private Instant lockTime;
    private String timeZoneName;
    private boolean regular;
    private int intervalLength;
    private IntervalLengthUnit intervalLengthUnit;
    private int offset;
    private long version;
    private Instant createTime;
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    // association
    private Reference<IVault> vault = ValueReference.absent();
    private Reference<RecordSpec> recordSpec = ValueReference.absent();

    // cached values
    private ZoneId zoneId;

    private final DataModel dataModel;
    private final IdsService idsService;

    @Inject
    TimeSeriesImpl(DataModel dataModel, IdsService idsService) {
        this.dataModel = dataModel;
        this.idsService = idsService;
    }

    TimeSeriesImpl init(IVault vault, RecordSpec recordSpec, ZoneId zoneId) {
        this.vault.set(Objects.requireNonNull(vault));
        this.recordSpec.set(Objects.requireNonNull(recordSpec));
        this.zoneId = zoneId;
        this.timeZoneName = zoneId.getId();
        this.regular = false;
        return this;
    }

    TimeSeriesImpl init(IVault vault, RecordSpec recordSpec, ZoneId zoneId, TemporalAmount interval, int offsetInHours) {
        init(vault, recordSpec, zoneId);
        this.regular = true;
        setInterval(interval);
        this.offset = offsetInHours;
        return this;
    }

    private void setInterval(TemporalAmount interval) {
        List<TemporalUnit> units = interval.getUnits().stream().filter(unit -> interval.get(unit) > 0).collect(Collectors.toList());
        if (units.size() > 1) {
            throw new IllegalArgumentException("Composite intervals are not supported");
        }
        if (units.isEmpty()) {
            throw new IllegalArgumentException("Empty interval not supported");
        }
        TemporalUnit unit = units.get(0);
        if (unit.equals(ChronoUnit.SECONDS)) {
            long seconds = interval.get(unit);
            if ((seconds % 60) != 0) {
                throw new IllegalArgumentException("Duration not in whole minutes");
            }
            long minutes = seconds / 60;
            if (minutes > 60 && (minutes % 60) != 0) {
                throw new IllegalArgumentException("Minute interval lengths that are multiple by one hour are supported.");
            }
            if (minutes < 60 && (60 % minutes) != 0) {
                throw new IllegalArgumentException("Minute interval lengths that are divisors of one hour are supported.");
            }
            this.intervalLength = (int) minutes;
            this.intervalLengthUnit = IntervalLengthUnit.MINUTE;
        }
        if (unit.equals(ChronoUnit.DAYS)) {
            long days = interval.get(unit);
            if (days != 1) {
                throw new IllegalArgumentException("For Day only 1 as length is supported.");
            }
            this.intervalLength = (int) days;
            this.intervalLengthUnit = IntervalLengthUnit.DAY;
        }
        if (unit.equals(ChronoUnit.MONTHS)) {
            long months = interval.get(unit);
            if (months != 1) {
                throw new IllegalArgumentException("For Month only 1 as length is supported.");
            }
            this.intervalLength = (int) months;
            this.intervalLengthUnit = IntervalLengthUnit.MONTH;
        }
        if (unit.equals(ChronoUnit.YEARS)) {
            long years = interval.get(unit);
            if (years != 1) {
                throw new IllegalArgumentException("For Year only 1 as length is supported.");
            }
            this.intervalLength = (int) years;
            this.intervalLengthUnit = IntervalLengthUnit.YEAR;
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Instant getFirstDateTime() {
        return firstTime;
    }

    @Override
    public Instant getLastDateTime() {
        return lastTime;
    }

    @Override
    public Instant getLockDateTime() {
        return lockTime;
    }

    @Override
    public ZoneId getZoneId() {
        if (zoneId == null) {
            // TODO may need to optimized as TimeZone.getTimeZone is probably the slowest method in the JDK
            zoneId = ZoneId.of(timeZoneName);
        }
        return zoneId;
    }

    @Override
    public boolean isRegular() {
        return regular;
    }

    @Override
    public TemporalAmount interval() {
        return intervalLengthUnit.amount(intervalLength);
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public IVault getVault() {
        return vault.get();
    }

    @Override
    public RecordSpecImpl getRecordSpec() {
        return (RecordSpecImpl) recordSpec.get();
    }

    @Override
    public String toString() {
        return "TimeSeries " + id + " (version: " + version + " created: " + getCreateDate() + " modified: " + getModDate() + ")";
    }

    public Instant getCreateDate() {
        return createTime;
    }

    public Instant getModDate() {
        return modTime;
    }

    void persist() {
        dataModel.persist(this);
    }

    @Override
    public boolean add(Instant instant, boolean overrule, Object... values) {
        TimeSeriesDataStorer storer = overrule ? idsService.createOverrulingStorer() : idsService.createNonOverrulingStorer();
        storer.add(this, instant, values);
        StorerStats stats = storer.execute();
        return stats.getInsertCount() > 0 || stats.getUpdateCount() > 0;
    }

    void updateRange(Instant minDate, Instant maxDate) {
        List<String> updateAspects = new ArrayList<>();
        if (minDate != null) {
            if (this.firstTime == null || firstTime.isAfter(minDate)) {
                firstTime = minDate;
                updateAspects.add("firstTime");
            }
        }
        if (maxDate != null) {
            if (this.lastTime == null || lastTime.isBefore(maxDate)) {
                lastTime = maxDate;
                updateAspects.add("lastTime");
            }
        }
        if (!updateAspects.isEmpty()) {
            dataModel.update(this, updateAspects.toArray(new String[updateAspects.size()]));
        }
    }

    Calendar getStartCalendar(Instant instant) {
        Calendar result = Calendar.getInstance(TimeZone.getTimeZone(getZoneId()));
        result.setTimeInMillis(instant.toEpochMilli());
        result.add(intervalLengthUnit.getCalendarCode(), -intervalLength);
        if (getOffset() != 0) {
            // use set instead of add, because calendar behavior is unexpected for adding hours to midnight on a DST transition day.
            result.set(Calendar.HOUR_OF_DAY, offset);
        }
        return result;
    }


    @Override
    public boolean isValidInstant(Instant instant) {
        return getVault().isValidInstant(instant) && isValid(instant);
    }

    private boolean isValid(Instant instant) {
        if (lockTime != null && !instant.isAfter(lockTime)) {
            return false;
        }
        if (!isRegular()) {
            return true;
        }
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, getZoneId());
        if (intervalLengthUnit == MINUTE) {
            return dateTime.getMinute() % intervalLength == 0
                    && dateTime.getSecond() == 0
                    && dateTime.getNano() == 0;
        }
        if (!validTimeOfDay(dateTime)) {
            return false;
        }
        return !MONTH.equals(intervalLengthUnit) || dateTime.getDayOfMonth() == 1;
    }

    Instant validInstantOnOrAfter(Instant instant) {
        if (isValid(instant)) {
            return instant;
        }
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, getZoneId());
        if (intervalLengthUnit == MINUTE) {
            dateTime = dateTime.truncatedTo(ChronoUnit.MINUTES);
            if (dateTime.getMinute() % intervalLength != 0) {
                dateTime = dateTime.withMinute((dateTime.getMinute() / intervalLength) * intervalLength);
            }
            return next(dateTime.toInstant(), 1);
        } else {
            if (dateTime.getHour() < getOffset()) {
                dateTime = dateTime.minusDays(1);
            }
            dateTime = dateTime.withHour(getOffset()).truncatedTo(ChronoUnit.HOURS);
            switch (intervalLengthUnit) {
                case DAY: {
                    return next(dateTime.toInstant(), 1);
                }
                case MONTH: {
                    dateTime = dateTime.withDayOfMonth(1);
                    return next(dateTime.toInstant(), 1);
                }
                case YEAR: {
                    dateTime = dateTime.withDayOfYear(1);
                    return next(dateTime.toInstant(), 1);
                }
                default: {
                    // Can only be minute but that is already taken care of
                    throw new IllegalArgumentException("Minute case should already have been taken care of");
                }
            }
        }
    }

    private boolean validTimeOfDay(ZonedDateTime dateTime) {
        return dateTime.getMinute() == 0 && dateTime.getSecond() == 0 && dateTime.getNano() == 0 && dateTime.getHour() == getOffset();
    }

    @Override
    public List<TimeSeriesEntry> getEntries(Range<Instant> interval) {
        return getVault().getEntries(this, interval);
    }

    @Override
    public List<TimeSeriesJournalEntry> getJournalEntries(Range<Instant> interval) {
        return getVault().getJournalEntries(this, interval);
    }

    @Override
    public SqlFragment getRawValuesSql(Range<Instant> interval, Pair<String, String>... fieldSpecAndAliasNames) {
        return this.getVault().getRawValuesSql(this, interval, fieldSpecAndAliasNames);
    }

    @Override
    public List<TimeSeriesEntry> getEntriesUpdatedSince(Range<Instant> interval, Instant since) {
        return getVault().getEntriesUpdatedSince(this, interval, since);
    }

    @Override
    public Optional<TimeSeriesEntry> getEntry(Instant when) {
        return getVault().getEntry(this, when);
    }

    void lock() {
        TimeSeriesImpl latest = dataModel.mapper(TimeSeriesImpl.class).lock(getId());
        if (latest.version != this.version) {
            throw new OptimisticLockException();
        }
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof TimeSeriesImpl && this.id == ((TimeSeriesImpl) other).id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    Instant next(Instant instant, int numberOfEntries) {
        if (!isRegular()) {
            throw new UnsupportedOperationException("Unsupported operation on non-regular timeseries");
        }
        if (!isValid(instant)) {
            throw new IllegalArgumentException("Interval timestamp \'" + instant + "\' is not valid");
        }
        if (intervalLengthUnit == MINUTE) {
            return instant.plusSeconds(numberOfEntries * intervalLength * 60);
        }
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, getZoneId());
        if (numberOfEntries > 0) {
            dateTime = dateTime.plus(this.intervalLengthUnit.amount(numberOfEntries * intervalLength));
        } else {
            dateTime = dateTime.minus(this.intervalLengthUnit.amount(-numberOfEntries * intervalLength));
        }
        return dateTime.toInstant();
    }

    @Override
    public List<TimeSeriesEntry> getEntriesBefore(Instant when, int entryCount) {
        return getVault().getEntriesBefore(this, when, entryCount, false);
    }

    @Override
    public List<TimeSeriesEntry> getEntriesOnOrBefore(Instant when, int entryCount) {
        return getVault().getEntriesBefore(this, when, entryCount, true);
    }

    @Override
    public void removeEntries(Range<Instant> range) {
        if (lockTime != null) {
            Range<Instant> allowed = Range.greaterThan(lockTime);
            if (allowed.isConnected(range)) {
                range = range.intersection(allowed);
            } else {
                return;
            }
        }
        getVault().removeEntries(this, range);
        doUpdateLastDateTime(range);
    }

    private void doUpdateLastDateTime(Range<Instant> range) {
        if (range.contains(lastTime)) {
            List<TimeSeriesEntry> entriesBefore = getEntriesOnOrBefore(lastTime, 1); // Search for first entry before or on lastTime boundary
            if (!entriesBefore.isEmpty()) {
                lastTime = entriesBefore.get(0).getTimeStamp();
            } else {
                lastTime = null;
            }
            dataModel.update(this, "lastTime");
        }
    }

    @Override
    public List<Instant> toList(Range<Instant> range) {
        if (!isRegular()) {
            return Collections.emptyList();
        }
        if (!range.hasLowerBound() || !range.hasUpperBound()) {
            throw new IllegalArgumentException("Range must be finite");
        }
        ImmutableList.Builder<Instant> builder = ImmutableList.builder();
        Instant start = validInstantOnOrAfter(range.lowerEndpoint());
        if (!range.contains(start)) {
            start = next(start, 1);
        }
        while (range.contains(start)) {
            builder.add(start);
            start = next(start, 1);
        }
        return builder.build();
    }

    @Override
    public Instant getNextDateTime(Instant instant) {
        return next(instant, 1);
    }

    @Override
    public Instant getPreviousDateTime(Instant instant) {
        if (isRegular() && !isValid(instant)) {
            return next(validInstantOnOrAfter(instant), -1);
        }
        return next(instant, -1);
    }

}
