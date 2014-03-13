package com.elster.jupiter.metering.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.time.Interval;

public class MeterActivationsImpl implements ReadingContainer {
	
	private final List<MeterActivationImpl> meterActivations = new ArrayList<>();
	
	private void add(MeterActivationImpl meterActivation) {
		meterActivations.add(meterActivation);
	}

	@Override
	public List<? extends BaseReadingRecord> getReadings(Interval interval, ReadingType readingType) {
		List<BaseReadingRecord> result = new ArrayList<>();
		for (MeterActivationImpl meterActivation : meterActivations)  {
			result.addAll(meterActivation.getReadings(interval,readingType));
		}
		return result;
	}

	@Override
	public Set<ReadingType> getReadingTypes(Interval interval) {
		Set<ReadingType> result = new HashSet<>();
		for (MeterActivationImpl meterActivation : meterActivations)  {
			result.addAll(meterActivation.getReadingTypes());
		}
		return result;	
	}

	@Override
	public List<? extends BaseReadingRecord> getReadingsBefore(Date when, ReadingType readingType, int count) {
		if (meterActivations.isEmpty()) {
			return Collections.emptyList();
		}
		List <BaseReadingRecord> result = new ArrayList<>();
		result.addAll(last().getReadingsBefore(when, readingType , count));
		for (int i = meterActivations.size() - 2 ; i >= 0 && result.size() < count ; i--) {
			result.addAll(meterActivations.get(i).getReadingsBefore(when,readingType, count - result.size()));
		}
		return result;
	}
	
	private MeterActivationImpl last() {
		return meterActivations.get(meterActivations.size() - 1);
	}
	
	public static MeterActivationsImpl from(List<MeterActivationImpl> candidates , Interval interval) {
		MeterActivationsImpl meterActivations = new MeterActivationsImpl();
		for (MeterActivationImpl meterActivation : candidates) {
			if (meterActivation.overlaps(interval)) {
				meterActivations.add(meterActivation);
			}
		}
		return meterActivations;
	}
	
	public static MeterActivationsImpl from(List<MeterActivationImpl> candidates) {
		MeterActivationsImpl meterActivations = new MeterActivationsImpl();
		for (MeterActivationImpl meterActivation : candidates) {
			meterActivations.add(meterActivation);
		}
		return meterActivations;
	}
}
