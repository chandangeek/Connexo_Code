package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DoesNotExistException;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import javax.inject.Inject;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public final class ChannelImpl implements ChannelContract {

    private static final int REGULARVAULTID = 1;
    private static final int IRREGULARVAULTID = 2;

    // persistent fields
    private long id;
    private long version;
    @SuppressWarnings("unused")
    private UtcInstant createTime;
    @SuppressWarnings("unused")
    private UtcInstant modTime;
    @SuppressWarnings("unused")
    private String userName;


    // associations
    private Reference<MeterActivation> meterActivation = ValueReference.absent();
    private Reference<TimeSeries> timeSeries = ValueReference.absent();
    private Reference<ReadingTypeImpl> mainReadingType = ValueReference.absent();
    private Reference<ReadingTypeImpl> bulkQuantityReadingType = ValueReference.absent();
    private List<ReadingTypeInChannel> readingTypeInChannels = new ArrayList<>();

    private final IdsService idsService;
    private final MeteringService meteringService;
    private final Clock clock;
    private final DataModel dataModel;

    @Inject
    ChannelImpl(DataModel dataModel, IdsService idsService, MeteringService meteringService, Clock clock) {
        this.dataModel = dataModel;
        this.idsService = idsService;
        this.meteringService = meteringService;
        this.clock = clock;
    }

    ChannelImpl init(MeterActivation meterActivation, List<ReadingTypeImpl> readingTypes) {
        this.meterActivation.set(meterActivation);
        this.mainReadingType.set(readingTypes.get(0));
        int index = 1;
        if (readingTypes.size() > 1) {
            if (mainReadingType.get().isBulkQuantityReadingType(readingTypes.get(index))) {
                bulkQuantityReadingType.set(readingTypes.get(index++));
            }
        }
        for (; index < readingTypes.size(); index++) {
            this.readingTypeInChannels.add(new ReadingTypeInChannel().init(this, readingTypes.get(index)));
        }
        this.timeSeries.set(createTimeSeries());
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public MeterActivation getMeterActivation() {
        return meterActivation.get();
    }

    public TimeSeries getTimeSeries() {
        return timeSeries.get();
    }

    public Date getLastDateTime() {
        Instant instant = timeSeries.get().getLastDateTime();
        return instant == null ? null : Date.from(instant);
    }

    Optional<TemporalAmount> getIntervalLength() {
        Iterator<ReadingTypeImpl> it = getReadingTypes().iterator();
        Optional<TemporalAmount> result = ((ReadingTypeImpl) it.next()).getIntervalLength();
        while (it.hasNext()) {
            ReadingTypeImpl readingType = (ReadingTypeImpl) it.next();
            Optional<TemporalAmount> intervalLength = readingType.getIntervalLength();
            if (!intervalLength.equals(result)) {
                throw new IllegalArgumentException();
            }
        }
        return result;
    }

    private TimeSeries createTimeSeries() {
        Vault vault = getVault();
        RecordSpec recordSpec = getRecordSpec();
        TimeZone timeZone = clock.getTimeZone();
        return isRegular() ?
                vault.createRegularTimeSeries(recordSpec, clock.getTimeZone(), getIntervalLength().get(), 0) :
                vault.createIrregularTimeSeries(recordSpec, timeZone);
    }

    @Override
    public Object[] toArray(BaseReading reading, ProcessStatus status) {
        return getRecordSpecDefinition().toArray(reading, status);
    }

    private RecordSpecs getRecordSpecDefinition() {
        if (isRegular()) {
            return bulkQuantityReadingType.isPresent() ? RecordSpecs.BULKQUANTITYINTERVAL : RecordSpecs.SINGLEINTERVAL;
        } else {
            return hasMacroPeriod() ? RecordSpecs.BILLINGPERIOD : RecordSpecs.BASEREGISTER;
        }
    }

    private RecordSpec getRecordSpec() {
        return getRecordSpecDefinition().get(idsService);
    }

    private Vault getVault() {
        int id = isRegular() ? REGULARVAULTID : IRREGULARVAULTID;
        Optional<Vault> result = idsService.getVault(MeteringService.COMPONENTNAME, id);
        if (result.isPresent()) {
            return result.get();
        }
        throw new DoesNotExistException(String.valueOf(id));
    }

    @Override
    public List<ReadingTypeImpl> getReadingTypes() {
        ImmutableList.Builder<ReadingTypeImpl> builder = ImmutableList.builder();
        builder.add(getMainReadingType());
        if (bulkQuantityReadingType.isPresent()) {
            builder.add(bulkQuantityReadingType.get());
        }
        for (ReadingTypeInChannel each : readingTypeInChannels) {
            builder.add(each.getReadingType());
        }
        return builder.build();
    }

    @Override
    public List<IntervalReadingRecord> getIntervalReadings(Interval interval) {
        if (!isRegular()) {
            return Collections.emptyList();
        }
        List<TimeSeriesEntry> entries = getTimeSeries().getEntries(interval);
        ImmutableList.Builder<IntervalReadingRecord> builder = ImmutableList.builder();
        for (TimeSeriesEntry entry : entries) {
            builder.add(new IntervalReadingRecordImpl(this, entry));
        }
        return builder.build();
    }

    private BaseReadingRecord createReading(boolean regular, TimeSeriesEntry entry) {
        return regular ? new IntervalReadingRecordImpl(this, entry) : new ReadingRecordImpl(this, entry);
    }

    @Override
    public List<BaseReadingRecord> getReadings(Interval interval) {
        boolean regular = isRegular();
        List<TimeSeriesEntry> entries = getTimeSeries().getEntries(interval);
        ImmutableList.Builder<BaseReadingRecord> builder = ImmutableList.builder();
        for (TimeSeriesEntry entry : entries) {
            builder.add(createReading(regular, entry));
        }
        return builder.build();
    }


    public Optional<BaseReadingRecord> getReading(Date when) {
        java.util.Optional<TimeSeriesEntry> entryHolder = getTimeSeries().getEntry(when.toInstant());
        if (entryHolder.isPresent()) {
            return Optional.of(createReading(isRegular(), entryHolder.get()));
        } else {
            return Optional.absent();
        }
    }

    @Override
    public List<IntervalReadingRecord> getIntervalReadings(ReadingType readingType, Interval interval) {
        if (!isRegular()) {
            return Collections.emptyList();
        }
        List<TimeSeriesEntry> entries = getTimeSeries().getEntries(interval);
        ImmutableList.Builder<IntervalReadingRecord> builder = ImmutableList.builder();
        for (TimeSeriesEntry entry : entries) {
            IntervalReadingRecordImpl reading = new IntervalReadingRecordImpl(this, entry);
            builder.add(new FilteredIntervalReadingRecord(reading, getReadingTypes().indexOf(readingType)));
        }
        return builder.build();
    }

    @Override
    public List<ReadingRecord> getRegisterReadings(ReadingType readingType, Interval interval) {
        if (isRegular()) {
            return Collections.emptyList();
        }
        List<TimeSeriesEntry> entries = getTimeSeries().getEntries(interval);
        ImmutableList.Builder<ReadingRecord> builder = ImmutableList.builder();
        for (TimeSeriesEntry entry : entries) {
            ReadingRecordImpl reading = new ReadingRecordImpl(this, entry);
            builder.add(new FilteredReadingRecord(reading, getReadingTypes().indexOf(readingType)));
        }
        return builder.build();
    }

    @Override
    public List<BaseReadingRecord> getReadings(ReadingType readingType, Interval interval) {
        boolean isRegular = isRegular();
        int index = getReadingTypes().indexOf(readingType);
        List<TimeSeriesEntry> entries = getTimeSeries().getEntries(interval);
        ImmutableList.Builder<BaseReadingRecord> builder = ImmutableList.builder();
        for (TimeSeriesEntry entry : entries) {
            builder.add(
                    isRegular ?
                            new FilteredIntervalReadingRecord(new IntervalReadingRecordImpl(this, entry), index) :
                            new FilteredReadingRecord(new ReadingRecordImpl(this, entry), index));
        }
        return builder.build();
    }

    @Override
    public List<ReadingRecord> getRegisterReadings(Interval interval) {
        if (isRegular()) {
            return Collections.emptyList();
        }
        List<TimeSeriesEntry> entries = getTimeSeries().getEntries(interval);
        ImmutableList.Builder<ReadingRecord> builder = ImmutableList.builder();
        for (TimeSeriesEntry entry : entries) {
            builder.add(new ReadingRecordImpl(this, entry));
        }
        return builder.build();
    }

    @Override
    public ReadingTypeImpl getMainReadingType() {
        return mainReadingType.get();
    }

    @Override
    public Optional<ReadingTypeImpl> getBulkQuantityReadingType() {
        return bulkQuantityReadingType.getOptional();
    }

    @Override
    public ReadingQualityRecord createReadingQuality(ReadingQualityType type, BaseReadingRecord baseReadingRecord) {
        return ReadingQualityRecordImpl.from(dataModel, type, this, baseReadingRecord);
    }

    @Override
    public ReadingQualityRecord createReadingQuality(ReadingQualityType type, Date timestamp) {
        return ReadingQualityRecordImpl.from(dataModel, type, this, timestamp);
    }

    @Override
    public long getVersion() {
        return version;
    }

    public List<ReadingQualityRecord> findReadingQuality(Range<Instant> range) {
        Condition condition = inRange(range).and(ofThisChannel());
        return dataModel.mapper(ReadingQualityRecord.class).select(condition);
    }

    @Override
    public List<ReadingQualityRecord> findReadingQuality(Interval interval) {
        return findReadingQuality(interval.toOpenClosedRange());
    }

    private Condition inRange(Range<Instant> range) {
        return where("readingTimestamp").in(range);
    }

    private Condition ofThisChannel() {
        return where("channelId").isEqualTo(getId());
    }

    @Override
    public Optional<ReadingQualityRecord> findReadingQuality(ReadingQualityType type, Date timestamp) {
        Condition condition = ofThisChannel().and(withTimestamp(timestamp));
        List<ReadingQualityRecord> list = dataModel.mapper(ReadingQualityRecord.class).select(condition);
        return FluentIterable.from(list).first();
    }

    private Condition withTimestamp(Date timestamp) {
        return where("readingTimestamp").isEqualTo(timestamp);
    }

    @Override
    public List<ReadingQualityRecord> findReadingQuality(ReadingQualityType type, Interval interval) {
        Condition ofTypeAndInInterval = ofThisChannel().and(inRange(interval.toClosedRange())).and(where("typeCode").isEqualTo(type.getCode()));
        return dataModel.mapper(ReadingQualityRecord.class).select(ofTypeAndInInterval, Order.ascending("readingTimestamp"));
    }

    @Override
    public List<ReadingQualityRecord> findReadingQuality(Date timestamp) {
        Condition atTimestamp = ofThisChannel().and(withTimestamp(timestamp));
        return dataModel.mapper(ReadingQualityRecord.class).select(atTimestamp, Order.ascending("readingTimestamp"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return id == ((ChannelImpl) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public boolean isRegular() {
        return getIntervalLength().isPresent();
    }

    @Override
    public List<BaseReadingRecord> getReadingsBefore(Date when, int readingCount) {
        boolean regular = isRegular();
        List<TimeSeriesEntry> entries = getTimeSeries().getEntriesBefore(when.toInstant(), readingCount);
        ImmutableList.Builder<BaseReadingRecord> builder = ImmutableList.builder();
        for (TimeSeriesEntry entry : entries) {
            builder.add(createReading(regular, entry));
        }
        return builder.build();
    }

    @Override
    public List<BaseReadingRecord> getReadingsOnOrBefore(Date when, int readingCount) {
        boolean regular = isRegular();
        List<TimeSeriesEntry> entries = getTimeSeries().getEntriesOnOrBefore(when.toInstant(), readingCount);
        ImmutableList.Builder<BaseReadingRecord> builder = ImmutableList.builder();
        for (TimeSeriesEntry entry : entries) {
            builder.add(createReading(regular, entry));
        }
        return builder.build();
    }

    @Override
    public boolean hasMacroPeriod() {
        return !mainReadingType.get().equals(MacroPeriod.NOTAPPLICABLE);
    }

    @Override
    public boolean hasData() {
        return getTimeSeries().getFirstDateTime() != null || getTimeSeries().getLastDateTime() != null;
    }

    @Override
    public void editReadings(List<? extends BaseReading> readings) {
    	if (readings.isEmpty()) {
            return;
        }
    	ReadingStorer storer = meteringService.createOverrulingStorer();
        Range<Instant> range = readings.stream().map(reading -> reading.getTimeStamp().toInstant()).map(Range::singleton).reduce(Range::span).get();
        List<ReadingQualityRecord> allQualityRecords = findReadingQuality(range);
        ReadingQualityType editQualityType = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.EDITGENERIC);
        ReadingQualityType addQualityType = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ADDED);
        for (BaseReading reading : readings) {
        	List<ReadingQualityRecordImpl> currentQualityRecords = allQualityRecords.stream()
        		.filter(qualityRecord -> qualityRecord.getReadingTimestamp().equals(reading.getTimeStamp()))
        		.map(ReadingQualityRecordImpl.class::cast)
        		.collect(Collectors.toList());
        	ProcessStatus processStatus = ProcessStatus.of(ProcessStatus.Flag.EDITED);
        	Optional<BaseReadingRecord> oldReading = getReading(reading.getTimeStamp());
        	boolean hasEditQuality = currentQualityRecords.stream().anyMatch(qualityRecord -> qualityRecord.hasEditCategory());
        	if (oldReading.isPresent()) {
        		processStatus = processStatus.or(oldReading.get().getProcesStatus());
        		if (!hasEditQuality) {
        			this.createReadingQuality(editQualityType, reading.getTimeStamp());
        		}
        	} else if (!hasEditQuality) {
        		this.createReadingQuality(addQualityType, reading.getTimeStamp());
        	}
        	currentQualityRecords.stream()
        		.filter(qualityRecord -> qualityRecord.isSuspect())
        		.forEach(qualityRecord -> qualityRecord.delete());
        	currentQualityRecords.stream()
    			.filter(qualityRecord -> qualityRecord.hasValdiationCategory() || qualityRecord.isMissing())
    			.forEach(qualityRecord -> qualityRecord.clearActualFlag());
        	storer.addReading(this, reading, processStatus);
        }
        storer.execute();
    }

    @Override
    public void removeReadings(List<? extends BaseReadingRecord> readings) {
        if (readings.isEmpty()) {
            return;
        }
        Set<Instant> readingTimes = readings.stream().map(reading -> reading.getTimeStamp().toInstant()).collect(Collectors.toSet());
        List<ReadingQualityRecord> qualityRecords = findReadingQuality(Range.encloseAll(readingTimes));
        readingTimes.forEach(instant -> timeSeries.get().removeEntry(instant));
        qualityRecords.stream()
        	.filter(quality -> readingTimes.contains(quality.getReadingTimestamp().toInstant()))
            .forEach(qualityRecord -> qualityRecord.delete());
    }
}
