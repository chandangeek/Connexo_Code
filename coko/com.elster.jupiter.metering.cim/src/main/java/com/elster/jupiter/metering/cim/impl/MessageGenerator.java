package com.elster.jupiter.metering.cim.impl;

import ch.iec.tc57._2011.meterreadings_.Meter;
import ch.iec.tc57._2011.meterreadings_.MeterReading;
import ch.iec.tc57._2011.meterreadings_.MeterReadings;
import ch.iec.tc57._2011.meterreadings_.Reading;
import ch.iec.tc57._2011.schema.message.CreatedMeterReadings;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ObjectFactory;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;

import java.util.List;
import java.util.UUID;

public class MessageGenerator {

    private final Sender sender;
    private final Clock clock;
    private final ObjectFactory messageObjectFactory = new ObjectFactory();
    private final MeterReadingsGenerator meterReadingsGenerator = new MeterReadingsGenerator();

    public MessageGenerator(Sender sender, Clock clock) {
        this.sender = sender;
        this.clock = clock;
    }

    public void generateMessage(com.elster.jupiter.metering.Meter meter, Interval interval) {
        MeterActivation meterActivation = meter.getCurrentMeterActivation().get();

        CreatedMeterReadings message = messageObjectFactory.createCreatedMeterReadings();
        message.setHeader(createHeader());
        message.setPayload(messageObjectFactory.createPayloadType());


        MeterReadings meterReadings = meterReadingsGenerator.createMeterReadings(meterActivation, interval);

        sender.send(message, meterReadings);
    }

    void addBaseReadings(MeterReading meterReading, List<? extends BaseReadingRecord> intervalReadings) {
        meterReadingsGenerator.addBaseReadings(meterReading, intervalReadings);
    }

    HeaderType createHeader() {
        HeaderType header = messageObjectFactory.createHeaderType();
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

    MeterReading createMeterReading(MeterReadings meterReadings, Meter meter) {
        return meterReadingsGenerator.createMeterReading(meterReadings, meter);
    }

    Meter createMeter(com.elster.jupiter.metering.Meter meter) {
        return meterReadingsGenerator.createMeter(meter);
    }

    Reading createReading(MeterReading meterReading, BaseReading baseReading, Reading.ReadingType readingType) {
        return meterReadingsGenerator.createReading(meterReading, baseReading, readingType);
    }

    Reading.ReadingType createReadingType(ReadingType type) {
        return meterReadingsGenerator.createReadingType(type);
    }
}
