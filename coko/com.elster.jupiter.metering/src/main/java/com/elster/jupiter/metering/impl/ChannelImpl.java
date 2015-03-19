package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.EventType;
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
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DoesNotExistException;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public final class ChannelImpl implements ChannelContract {

    static final int INTERVALVAULTID = 1;
    static final int IRREGULARVAULTID = 2;
    static final int DAILYVAULTID = 3;

    // persistent fields
    private long id;
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
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
    @SuppressWarnings("unused")
	private final Clock clock;
    private final DataModel dataModel;
    private final EventService eventService;

    @Inject
    ChannelImpl(DataModel dataModel, IdsService idsService, MeteringService meteringService, Clock clock, EventService eventService) {
        this.dataModel = dataModel;
        this.idsService = idsService;
        this.meteringService = meteringService;
        this.clock = clock;
        this.eventService = eventService;
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
        this.timeSeries.set(createTimeSeries(meterActivation.getZoneId()));
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
    
    public ZoneId getZoneId() {
    	return timeSeries.get().getZoneId();
    }

    @Override
    public Instant getLastDateTime() {
        return timeSeries.get().getLastDateTime();
    }

    @Override
    public Instant getFirstDateTime() {
    	return timeSeries.get().getFirstDateTime();
    }

    @Override
    public Instant getNextDateTime(Instant instant) {
        return getTimeSeries().getNextDateTime(instant);
    }

    @Override
    public Instant getPreviousDateTime(Instant instant) {
        return getTimeSeries().getPreviousDateTime(instant);
    }

    @Override
    public Optional<TemporalAmount> getIntervalLength() {
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

    private TimeSeries createTimeSeries(ZoneId zoneId) {
        Vault vault = getVault();
        RecordSpec recordSpec = getRecordSpec();
        TimeZone timeZone = TimeZone.getTimeZone(zoneId);
        return isRegular() ?
                vault.createRegularTimeSeries(recordSpec, timeZone, getIntervalLength().get(), 0) :
                vault.createIrregularTimeSeries(recordSpec, timeZone);
    }

    @Override
    public Object[] toArray(BaseReading reading, ProcessStatus status) {
        return getRecordSpecDefinition().toArray(reading, status);
    }

    @Override
    public void validateValues(BaseReading reading, Object[] values) {
        getRecordSpecDefinition().validateValues(reading, values);
    }

    public Optional<Range<Instant>> getTimePeriod(BaseReading reading, Object[] values){
        return getRecordSpecDefinition().getTimePeriod(reading, values);
    }

    RecordSpecs getRecordSpecDefinition() {
        if (isRegular()) {
            return bulkQuantityReadingType.isPresent() ? RecordSpecs.BULKQUANTITYINTERVAL : RecordSpecs.SINGLEINTERVAL;
        } else {
            return hasMacroPeriod() ? RecordSpecs.BILLINGPERIOD : RecordSpecs.BASEREGISTER;
        }
    }

    private RecordSpec getRecordSpec() {
        return getRecordSpecDefinition().get(idsService);
    }

    private int getVaultId() {
    	return getIntervalLength()
    		.map(temporalAmount -> temporalAmount instanceof Period ? DAILYVAULTID : INTERVALVAULTID)
    		.orElse(IRREGULARVAULTID);
    }
    
    private Vault getVault() {
        Optional<Vault> result = idsService.getVault(MeteringService.COMPONENTNAME, getVaultId());
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
    public List<IntervalReadingRecord> getIntervalReadings(Range<Instant> interval) {
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
    public List<BaseReadingRecord> getReadings(Range<Instant> interval) {
        boolean regular = isRegular();
        List<TimeSeriesEntry> entries = getTimeSeries().getEntries(interval);
        ImmutableList.Builder<BaseReadingRecord> builder = ImmutableList.builder();
        for (TimeSeriesEntry entry : entries) {
            builder.add(createReading(regular, entry));
        }
        return builder.build();
    }


    public Optional<BaseReadingRecord> getReading(Instant when) {
        java.util.Optional<TimeSeriesEntry> entryHolder = getTimeSeries().getEntry(when);
        if (entryHolder.isPresent()) {
            return Optional.of(createReading(isRegular(), entryHolder.get()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<IntervalReadingRecord> getIntervalReadings(ReadingType readingType, Range<Instant> interval) {
        if (!isRegular()) {
            return Collections.emptyList();
        }
        List<TimeSeriesEntry> entries = getTimeSeries().getEntries(interval);
        ImmutableList.Builder<IntervalReadingRecord> builder = ImmutableList.builder();
        for (TimeSeriesEntry entry : entries) {
            IntervalReadingRecordImpl reading = new IntervalReadingRecordImpl(this, entry);
            builder.add(reading.filter(readingType));
        }
        return builder.build();
    }

    @Override
    public List<ReadingRecord> getRegisterReadings(ReadingType readingType, Range<Instant> interval) {
        if (isRegular()) {
            return Collections.emptyList();
        }
        List<TimeSeriesEntry> entries = getTimeSeries().getEntries(interval);
        ImmutableList.Builder<ReadingRecord> builder = ImmutableList.builder();
        for (TimeSeriesEntry entry : entries) {
            ReadingRecordImpl reading = new ReadingRecordImpl(this, entry);
            builder.add(reading.filter(readingType));
        }
        return builder.build();
    }

    @Override
    public List<BaseReadingRecord> getReadings(ReadingType readingType, Range<Instant> interval) {
        boolean isRegular = isRegular();
        List<TimeSeriesEntry> entries = getTimeSeries().getEntries(interval);
        ImmutableList.Builder<BaseReadingRecord> builder = ImmutableList.builder();
        for (TimeSeriesEntry entry : entries) {
        	BaseReadingRecord reading = isRegular ?
                    new IntervalReadingRecordImpl(this, entry) :
                    new ReadingRecordImpl(this, entry);
            builder.add(reading.filter(readingType));
        }
        return builder.build();
    }

    @Override
    public List<ReadingRecord> getRegisterReadings(Range<Instant> interval) {
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
    public ReadingQualityRecord createReadingQuality(ReadingQualityType type, BaseReading baseReading) {
        return ReadingQualityRecordImpl.from(dataModel, type, this, baseReading);
    }

    @Override
    public ReadingQualityRecord createReadingQuality(ReadingQualityType type, Instant timestamp) {
        return ReadingQualityRecordImpl.from(dataModel, type, this, timestamp);
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public List<ReadingQualityRecord> findReadingQuality(Range<Instant> range) {
        Condition condition = inRange(range).and(ofThisChannel());
        return dataModel.mapper(ReadingQualityRecord.class).select(condition,  Order.ascending("readingTimestamp"));
    }

    @Override
    public List<ReadingQualityRecord> findActualReadingQuality(Range<Instant> range) {
        Condition condition = inRange(range).and(ofThisChannel()).and(isActual());
        return dataModel.mapper(ReadingQualityRecord.class).select(condition, Order.ascending("readingTimestamp"));
    }

    private Condition isActual() {
        return where("actual").isEqualTo(true);
    }

    private Condition inRange(Range<Instant> range) {
        return where("readingTimestamp").in(range);
    }

    private Condition ofThisChannel() {
        return where("channel").isEqualTo(this);
    }

    private Condition ofType(ReadingQualityType type) {
    	return where("typeCode").isEqualTo(type.getCode());
    }
    
    @Override
    public Optional<ReadingQualityRecord> findReadingQuality(ReadingQualityType type, Instant timestamp) {
        Condition condition = ofThisChannel().and(withTimestamp(timestamp)).and(ofType(type));
        return dataModel.mapper(ReadingQualityRecord.class).select(condition).stream().findFirst();
    }

    private Condition withTimestamp(Instant timestamp) {
        return where("readingTimestamp").isEqualTo(timestamp);
    }

    @Override
    public List<ReadingQualityRecord> findReadingQuality(ReadingQualityType type, Range<Instant> range) {
        Condition ofTypeAndInInterval = ofThisChannel().and(inRange(range)).and(ofType(type));
        return dataModel.mapper(ReadingQualityRecord.class).select(ofTypeAndInInterval, Order.ascending("readingTimestamp"));
    }

    @Override
    public List<ReadingQualityRecord> findActualReadingQuality(ReadingQualityType type, Range<Instant> interval) {
        Condition ofTypeAndInInterval = ofThisChannel().and(inRange(interval)).and(ofType(type)).and(isActual());
        return dataModel.mapper(ReadingQualityRecord.class).select(ofTypeAndInInterval, Order.ascending("readingTimestamp"));
    }

    @Override
    public List<ReadingQualityRecord> findReadingQuality(Instant timestamp) {
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
    public List<BaseReadingRecord> getReadingsBefore(Instant when, int readingCount) {
        boolean regular = isRegular();
        List<TimeSeriesEntry> entries = getTimeSeries().getEntriesBefore(when, readingCount);
        ImmutableList.Builder<BaseReadingRecord> builder = ImmutableList.builder();
        for (TimeSeriesEntry entry : entries) {
            builder.add(createReading(regular, entry));
        }
        return builder.build();
    }

    @Override
    public List<BaseReadingRecord> getReadingsOnOrBefore(Instant when, int readingCount) {
        boolean regular = isRegular();
        List<TimeSeriesEntry> entries = getTimeSeries().getEntriesOnOrBefore(when, readingCount);
        ImmutableList.Builder<BaseReadingRecord> builder = ImmutableList.builder();
        entries.forEach(entry -> builder.add(createReading(regular, entry)));
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
        Range<Instant> range = readings.stream().map(reading -> reading.getTimeStamp()).map(Range::singleton).reduce(Range::span).get();
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
        			this.createReadingQuality(editQualityType, reading).save();
        		}
        	} else if (!hasEditQuality) {
        		this.createReadingQuality(addQualityType, reading).save();
        	}
        	currentQualityRecords.stream()
        		.filter(qualityRecord -> qualityRecord.isSuspect())
        		.forEach(qualityRecord -> qualityRecord.delete());
        	currentQualityRecords.stream()
    			.filter(qualityRecord -> qualityRecord.hasValidationCategory() || qualityRecord.isMissing())
    			.forEach(qualityRecord -> qualityRecord.makePast());
        	storer.addReading(this, reading, processStatus);
        }
        storer.execute();
    }

    @Override
    public void removeReadings(List<? extends BaseReadingRecord> readings) {
        if (readings.isEmpty()) {
            return;
        }
        Set<Instant> readingTimes = readings.stream().map(reading -> reading.getTimeStamp()).collect(Collectors.toSet());
        List<ReadingQualityRecord> qualityRecords = findReadingQuality(Range.encloseAll(readingTimes));
        readingTimes.forEach(instant -> timeSeries.get().removeEntry(instant));
        qualityRecords.stream()
        	.filter(quality -> readingTimes.contains(quality.getReadingTimestamp()))
            .forEach(qualityRecord -> qualityRecord.delete());
        ReadingQualityType rejected = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.REJECTED);
        readingTimes.forEach(readingTime -> createReadingQuality(rejected, readingTime).save());
        eventService.postEvent(EventType.READINGS_DELETED.topic(), new ReadingsDeletedEventImpl(this,readingTimes));
    }
    
    @Override
	public List<Instant> toList(Range<Instant> range) {
		return timeSeries.get().toList(range);
	}
    
    public static class ReadingsDeletedEventImpl implements Channel.ReadingsDeletedEvent {
		private ChannelImpl channel;
		private Set<Instant> readingTimes;
    	
    	public ReadingsDeletedEventImpl(ChannelImpl channel, Set<Instant> readingTimes) {
    		this.channel = channel;
    		this.readingTimes = readingTimes;
		}
    	
    	@Override
    	public Channel getChannel() {
			return channel;
		}

    	@Override
		public Set<Instant> getReadingTimeStamps(){
			return readingTimes;
		}

    	public long getChannelId() {
    		return channel.getId();
    	}
    	
    	public long getStartMillis() {
    		return readingTimes.stream().min(Comparator.naturalOrder()).get().toEpochMilli();
    	}
    	
    	public long getEndMillis() {
    		return readingTimes.stream().max(Comparator.naturalOrder()).get().toEpochMilli();
    	}

    }

	
}
