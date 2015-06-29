package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MeterActivationsImpl implements ReadingContainer {
	
	private final List<MeterActivationImpl> meterActivations = new ArrayList<>();
	
	private void add(MeterActivationImpl meterActivation) {
		meterActivations.add(meterActivation);
	}

	@Override
	public List<? extends BaseReadingRecord> getReadings(Range<Instant> range, ReadingType readingType) {
		return meterActivations.stream()
				.flatMap(meterActivation -> meterActivation.getReadings(range, readingType).stream())
				.collect(Collectors.toList());
	}

	@Override
	public List<? extends BaseReadingRecord> getReadingsUpdatedSince(Range<Instant> range, ReadingType readingType, Instant since) {
		return meterActivations.stream()
				.flatMap(meterActivation -> meterActivation.getReadingsUpdatedSince(range, readingType, since).stream())
				.collect(Collectors.toList());
	}

	@Override
	public Set<ReadingType> getReadingTypes(Range<Instant> range) {
		return meterActivations.stream()
				.map(MeterActivation::getReadingTypes)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
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
		candidates.stream()
				.filter(meterActivation -> meterActivation.overlaps(range))
				.forEach(meterActivations::add);
		return meterActivations;
	}

	@Override
	public List<Instant> toList(ReadingType readingType, Range<Instant> exportInterval) {
		return meterActivations.stream()
				.findFirst()
				.map(meterActivation -> meterActivation.toList(readingType, exportInterval))
				.orElseGet(Collections::emptyList);
	}

	public static MeterActivationsImpl from(List<MeterActivationImpl> candidates) {
		MeterActivationsImpl meterActivations = new MeterActivationsImpl();
		candidates.stream()
				.forEach(meterActivations::add);
		return meterActivations;
	}

    @Override
    public boolean is(ReadingContainer other) {
        return this == other;
    }

    @Override
    public Optional<Meter> getMeter(Instant instant) {
        return meterActivations.stream()
                .filter(activation -> activation.getRange().contains(instant))
                .findAny()
                .flatMap(MeterActivationImpl::getMeter);
    }

    @Override
    public Optional<UsagePoint> getUsagePoint(Instant instant) {
        return meterActivations.stream()
                .filter(activation -> activation.getRange().contains(instant))
                .findAny()
                .flatMap(MeterActivationImpl::getUsagePoint);
    }

    @Override
    public ZoneId getZoneId() {
        return meterActivations.stream()
                .findAny()
                .map(MeterActivation::getZoneId)
                .orElse(ZoneId.systemDefault());
    }

	@Override
	public List<ReadingQualityRecord> getReadingQualities(ReadingQualityType readingQualityType, ReadingType readingType, Range<Instant> interval) {
		return meterActivations.stream()
				.flatMap(meterActivation -> meterActivation.getReadingQualities(readingQualityType, readingType, interval).stream())
				.collect(Collectors.toList());
	}

    @Override
    public List<? extends MeterActivation> getMeterActivations() {
        return Collections.unmodifiableList(meterActivations);
    }
}
