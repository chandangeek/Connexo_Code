package com.elster.jupiter.metering.cim.impl;

import ch.iec.tc57._2011.meterreadings_.ObjectFactory;
import ch.iec.tc57._2011.meterreadings_.Meter;
import ch.iec.tc57._2011.meterreadings_.MeterReading;
import ch.iec.tc57._2011.meterreadings_.MeterReadings;
import ch.iec.tc57._2011.meterreadings_.Reading;
import ch.iec.tc57._2011.schema.message.HeaderType;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Interval;
import org.osgi.service.event.EventConstants;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class MeterReadingCreatedMessageHandler implements MessageHandler {

    private final JsonService jsonService;
    private final MeteringService meteringService;
    private final MessageGenerator messageGenerator;
    private ObjectFactory payloadObjectFactory = new ObjectFactory();

    public MeterReadingCreatedMessageHandler(JsonService jsonService, MeteringService meteringService, MessageGenerator generator) {
        this.jsonService = jsonService;
        this.meteringService = meteringService;
        this.messageGenerator = generator;
    }

    @Override
    public void process(Message message) {
        Map<?, ?> map = jsonService.deserialize(message.getPayload(), Map.class);
        Object topic = map.get(EventConstants.EVENT_TOPIC);
        if ("com/elster/jupiter/metering/meterreading/CREATED".equals(topic)) {
            handleCreatedMessage(map);
        }
    }

    private void handleCreatedMessage(Map<?, ?> map) {
        Long start = getLong(map, "start");
        Long end = getLong(map, "end");
        Long meterId = getLong(map, "meterId");

        Interval interval = new Interval(new Date(start), new Date(end));
        messageGenerator.generateMessage(meteringService.findMeter(meterId).get(), interval);
    }

    private Long getLong(Map<?, ?> map, String key) {
        Object contents = map.get(key);
        return contents instanceof Long ? (Long) contents : ((Integer) contents).longValue();
    }

    public void generateMessage(com.elster.jupiter.metering.Meter meter, Interval interval) {

        messageGenerator.generateMessage(meter, interval);
    }

    private void addBaseReadings(MeterReading meterReading, List<? extends BaseReadingRecord> intervalReadings) {
        messageGenerator.addBaseReadings(meterReading, intervalReadings);
    }

    private HeaderType createHeader() {

        return messageGenerator.createHeader();
    }

    private MeterReadings createMeterReadings() {
        return payloadObjectFactory.createMeterReadings();
    }

    private MeterReading createMeterReading(MeterReadings meterReadings, Meter meter) {
        return messageGenerator.createMeterReading(meterReadings, meter);
    }

    private Meter createMeter(com.elster.jupiter.metering.Meter meter) {
        return messageGenerator.createMeter(meter);
    }

    private Reading createReading(MeterReading meterReading, BaseReading baseReading, Reading.ReadingType readingType) {
        return messageGenerator.createReading(meterReading, baseReading, readingType);
    }

    private Reading.ReadingType createReadingType(com.elster.jupiter.metering.ReadingType type) {
        return messageGenerator.createReadingType(type);
    }


}
