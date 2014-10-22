package com.energyict.mdc.device.data.impl;


import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.collections.DualIterable;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.Range;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;

import java.time.Instant;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Provides an implementation of a Register of a {@link com.energyict.mdc.device.data.Device},
 * which is actually a wrapping around a {@link com.energyict.mdc.device.config.RegisterSpec}
 * of the {@link com.energyict.mdc.device.config.DeviceConfiguration}
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/14
 * Time: 11:19
 */
public abstract class RegisterImpl<R extends Reading> implements Register<R> {

    /**
     * The {@link RegisterSpec} for which this Register is serving
     */
    private final RegisterSpec registerSpec;
    /**
     * The Device which <i>owns</i> this Register
     */
    private final DeviceImpl device;

    public RegisterImpl(DeviceImpl device, RegisterSpec registerSpec) {
        this.registerSpec = registerSpec;
        this.device = device;
    }

    @Override
    public Device getDevice() {
        return device;
    }

    @Override
    public RegisterSpec getRegisterSpec() {
        return registerSpec;
    }

    @Override
    public Optional<R> getReading(Date timestamp) {
        return this.getReading(timestamp.toInstant());
    }

    private Optional<R> getReading(Instant timestamp) {
        List<R> atMostOne = this.getReadings(Range.singleton(timestamp));
        if (atMostOne.isEmpty()) {
            return Optional.empty();
        }
        else {
            return Optional.of(atMostOne.get(0));
        }
    }

    @Override
    public List<R> getReadings(Interval interval) {
        return this.getReadings(interval.toOpenClosedRange());
    }

    private List<R> getReadings(Range<Instant> interval) {
        List<ReadingRecord> koreReadings = this.device.getReadingsFor(this, interval);
        List<List<ReadingQuality>> readingQualities = this.getReadingQualities(koreReadings);
        return this.toReadings(koreReadings, readingQualities);
    }

    private List<List<ReadingQuality>> getReadingQualities(List<ReadingRecord> koreReadings) {
        List<List<ReadingQuality>> readingQualities = new ArrayList<>(koreReadings.size());
        // Todo: call validationService, create empty list for each kore reading until API is available
        for (int i = 0; i < koreReadings.size(); i++) {
            readingQualities.add(Collections.<ReadingQuality>emptyList());
        }
        return readingQualities;
    }

    private List<R> toReadings(List<ReadingRecord> koreReadings, List<List<ReadingQuality>> readingQualities) {
        List<R> readings = new ArrayList<>(koreReadings.size());
        for (Pair<ReadingRecord, List<ReadingQuality>> koreReadingAndQuality : DualIterable.endWithShortest(koreReadings, readingQualities)) {
            readings.add(this.toReading(koreReadingAndQuality));
        }
        return readings;
    }

    private R toReading (Pair<ReadingRecord, List<ReadingQuality>> koreReadingAndQuality) {
        if (koreReadingAndQuality.getLast().isEmpty()) {
            return this.newUnvalidatedReading(koreReadingAndQuality.getFirst());
        }
        else {
            return this.newValidatedReading(koreReadingAndQuality.getFirst(), koreReadingAndQuality.getLast());
        }
    }

    protected abstract R newUnvalidatedReading(ReadingRecord actualReading);

    protected abstract R newValidatedReading(ReadingRecord actualReading, List<ReadingQuality> readingQualities);

    @Override
    public Optional<R> getLastReading() {
        return this.device.getLastReadingFor(this).map(this::toReading);
    }

    private R toReading(ReadingRecord readingRecord) {
        List<ReadingQuality> readingQualities = this.getReadingQualities(Arrays.asList(readingRecord)).get(0);
        return this.toReading(Pair.of(readingRecord, readingQualities));
    }

    @Override
    public Optional<Date> getLastReadingDate() {
        return this.getLastReading().map(Reading::getTimeStamp);
    }

    @Override
    public ObisCode getRegisterTypeObisCode() {
        return getRegisterSpec().getRegisterType().getObisCode();
    }

    @Override
    public ObisCode getRegisterSpecObisCode() {
        return getRegisterSpec().getObisCode();
    }

    @Override
    public ObisCode getDeviceObisCode() {
        return getRegisterSpec().getDeviceObisCode();
    }

    @Override
    public long getRegisterSpecId() {
        return getRegisterSpec().getId();
    }

    @Override
    public ReadingType getReadingType() {
        return getRegisterSpec().getReadingType();
    }
}
