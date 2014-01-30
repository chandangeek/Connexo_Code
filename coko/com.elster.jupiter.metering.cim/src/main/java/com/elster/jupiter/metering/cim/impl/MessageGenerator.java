package com.elster.jupiter.metering.cim.impl;

import ch.iec.tc57._2011.meterreadings_.Meter;
import ch.iec.tc57._2011.meterreadings_.MeterReading;
import ch.iec.tc57._2011.meterreadings_.MeterReadings;
import ch.iec.tc57._2011.meterreadings_.Reading;
import ch.iec.tc57._2011.schema.message.CreatedMeterReadings;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ObjectFactory;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
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
    private final ch.iec.tc57._2011.meterreadings_.ObjectFactory payloadObjectFactory = new ch.iec.tc57._2011.meterreadings_.ObjectFactory();

    public MessageGenerator(Sender sender, Clock clock) {
        this.sender = sender;
        this.clock = clock;
    }

    public void generateMessage(com.elster.jupiter.metering.Meter meter, Interval interval) {
        MeterActivation meterActivation = meter.getCurrentMeterActivation().get();

        CreatedMeterReadings message = messageObjectFactory.createCreatedMeterReadings();
        message.setHeader(createHeader());
        message.setPayload(messageObjectFactory.createPayloadType());


        MeterReadings meterReadings = payloadObjectFactory.createMeterReadings();
        MeterReading meterReading = createMeterReading(meterReadings, createMeter(meter));
        for (Channel channel : meterActivation.getChannels()) {
            addBaseReadings(meterReading, channel.getIntervalReadings(interval));
            addBaseReadings(meterReading, channel.getRegisterReadings(interval));
        }

        sender.send(message, meterReadings);
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
