package com.elster.jupiter.metering.cim.impl;

import ch.iec.tc57._2011.meterreadings.MeterReadings;
import ch.iec.tc57._2011.schema.message.CreatedMeterReadings;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ObjectFactory;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.cim.Sender;
import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
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

    public void generateMessage(com.elster.jupiter.metering.Meter meter, Range<Instant> range) {
        MeterActivation meterActivation = meter.getCurrentMeterActivation().get();

        CreatedMeterReadings message = messageObjectFactory.createCreatedMeterReadings();
        message.setHeader(createHeader());
        message.setPayload(messageObjectFactory.createPayloadType());


        MeterReadings meterReadings = meterReadingsGenerator.createMeterReadings(meterActivation, range);

        sender.send(message, meterReadings);
    }

    HeaderType createHeader() {
        HeaderType header = messageObjectFactory.createHeaderType();
        header.setVerb("created");
        header.setNoun("MeterReadings");
        header.setRevision("");
        header.setContext("");
        header.setTimestamp(Date.from(clock.instant()));
        header.setSource("MDM");
        header.setAsyncReplyFlag(false);
        header.setAckRequired(false);
        header.setMessageID(UUID.randomUUID().toString());
        header.setCorrelationID("");
        header.setComment("");

        return header;
    }

}
