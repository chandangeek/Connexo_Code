package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.CannotDeleteMeter;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeterAlreadyActive;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MeterImpl extends AbstractEndDeviceImpl<MeterImpl> implements Meter {

    @SuppressWarnings("unused")
    private Reference<AmrSystem> amrSystem = ValueReference.absent();
    private List<MeterActivationImpl> meterActivations = new ArrayList<>();

    private final MeteringService meteringService;
    private final Thesaurus thesaurus;
    private final Provider<MeterActivationImpl> meterActivationFactory;
    private final Provider<EndDeviceEventRecordImpl> deviceEventFactory;

    @Inject
    MeterImpl(Clock clock, DataModel dataModel, EventService eventService, Provider<EndDeviceEventRecordImpl> deviceEventFactory,
              MeteringService meteringService, Thesaurus thesaurus, Provider<MeterActivationImpl> meterActivationFactory) {
        super(clock, dataModel, eventService, deviceEventFactory, MeterImpl.class);
        this.meteringService = meteringService;
        this.thesaurus = thesaurus;
        this.meterActivationFactory = meterActivationFactory;
        this.deviceEventFactory = deviceEventFactory;
    }

    @Override
    public List<? extends MeterActivation> getMeterActivations() {
        return ImmutableList.copyOf(meterActivations);
    }

    @Override
    public void store(MeterReading meterReading) {
        new MeterReadingStorer(getDataModel(), meteringService, this, meterReading, thesaurus, getEventService(), deviceEventFactory).store();
    }

    @Override
    public MeterActivationImpl activate(Instant start) {
        checkOverlaps(start);
        MeterActivationImpl result = meterActivationFactory.get().init(this, start);
        getDataModel().persist(result);
        meterActivations.add(result);
        return result;
    }

    private void checkOverlaps(Instant start) {
        if (meterActivations.stream()
                .filter(meterActivation -> meterActivation.getRange().isConnected(Range.atLeast(start)))
                .anyMatch(meterActivation -> !meterActivation.getRange().intersection(Range.atLeast(start)).isEmpty())) {
            throw new MeterAlreadyActive(thesaurus, this, start);
        }
    }

    @Override
    public MeterActivationImpl activate(UsagePoint usagePoint, Instant start) {
        checkOverlaps(start);
        MeterActivationImpl result = meterActivationFactory.get().init(this, usagePoint, start);
        getDataModel().persist(result);
        meterActivations.add(result);
        return result;
    }

    void adopt(MeterActivationImpl meterActivation) {
        meterActivations.stream()
                .filter(activation -> activation.getId() != meterActivation.getId())
                .reduce((m1, m2) -> m2)
                .ifPresent(last -> {
                    if (last.getRange().lowerEndpoint().isAfter(meterActivation.getRange().lowerEndpoint())) {
                        throw new IllegalArgumentException("Invalid start date");
                    } else {
                        if (!last.getRange().hasUpperBound() || last.getRange().upperEndpoint().isAfter(meterActivation.getRange().lowerEndpoint())) {
                            last.endAt(meterActivation.getRange().lowerEndpoint());
                        }
                    }
                });
        Optional<MeterActivationImpl> first = meterActivations.stream().filter(activation -> activation.getId() != meterActivation.getId()).findFirst();
        if (!first.isPresent()) {
            meterActivations.add(meterActivation);
        }
    }


    @Override
    public Optional<MeterActivation> getCurrentMeterActivation() {
        return meterActivations.stream()
                .map(MeterActivation.class::cast)
                .filter(MeterActivation::isCurrent)
                .findAny();
    }

    @Override
    public Optional<? extends MeterActivation> getMeterActivation(Instant when) {
        return meterActivations.stream()
                .filter(meterActivation -> meterActivation.isEffectiveAt(when))
                .findFirst();
    }


    @Override
    public List<? extends BaseReadingRecord> getReadings(Range<Instant> range, ReadingType readingType) {
        return MeterActivationsImpl.from(meterActivations, range).getReadings(range, readingType);
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsUpdatedSince(Range<Instant> range, ReadingType readingType, Instant since) {
        return MeterActivationsImpl.from(meterActivations, range).getReadingsUpdatedSince(range, readingType, since);
    }

    @Override
    public Set<ReadingType> getReadingTypes(Range<Instant> range) {
        return MeterActivationsImpl.from(meterActivations, range).getReadingTypes(range);
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsBefore(Instant when, ReadingType readingType, int count) {
        return MeterActivationsImpl.from(meterActivations).getReadingsBefore(when, readingType, count);
    }


    @Override
    public List<? extends BaseReadingRecord> getReadingsOnOrBefore(Instant when, ReadingType readingType, int count) {
        return MeterActivationsImpl.from(meterActivations).getReadingsOnOrBefore(when, readingType, count);
    }

    @Override
    public boolean hasData() {
        return MeterActivationsImpl.from(meterActivations).hasData();
    }

    @Override
    public void delete() {
        if (meterActivations.size() > 0) {
            throw new CannotDeleteMeter(thesaurus, MessageSeeds.CANNOT_DELETE_METER_METER_ACTIVATIONS_EXIST, getMRID());
        }

        super.delete();
    }

    @Override
    public List<? extends ReadingQualityRecord> getReadingQualities(Range<Instant> range) {
        if (!range.hasLowerBound() && !range.hasUpperBound()) {
            throw new IllegalArgumentException();
        }
        QueryExecutor<ReadingQualityRecord> query = getDataModel().query(ReadingQualityRecord.class, Channel.class, MeterActivation.class);
        Condition condition = Where.where("channel.meterActivation.meter").isEqualTo(this);
        condition = condition.and(Where.where("readingTimestamp").in(range));
        return query.select(condition);
    }

    @Override
    public boolean is(ReadingContainer other) {
        return other instanceof Meter && ((Meter) other).getId() == getId();
    }

    @Override
    public Optional<Meter> getMeter(Instant instant) {
        return Optional.of(this);
    }

    @Override
    public Optional<UsagePoint> getUsagePoint(Instant instant) {
        return getMeterActivation(instant).flatMap(MeterActivation::getUsagePoint);
    }

    @Override
    public ZoneId getZoneId() {
        return getCurrentMeterActivation()
                .map(MeterActivation::getZoneId)
                .orElse(ZoneId.systemDefault());
    }
}
