package com.elster.jupiter.metering.impl;

import java.util.*;

import com.elster.jupiter.ids.*;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.time.UtcInstant;

import static com.elster.jupiter.metering.impl.Bus.*;

class ChannelImpl implements Channel {
	
	private static final int REGULARVAULTID = 1;
	private static final int IRREGULARVAULTID = 2;
	private static final int REGULARRECORDSPECID = 1;
	private static final int IRREGULARRECORDSPECID = 2;
	
	// persistent fields
	private long id;
	private long meterActivationId;
	private long timeSeriesId;
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
	private List<ReadingType> readingTypes;
	
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
			timeSeries = Bus.getServiceLocator().getIdsService().getTimeSeries(timeSeriesId);
		}
		return timeSeries;
	}

	void init(ReadingType[] readingTypes) {
		this.readingTypes = Arrays.asList(readingTypes);
		this.timeSeries = createTimeSeries();
		this.timeSeriesId = timeSeries.getId();
		Bus.getOrmClient().getChannelFactory().persist(this);
		persistReadingTypes();
	}
	
	IntervalLength getIntervalLength() {
		if (readingTypes.isEmpty()) {
			throw new IllegalArgumentException();
		}
		Iterator<ReadingType> it = readingTypes.iterator();
		IntervalLength result = ((ReadingTypeImpl) it.next()).getIntervalLength();
		while (it.hasNext()) {
			ReadingTypeImpl readingType = (ReadingTypeImpl) it.next();
			IntervalLength intervalLength = readingType.getIntervalLength();
			boolean failed = 
					(result == null && intervalLength != null) ||
					((result != null && intervalLength == null) || !result.equals(intervalLength));
			if (failed) {
				throw new IllegalArgumentException();
			}
		}
		return result;
	}
	
	TimeSeries createTimeSeries() {
		IntervalLength intervalLength = getIntervalLength();
		Vault vault = getServiceLocator().getIdsService().getVault(COMPONENTNAME, intervalLength == null ? IRREGULARVAULTID : REGULARVAULTID);
		RecordSpec recordSpec = getServiceLocator().getIdsService().getRecordSpec(COMPONENTNAME, intervalLength == null ? IRREGULARRECORDSPECID : REGULARRECORDSPECID);
		TimeZone timeZone = TimeZone.getDefault();
		return intervalLength == null ? 
				vault.createIrregularTiemSeries(recordSpec, timeZone) :
				vault.createRegularTimeSeries(recordSpec, TimeZone.getDefault(), intervalLength.getLength() , intervalLength.getUnitCode(),0);		
	}
	
	void persistReadingTypes() {
		int offset = 1;
		DataMapper<ReadingTypeInChannel> factory = Bus.getOrmClient().getReadingTypeInChannelFactory();
		for (ReadingType readingType : readingTypes) {
			factory.persist(new ReadingTypeInChannel(this, readingType, offset++));
		}
	}
	
	private List<ReadingType> getReadingTypes(boolean protect) {
		if (readingTypes == null) {
			readingTypes = doGetReadingTypes();
		}
		return protect ? Collections.unmodifiableList(readingTypes) : readingTypes;
		
	}
	
	private List<ReadingType> doGetReadingTypes() {
		List<ReadingTypeInChannel> helpers = Bus.getOrmClient().getReadingTypeInChannelFactory().find("channelId",getId(),"position");
		List<ReadingType> result = new ArrayList<>(helpers.size());
		for (ReadingTypeInChannel each : helpers) {
			result.add(each.getReadingType());
		}
		return result;
	}
	
	@Override
	public List<ReadingType> getReadingTypes() {
		return getReadingTypes(true);
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
}
