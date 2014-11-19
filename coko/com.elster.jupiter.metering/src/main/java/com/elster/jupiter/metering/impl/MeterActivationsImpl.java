package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MeterActivationsImpl implements ReadingContainer {
	
	private final List<MeterActivationImpl> meterActivations = new ArrayList<>();
	
	private void add(MeterActivationImpl meterActivation) {
		meterActivations.add(meterActivation);
	}

	@Override
	public List<? extends BaseReadingRecord> getReadings(Range<Instant> range, ReadingType readingType) {
		List<BaseReadingRecord> result = new ArrayList<>();
		for (MeterActivationImpl meterActivation : meterActivations)  {
			result.addAll(meterActivation.getReadings(range, readingType));
		}
		return result;
	}

	@Override
	public Set<ReadingType> getReadingTypes(Range<Instant> range) {
		Set<ReadingType> result = new HashSet<>();
		for (MeterActivationImpl meterActivation : meterActivations)  {
			result.addAll(meterActivation.getReadingTypes());
		}
		return result;	
	}

	@Override
	public List<? extends BaseReadingRecord> getReadingsBefore(Instant when, ReadingType readingType, int count) {
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
	
	@Override
	public List<? extends BaseReadingRecord> getReadingsOnOrBefore(Instant when, ReadingType readingType, int count) {
		if (meterActivations.isEmpty()) {
			return Collections.emptyList();
		}
		List <BaseReadingRecord> result = new ArrayList<>();
		result.addAll(last().getReadingsOnOrBefore(when, readingType , count));
		for (int i = meterActivations.size() - 2 ; i >= 0 && result.size() < count ; i--) {
			result.addAll(meterActivations.get(i).getReadingsOnOrBefore(when, readingType, count - result.size()));
		}
		return result;
	}

    @Override
    public boolean hasData() {
        return meterActivations.stream().anyMatch(MeterActivationImpl::hasData);
    }

    private MeterActivationImpl last() {
		return meterActivations.get(meterActivations.size() - 1);
	}
	
	public static MeterActivationsImpl from(List<MeterActivationImpl> candidates , Range<Instant> range) {
		MeterActivationsImpl meterActivations = new MeterActivationsImpl();
		for (MeterActivationImpl meterActivation : candidates) {
			if (meterActivation.overlaps(range)) {
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

    @Override
    public boolean is(ReadingContainer other) {
        return this == other;
    }
}
