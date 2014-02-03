package com.elster.jupiter.metering.cim.impl;

import ch.iec.tc57._2011.meterreadings_.Meter;
import ch.iec.tc57._2011.meterreadings_.MeterReading;
import ch.iec.tc57._2011.meterreadings_.MeterReadings;
import ch.iec.tc57._2011.meterreadings_.ObjectFactory;
import ch.iec.tc57._2011.meterreadings_.Reading;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.util.time.Interval;

import java.util.List;

public class MeterReadingsGenerator {

    final ObjectFactory payloadObjectFactory = new ObjectFactory();

    public MeterReadingsGenerator() {
    }

    public MeterReadings createMeterReadings(MeterActivation meterActivation, Interval interval) {
        MeterReadings meterReadings = payloadObjectFactory.createMeterReadings();
        addMeterReadings(meterReadings, meterActivation, interval);
        return meterReadings;
    }

    public void addMeterReadings(MeterReadings meterReadings, MeterActivation meterActivation, Interval interval) {
        MeterReading meterReading = createMeterReading(meterReadings, createMeter(meterActivation.getMeter().get()));
        for (Channel channel : meterActivation.getChannels()) {
            addBaseReadings(meterReading, getReadings(channel, interval));
        }
    }

    private List<? extends BaseReadingRecord> getReadings(Channel channel, Interval interval) {
        if (channel.isRegular()) {
            return channel.getIntervalReadings(interval);
        }
        return channel.getRegisterReadings(interval);
    }

    void addBaseReadings(MeterReading meterReading, List<? extends BaseReadingRecord> intervalReadings) {
        for (BaseReadingRecord baseReading : intervalReadings) {
            addReadingsPerReadingType(meterReading, baseReading);
        }
    }

    private void addReadingsPerReadingType(MeterReading meterReading, BaseReadingRecord baseReading) {
        for (ReadingType readingType : baseReading.getReadingTypes()) {
            createReading(meterReading, baseReading, createReadingType(readingType));
        }
    }

    MeterReading createMeterReading(MeterReadings meterReadings, Meter meter) {
        MeterReading meterReading = payloadObjectFactory.createMeterReading();
        meterReading.setMeter(meter);
        meterReadings.getMeterReading().add(meterReading);
        return meterReading;
    }

    Meter createMeter(com.elster.jupiter.metering.Meter meter) {
        Meter value = payloadObjectFactory.createMeter();
        value.setMRID(meter.getMRID());
        return value;
    }

    Reading createReading(MeterReading meterReading, BaseReading baseReading, Reading.ReadingType readingType) {
        Reading reading = payloadObjectFactory.createReading();
        reading.setReadingType(readingType);
        reading.setReportedDateTime(baseReading.getTimeStamp());
        reading.setValue(baseReading.getValue().toString());
        meterReading.getReadings().add(reading);
        return reading;
    }

    Reading.ReadingType createReadingType(ReadingType type) {
        Reading.ReadingType readingType = payloadObjectFactory.createReadingReadingType();
        readingType.setRef(type.getMRID());
        return readingType;
    }
}