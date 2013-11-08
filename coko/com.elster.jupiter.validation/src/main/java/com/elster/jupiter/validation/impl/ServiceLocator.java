package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.cache.ComponentCache;

public interface ServiceLocator {
    OrmClient getOrmClient();

    ComponentCache getComponentCache();

    EventService getEventService();
}
