package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DoesNotExistException;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import static com.elster.jupiter.metering.impl.Bus.COMPONENTNAME;

public final class ChannelImpl implements Channel {
	
	private static final int REGULARVAULTID = 1;
	private static final int IRREGULARVAULTID = 2;
	private static final int REGULARRECORDSPECID = 1;
	private static final int IRREGULARRECORDSPECID = 2;
	
	// persistent fields
	private long id;
	private long meterActivationId;
	private long timeSeriesId;
	private String mainReadingTypeMRID;
	private String cumulativeReadingTypeMRID;
	private long version;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
	@SuppressWarnings("unused")
	private UtcInstant modTime;
	@SuppressWarnings("unused")
	private String userName;

	
	// associations
	private MeterActivation meterActivation;
	private TimeSeries timeSeries;
	private ReadingType mainReadingType;
	private ReadingType cumulativeReadingType;
	private List<ReadingType> additionalReadingTypes;
	
	@SuppressWarnings("unused")
	private ChannelImpl() {	
	}
	
	ChannelImpl(MeterActivation meterActivation) {
		this.meterActivation = meterActivation;
		this.meterActivationId = meterActivation.getId();
	}
	
	@Override
	public long getId() {
		return id;
	}
	
	@Override 
	public MeterActivation getMeterActivation() {
		if (meterActivation == null) {
			meterActivation = Bus.getOrmClient().getMeterActivationFactory().getExisting(meterActivationId);
		}
		return meterActivation;
	}
	
	@Override
	public TimeSeries getTimeSeries() {
		if (timeSeries == null) {
            Optional<TimeSeries> result = Bus.getIdsService().getTimeSeries(timeSeriesId);
            if (result.isPresent()) {
                timeSeries = result.get();
            } else {
                throw new DoesNotExistException(String.valueOf(timeSeriesId));
            }
		}
		return timeSeries;
	}

	void init(List<ReadingType> readingTypes) {
		this.mainReadingType = readingTypes.get(0);
		this.mainReadingTypeMRID = mainReadingType.getMRID();
		int index = 1;
		if (readingTypes.size() > 1) {
			if (mainReadingType.isCumulativeReadingType(readingTypes.get(index))) {
				cumulativeReadingType = readingTypes.get(index++);
				cumulativeReadingTypeMRID = cumulativeReadingType.getMRID();
			} 
		}
		this.additionalReadingTypes = new ArrayList<>();
		for (; index < readingTypes.size() ; index++) {
			this.additionalReadingTypes.add(readingTypes.get(index));
		}
		this.timeSeries = createTimeSeries();
		this.timeSeriesId = timeSeries.getId();
		Bus.getOrmClient().getChannelFactory().persist(this);
        Bus.getEventService().postEvent(EventType.CHANNEL_CREATED.topic(), this);
		persistReadingTypes();
	}
	
	IntervalLength getIntervalLength() {		
		Iterator<ReadingType> it = getReadingTypes().iterator();
		IntervalLength result = ((ReadingTypeImpl) it.next()).getIntervalLength();
		while (it.hasNext()) {
			ReadingTypeImpl readingType = (ReadingTypeImpl) it.next();
			IntervalLength intervalLength = readingType.getIntervalLength();
			boolean failed = 
					(result == null && intervalLength != null) ||
					(result != null && !result.equals(intervalLength));
			if (failed) {
				throw new IllegalArgumentException();
			}
		}
		return result;
	}
	
	TimeSeries createTimeSeries() {
		IntervalLength intervalLength = getIntervalLength();
        Vault vault = getVault(intervalLength);
        RecordSpec recordSpec = getRecordSpec(intervalLength);
        TimeZone timeZone = Bus.getClock().getTimeZone();
		return intervalLength == null ? 
				vault.createIrregularTimeSeries(recordSpec, timeZone) :
				vault.createRegularTimeSeries(recordSpec, Bus.getClock().getTimeZone(), intervalLength.getLength() , intervalLength.getUnitCode(),0);
	}

    private RecordSpec getRecordSpec(IntervalLength intervalLength) {
        int id = intervalLength == null ? IRREGULARRECORDSPECID : REGULARRECORDSPECID;
        Optional<RecordSpec> result = Bus.getIdsService().getRecordSpec(COMPONENTNAME, id);
        if (result.isPresent()) {
            return result.get();
        }
        throw new DoesNotExistException(String.valueOf(id));
    }

    private Vault getVault(IntervalLength intervalLength) {
        int id = intervalLength == null ? IRREGULARVAULTID : REGULARVAULTID;
        Optional<Vault> result = Bus.getIdsService().getVault(COMPONENTNAME, id);
        if (result.isPresent()) {
            return result.get();
        }
        throw new DoesNotExistException(String.valueOf(id));
    }

    void persistReadingTypes() {
		int offset = 1;
		DataMapper<ReadingTypeInChannel> factory = Bus.getOrmClient().getReadingTypeInChannelFactory();
		for (ReadingType readingType : getAdditionalReadingTypes()) {
			factory.persist(new ReadingTypeInChannel(this, readingType, offset++));
		}
	}
	
	private List<ReadingType> getAdditionalReadingTypes() {
		if (additionalReadingTypes == null) {
			additionalReadingTypes = new ArrayList<>();
			for (ReadingTypeInChannel each : Bus.getOrmClient().getReadingTypeInChannelFactory().find("channel",this)) {
				additionalReadingTypes.add(each.getReadingType());
			}
		}
		return additionalReadingTypes;
	}
	
	@Override
	public List<ReadingType> getReadingTypes() {
        ImmutableList.Builder<ReadingType> builder = ImmutableList.builder();
        builder.add(getMainReadingType());
		ReadingType next = getCumulativeReadingType();
		if (next != null) {
			builder.add(next);
		}
		builder.addAll(getAdditionalReadingTypes());
		return builder.build();
	}

	@Override
	public List<IntervalReading> getIntervalReadings(Date from, Date to) {
		List<TimeSeriesEntry> entries = getTimeSeries().getEntries(from,to);
		ImmutableList.Builder<IntervalReading> builder = ImmutableList.builder();
		for (TimeSeriesEntry entry : entries) {
			builder.add(new IntervalReadingImpl(this, entry));
		}
		return builder.build();
	}
	
	@Override 
	public List<BaseReading> getReadings(Date from, Date to) {
		boolean isRegular = getIntervalLength() != null;
		List<TimeSeriesEntry> entries = getTimeSeries().getEntries(from,to);
		ImmutableList.Builder<BaseReading> builder = ImmutableList.builder();
		for (TimeSeriesEntry entry : entries) {
			builder.add(isRegular ? new IntervalReadingImpl(this, entry) : new ReadingImpl(this, entry));
		}
		return builder.build();
	}

    @Override
    public List<IntervalReading> getIntervalReadings(ReadingType readingType, Date from, Date to) {
        List<TimeSeriesEntry> entries = getTimeSeries().getEntries(from,to);
        ImmutableList.Builder<IntervalReading> builder = ImmutableList.builder();
        for (TimeSeriesEntry entry : entries) {
            IntervalReadingImpl reading = new IntervalReadingImpl(this, entry);
            builder.add(new FilteredIntervalReading(reading, getReadingTypes().indexOf(readingType)));
        }
        return builder.build();
    }

    @Override
    public List<Reading> getRegisterReadings(ReadingType readingType, Date from, Date to) {
        List<TimeSeriesEntry> entries = getTimeSeries().getEntries(from,to);
        ImmutableList.Builder<Reading> builder = ImmutableList.builder();
        for (TimeSeriesEntry entry : entries) {
            ReadingImpl reading = new ReadingImpl(this, entry);
            builder.add(new FilteredReading(reading, getReadingTypes().indexOf(readingType)));
        }
        return builder.build();
    }

    @Override
    public List<BaseReading> getReadings(ReadingType readingType, Date from, Date to) {
    	boolean isRegular = getIntervalLength() != null;
    	int index = getReadingTypes().indexOf(readingType);
        List<TimeSeriesEntry> entries = getTimeSeries().getEntries(from,to);
        ImmutableList.Builder<BaseReading> builder = ImmutableList.builder();
        for (TimeSeriesEntry entry : entries) {
            builder.add(
            	isRegular ? 
            	new FilteredIntervalReading(new IntervalReadingImpl(this, entry), index) :
            	new FilteredReading(new ReadingImpl(this, entry),index));
        }
        return builder.build();
    }
    @Override
	public List<Reading> getRegisterReadings(Date from, Date to) {
		List<TimeSeriesEntry> entries = getTimeSeries().getEntries(from,to);
		ImmutableList.Builder <Reading> builder = ImmutableList.builder();
		for (TimeSeriesEntry entry : entries) {
			builder.add(new ReadingImpl(this, entry));
		}
		return builder.build();
	}
	
	@Override
	public ReadingType getMainReadingType() {
		if (mainReadingType == null) {
			mainReadingType = Bus.getOrmClient().getReadingTypeFactory().getExisting(mainReadingTypeMRID);
		}
		return mainReadingType;
	}
	
	@Override
	public ReadingType getCumulativeReadingType() {
		if (cumulativeReadingTypeMRID == null) {
			return null;
		}
		if (cumulativeReadingType == null) {
			cumulativeReadingType = Bus.getOrmClient().getReadingTypeFactory().getExisting(cumulativeReadingTypeMRID);
		}
		return cumulativeReadingType;
	}

    @Override
    public long getVersion() {
        return version;
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
}
