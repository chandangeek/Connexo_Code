package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;

public interface ServiceLocator {
    OrmClient getOrmClient();

    ComponentCache getComponentCache();

    EventService getEventService();

    MeteringService getMeteringService();

    ValidationService getValidationService();

    Validator getValidator(String implementation);
}
