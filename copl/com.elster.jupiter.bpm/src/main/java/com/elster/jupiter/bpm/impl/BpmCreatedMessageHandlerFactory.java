package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(name="com.elster.jupiter.bpm.queue", service = MessageHandlerFactory.class, property = {"subscriber=BpmQueueSubsc", "destination=BpmQueueDest"}, immediate = true)
public class BpmCreatedMessageHandlerFactory implements MessageHandlerFactory {

        private volatile JsonService jsonService;
        private volatile Clock clock;

        @Override
        public MessageHandler newMessageHandler() {
            return new BpmCreatedMessageHandler(jsonService);
        }

        @Activate
        public void activate() {

        }

        @Deactivate
        public void deactivate() {

        }

        @Reference
        public void setJsonService(JsonService jsonService) {
            this.jsonService = jsonService;
        }

        @Reference
        public void setClock(Clock clock) {
            this.clock = clock;
        }
}