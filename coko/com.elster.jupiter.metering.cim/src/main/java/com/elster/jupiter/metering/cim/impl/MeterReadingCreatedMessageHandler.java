package com.elster.jupiter.metering.cim.impl;

import ch.iec.tc57._2011.meterreadings.ObjectFactory;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.json.JsonService;
import com.google.common.collect.Range;
import org.osgi.service.event.EventConstants;

import java.time.Instant;
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
        Range<Instant> range = Range.closed(Instant.ofEpochMilli(start), Instant.ofEpochMilli(end));
        messageGenerator.generateMessage(meteringService.findMeter(meterId).get(), range);
    }

    private Long getLong(Map<?, ?> map, String key) {
        Object contents = map.get(key);
        return contents instanceof Long ? (Long) contents : ((Integer) contents).longValue();
    }

}
