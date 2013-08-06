package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReading;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.Reading;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.plumbing.Bus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DoesNotExistException;
import com.elster.jupiter.orm.PersistenceEvent;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import static com.elster.jupiter.metering.plumbing.Bus.COMPONENTNAME;
import static com.elster.jupiter.metering.plumbing.Bus.getIdsService;

public class ChannelImpl implements Channel {
	
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
                throw new DoesNotExistException();
            }
		}
		return timeSeries;
	}

	void init(ReadingType[] readingTypes) {
		this.mainReadingType = readingTypes[0];
		this.mainReadingTypeMRID = mainReadingType.getMRID();
		int index = 1;
		if (readingTypes.length > 1) {
			if (mainReadingType.isCumulativeReadingType(readingTypes[index])) {
				cumulativeReadingType = readingTypes[index++];
				cumulativeReadingTypeMRID = cumulativeReadingType.getMRID();
			} 
		}
		this.additionalReadingTypes = new ArrayList<>();
		for (; index < readingTypes.length ; index++) {
			this.additionalReadingTypes.add(readingTypes[index]);
		}
		this.timeSeries = createTimeSeries();
		this.timeSeriesId = timeSeries.getId();
		Bus.getOrmClient().getChannelFactory().persist(this);
        Bus.getPublisher().publish(this, PersistenceEvent.CREATED);
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
        TimeZone timeZone = TimeZone.getDefault();
		return intervalLength == null ? 
				vault.createIrregularTiemSeries(recordSpec, timeZone) :
				vault.createRegularTimeSeries(recordSpec, TimeZone.getDefault(), intervalLength.getLength() , intervalLength.getUnitCode(),0);		
	}

    private RecordSpec getRecordSpec(IntervalLength intervalLength) {
        Optional<RecordSpec> result = getIdsService().getRecordSpec(COMPONENTNAME, intervalLength == null ? IRREGULARRECORDSPECID : REGULARRECORDSPECID);
        if (result.isPresent()) {
        return result.get();
        }
        throw new DoesNotExistException();
    }

    private Vault getVault(IntervalLength intervalLength) {
        Optional<Vault> result = getIdsService().getVault(COMPONENTNAME, intervalLength == null ? IRREGULARVAULTID : REGULARVAULTID);
        if (result.isPresent()) {
            return result.get();
        }
        throw new DoesNotExistException();
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
		List<ReadingType> result = new ArrayList<>();
		result.add(getMainReadingType());
		ReadingType next = getCumulativeReadingType();
		if (next != null) {
			result.add(next);
		}
		result.addAll(getAdditionalReadingTypes());
		return result;
	}

	@Override
	public List<IntervalReading> getIntervalReadings(Date from, Date to) {
		List<TimeSeriesEntry> entries = getTimeSeries().getEntries(from,to);
		List<IntervalReading> result = new ArrayList<>(entries.size());
		for (TimeSeriesEntry entry : entries) {
			result.add(new IntervalReadingImpl(this, entry));
		}
		return result;
	}

	@Override
	public List<Reading> getRegisterReadings(Date from, Date to) {
		List<TimeSeriesEntry> entries = getTimeSeries().getEntries(from,to);
		List<Reading> result = new ArrayList<>(entries.size());
		for (TimeSeriesEntry entry : entries) {
			result.add(new ReadingImpl(this, entry));
		}
		return result;	
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
}
