package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;

import java.util.Map;

public interface ServiceLocator {
    OrmClient getOrmClient();

    ComponentCache getComponentCache();

    EventService getEventService();

    MeteringService getMeteringService();

    ValidationService getValidationService();

    Validator getValidator(String implementation, Map<String, Quantity> props);

    Clock getClock();
}
