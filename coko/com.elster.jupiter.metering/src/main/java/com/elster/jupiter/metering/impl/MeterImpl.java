package com.elster.jupiter.metering.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.CannotDeleteMeter;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.time.Interval;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    MeterImpl(DataModel dataModel, EventService eventService, Provider<EndDeviceEventRecordImpl> deviceEventFactory,
              MeteringService meteringService, Thesaurus thesaurus, Provider<MeterActivationImpl> meterActivationFactory) {
        super(dataModel, eventService, deviceEventFactory, MeterImpl.class);
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
    public MeterActivationImpl activate(Date start) {
        MeterActivationImpl result = meterActivationFactory.get().init(this, start);
        getDataModel().persist(result);
        meterActivations.add(result);
        return result;
    }

    void adopt(MeterActivationImpl meterActivation) {
        if (!meterActivations.isEmpty()) {
            MeterActivationImpl last = meterActivations.get(meterActivations.size() - 1);
            if (last.getStart().after(meterActivation.getStart())) {
                throw new IllegalArgumentException("Invalid start date");
            } else {
                if (last.getEnd() == null || last.getEnd().after(meterActivation.getStart())) {
                    last.endAt(meterActivation.getStart());
                }
            }
        }
        meterActivations.add(meterActivation);
    }

    @Override
    public Optional<MeterActivation> getCurrentMeterActivation() {
        for (MeterActivation meterActivation : meterActivations) {
            if (meterActivation.isCurrent()) {
                return Optional.of(meterActivation);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<MeterActivation> getMeterActivation(Date when) {
        for (MeterActivation meterActivation : meterActivations) {
            if (meterActivation.isEffective(when)) {
                return Optional.of(meterActivation);
            }
        }
        return Optional.empty();
    }


    @Override
    public List<? extends BaseReadingRecord> getReadings(Interval interval, ReadingType readingType) {
        return MeterActivationsImpl.from(meterActivations, interval).getReadings(interval, readingType);
    }

    @Override
    public Set<ReadingType> getReadingTypes(Interval interval) {
        return MeterActivationsImpl.from(meterActivations, interval).getReadingTypes(interval);
    }

    @Override
    public List<? extends BaseReadingRecord> getReadingsBefore(Date when, ReadingType readingType, int count) {
        return MeterActivationsImpl.from(meterActivations).getReadingsBefore(when, readingType, count);
    }


    @Override
    public List<? extends BaseReadingRecord> getReadingsOnOrBefore(Date when, ReadingType readingType, int count) {
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
}
