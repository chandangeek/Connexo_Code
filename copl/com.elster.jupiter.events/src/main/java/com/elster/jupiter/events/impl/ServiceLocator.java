package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import org.osgi.service.event.EventAdmin;

public interface ServiceLocator {

    ComponentCache getComponentCache();

    Clock getClock();

    EventAdmin getEventAdmin();

    Publisher getPublisher();

    BeanService getBeanService();

    JsonService getJsonService();

    MessageService getMessageService();

    EventConfiguration getEventConfiguration();

    OrmClient getOrmClient();

    EventService getEventService();
}
