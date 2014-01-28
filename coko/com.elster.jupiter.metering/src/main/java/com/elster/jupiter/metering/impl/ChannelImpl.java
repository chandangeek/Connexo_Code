package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQuality;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DoesNotExistException;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import static com.elster.jupiter.util.conditions.Where.where;

public final class ChannelImpl implements Channel {
	
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
    private final Clock clock;
    private final DataModel dataModel;

    @Inject
	ChannelImpl(DataModel dataModel, IdsService idsService, Clock clock) {
        this.dataModel = dataModel;
        this.idsService = idsService;
        this.clock = clock;
    }
	
	ChannelImpl init(MeterActivation meterActivation,List<ReadingTypeImpl> readingTypes) {
		this.meterActivation.set(meterActivation);
        this.mainReadingType.set(readingTypes.get(0));
		int index = 1;
		if (readingTypes.size() > 1) {
			if (mainReadingType.get().isBulkQuantityReadingType(readingTypes.get(index))) {
				bulkQuantityReadingType.set(readingTypes.get(index++));
			} 
		}
		for (; index < readingTypes.size() ; index++) {
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
	
	@Override
	public TimeSeries getTimeSeries() {
		return timeSeries.get();
	}
	
	Optional<IntervalLength> getIntervalLength() {		
		Iterator<ReadingTypeImpl> it = getReadingTypes().iterator();
		Optional<IntervalLength> result = ((ReadingTypeImpl) it.next()).getIntervalLength();
		while (it.hasNext()) {
			ReadingTypeImpl readingType = (ReadingTypeImpl) it.next();
			Optional<IntervalLength> intervalLength = readingType.getIntervalLength();
			if (!intervalLength.equals(result)) {
				throw new IllegalArgumentException();
			}
		}
		return result;
	}
	
	private TimeSeries createTimeSeries() {
		Optional<IntervalLength> intervalLength = getIntervalLength();
		boolean regular = intervalLength.isPresent();
        Vault vault = getVault(regular);
        RecordSpec recordSpec = getRecordSpec(regular);
        TimeZone timeZone = clock.getTimeZone();
		return regular ? 
			vault.createRegularTimeSeries(recordSpec, clock.getTimeZone(), intervalLength.get().getLength() , intervalLength.get().getUnitCode(),0) :
			vault.createIrregularTimeSeries(recordSpec, timeZone);
	
	}

    private RecordSpec getRecordSpec(boolean regular) {
    	RecordSpecs definition;
    	if (regular) {
    		if (bulkQuantityReadingType.isPresent()) {
    			definition = RecordSpecs.BULKQUANTITYINTERVAL;
    		} else {
    			definition = RecordSpecs.SINGLEINTERVAL;
    		}
    	} else {
    		definition = RecordSpecs.BASEREGISTER;
    	}
        return definition.get(idsService);
    }

    private Vault getVault(boolean regular) {
        int id = regular ? REGULARVAULTID : IRREGULARVAULTID;
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
		return regular ? new IntervalReadingRecordImpl(this, entry) : new ReadingRecordImpl(this,entry);
	}
	
	@Override 
	public List<BaseReadingRecord> getReadings(Interval interval) {
		boolean regular = isRegular();
		List<TimeSeriesEntry> entries = getTimeSeries().getEntries(interval);
		ImmutableList.Builder<BaseReadingRecord> builder = ImmutableList.builder();
		for (TimeSeriesEntry entry : entries) {
			builder.add(createReading(regular,entry));
		}
		return builder.build();
	}

	
	public Optional<BaseReadingRecord> getReading(Date when) {
		Optional<TimeSeriesEntry> entryHolder = getTimeSeries().getEntry(when);
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
            	new FilteredReadingRecord(new ReadingRecordImpl(this, entry),index));
        }
        return builder.build();
    }
    @Override
	public List<ReadingRecord> getRegisterReadings(Interval interval) {
        if (isRegular()) {
            return Collections.emptyList();
        }
		List<TimeSeriesEntry> entries = getTimeSeries().getEntries(interval);
		ImmutableList.Builder <ReadingRecord> builder = ImmutableList.builder();
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
    public ReadingQuality createReadingQuality(ReadingQualityType type, BaseReadingRecord baseReadingRecord) {
        return ReadingQualityImpl.from(dataModel, type, this, baseReadingRecord);
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public List<ReadingQuality> findReadingQuality(Interval interval) {
        Condition condition = inInterval(interval).and(ofThisChannel());
        return dataModel.mapper(ReadingQuality.class).select(condition); 
    }

    private Condition inInterval(Interval interval) {        
        return where("readingTimestamp").inClosed(interval);
    }

    private Condition ofThisChannel() {
        return where("channelId").isEqualTo(getId());
    }

    @Override
    public Optional<ReadingQuality> findReadingQuality(ReadingQualityType type, Date timestamp) {
        Condition condition = ofThisChannel().and(withTimestamp(timestamp));
        List<ReadingQuality> list = dataModel.mapper(ReadingQuality.class).select(condition);
        return FluentIterable.from(list).first();
    }

    private Condition withTimestamp(Date timestamp) {
        return where("readingTimestamp").isEqualTo(timestamp);
    }

    @Override
    public List<ReadingQuality> findReadingQuality(ReadingQualityType type, Interval interval) {
        Condition ofTypeAndInInterval = inInterval(interval).and(Operator.EQUAL.compare("typeCode", type.getCode()));
        return dataModel.mapper(ReadingQuality.class).select(ofTypeAndInInterval,"readingTimestamp");
    }

    @Override
    public List<ReadingQuality> findReadingQuality(Date timestamp) {
        Condition atTimestamp = withTimestamp(timestamp);
        return dataModel.mapper(ReadingQuality.class).select(atTimestamp, "readingTimestamp");
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
    
}
