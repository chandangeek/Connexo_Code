package com.elster.jupiter.metering.cim.impl;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageGenerator {

    private final Sender sender;
    private final Clock clock;
    private final ObjectFactory objectFactory = new ObjectFactory();

    public MessageGenerator(Sender sender, Clock clock) {
        this.sender = sender;
        this.clock = clock;
    }

    public void generateMessage(com.elster.jupiter.metering.Meter meter, Interval interval) {
        MeterActivation meterActivation = meter.getCurrentMeterActivation().get();

        CreatedMeterReadings message = objectFactory.createCreatedMeterReadings();
        message.setHeader(createHeader());
        message.setPayload(createPayLoadType());
        MeterReadings meterReadings = createMeterReadings();
        MeterReading meterReading = createMeterReading(meterReadings, createMeter(meter));
        for (Channel channel : meterActivation.getChannels()) {
            addBaseReadings(meterReading, channel.getIntervalReadings(interval));
            addBaseReadings(meterReading, channel.getRegisterReadings(interval));
        }

        sender.send(message, meterReadings);
    }

    private PayloadType createPayLoadType() {
        PayloadType payloadType = new PayloadType();
        payloadType.any = new ArrayList<>();
        return payloadType;
    }

    void addBaseReadings(MeterReading meterReading, List<? extends BaseReadingRecord> intervalReadings) {
        for (BaseReadingRecord intervalReading : intervalReadings) {
            for (com.elster.jupiter.metering.ReadingType readingType : intervalReading.getReadingTypes()) {
                Reading.ReadingType type = createReadingType(readingType);
                createReading(meterReading, intervalReading, type);
            }
        }
    }

    HeaderType createHeader() {
        HeaderType header = new HeaderType();
        header.setVerb("created");
        header.setNoun("MeterReadings");
        header.setRevision("");
        header.setContext("");
        header.setTimestamp(clock.now());
        header.setSource("MDM");
        header.setAsyncReplyFlag(false);
        header.setAckRequired(false);
        header.setMessageID(UUID.randomUUID().toString());
        header.setCorrelationID("");
        header.setComment("");

        return header;
    }

    MeterReadings createMeterReadings() {
        MeterReadings meterReadings = objectFactory.createMeterReadings();
        meterReadings.meterReading = new ArrayList<MeterReading>();
        return meterReadings;


    }

    MeterReading createMeterReading(MeterReadings meterReadings, Meter meter) {
        MeterReading meterReading = objectFactory.createMeterReading();
        meterReading.setMeter(meter);
        meterReading.readings = new ArrayList<Reading>();
        meterReadings.meterReading.add(meterReading);
        return meterReading;
    }

    Meter createMeter(com.elster.jupiter.metering.Meter meter) {
        Meter value = objectFactory.createMeter();
        value.setMRID(meter.getMRID());
        return value;
    }

    Reading createReading(MeterReading meterReading, BaseReading baseReading, Reading.ReadingType readingType) {
        Reading reading = objectFactory.createReading();
        reading.setReadingType(readingType);
        reading.setReportedDateTime(baseReading.getTimeStamp());
        reading.setValue(baseReading.getValue().toString());
        meterReading.readings.add(reading);
        return reading;
    }

    Reading.ReadingType createReadingType(ReadingType type) {
        Reading.ReadingType readingType = objectFactory.createReadingReadingType();
        readingType.setRef(type.getMRID());
        return readingType;
    }
}
