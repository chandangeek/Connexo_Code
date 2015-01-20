package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.json.JsonService;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import javax.inject.Inject;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.spy;

public class SpyEventService implements EventService {
    private final EventService eventService;

    @Inject
    public SpyEventService(Clock clock, JsonService jsonService, Publisher publisher, BeanService beanService, OrmService ormService, MessageService messageService, BundleContext bundleContext, EventAdmin eventAdmin, NlsService nlsService) {
        eventService = spy(new EventServiceImpl(clock, jsonService, publisher, beanService, ormService, messageService, bundleContext, eventAdmin, nlsService));
    }

    @Override
    public void postEvent(String topic, Object source) {
        eventService.postEvent(topic, source);
    }

    @Override
    @TransactionRequired
    public EventTypeBuilder buildEventTypeWithTopic(String topic) {
        return eventService.buildEventTypeWithTopic(topic);
    }

    @Override
    public List<EventType> getEventTypes() {
        return eventService.getEventTypes();
    }

    @Override
    public Optional<EventType> getEventType(String topic) {
        return eventService.getEventType(topic);
    }

    @Override
    public List<EventType> getEventTypesForComponent(String component) {
        return eventService.getEventTypesForComponent(component);
    }

    public EventService getSpy() {
        return eventService;
    }

}
