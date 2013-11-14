package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.validation.ValidationService;

public interface ServiceLocator {
    OrmClient getOrmClient();

    ComponentCache getComponentCache();

    EventService getEventService();

    ValidationService getValidationService();
}
