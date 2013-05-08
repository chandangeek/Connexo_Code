package com.elster.jupiter.metering.impl;

import java.util.*;

import com.elster.jupiter.ids.*;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.plumbing.Bus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.util.time.UtcInstant;

import static com.elster.jupiter.metering.plumbing.Bus.*;

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
	@SuppressWarnings("unused")
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
	}
	@Override
	public long getId() {
		return id;
	}
	
	@Override 
	public MeterActivation getMeterActivation() {
		if (meterActivation == null) {
			meterActivation = Bus.getOrmClient().getMeterActivationFactory().get(meterActivationId);
		}
		return meterActivation;
	}
	
	@Override
	public TimeSeries getTimeSeries() {
		if (timeSeries == null) {
			timeSeries = Bus.getIdsService().getTimeSeries(timeSeriesId);
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
		this.additionalReadingTypes = new ArrayList<ReadingType>();
		for (; index < readingTypes.length ; index++) {
			this.additionalReadingTypes.add(readingTypes[index]);
		}
		this.timeSeries = createTimeSeries();
		this.timeSeriesId = timeSeries.getId();
		Bus.getOrmClient().getChannelFactory().persist(this);
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
		Vault vault = getIdsService().getVault(COMPONENTNAME, intervalLength == null ? IRREGULARVAULTID : REGULARVAULTID);
		RecordSpec recordSpec = getIdsService().getRecordSpec(COMPONENTNAME, intervalLength == null ? IRREGULARRECORDSPECID : REGULARRECORDSPECID);
		TimeZone timeZone = TimeZone.getDefault();
		return intervalLength == null ? 
				vault.createIrregularTiemSeries(recordSpec, timeZone) :
				vault.createRegularTimeSeries(recordSpec, TimeZone.getDefault(), intervalLength.getLength() , intervalLength.getUnitCode(),0);		
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
			mainReadingType = Bus.getOrmClient().getReadingTypeFactory().get(mainReadingTypeMRID);
		}
		return mainReadingType;
	}
	
	@Override
	public ReadingType getCumulativeReadingType() {
		if (cumulativeReadingTypeMRID == null) {
			return null;
		}
		if (cumulativeReadingType == null) {
			cumulativeReadingType = Bus.getOrmClient().getReadingTypeFactory().get(cumulativeReadingTypeMRID);
		}
		return cumulativeReadingType;
	}
}
