package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public enum Bus {
    ;

    public static final String COMPONENTNAME = "VAL";

    private static AtomicReference<ServiceLocator> locatorHolder = new AtomicReference<>();

    public static void setServiceLocator(ServiceLocator locator) {
        Bus.locatorHolder.set(Objects.requireNonNull(locator));
    }

    public static void clearServiceLocator(ServiceLocator old) {
        locatorHolder.compareAndSet(Objects.requireNonNull(old), null);
    }

    private static ServiceLocator getLocator() {
        return locatorHolder.get();
    }

    public static OrmClient getOrmClient() {
        return getLocator().getOrmClient();
    }

    public static EventService getEventService() {
        return getLocator().getEventService();
    }

    public static MeteringService getMeteringService() {
        return getLocator().getMeteringService();
    }

    public static ValidationService getValidationService() {
        return getLocator().getValidationService();
    }

    public static Validator getValidator(String implementation, Map<String, Quantity> props) {
        return getLocator().getValidator(implementation, props);
    }

    public static Clock getClock() {
        return getLocator().getClock();
    }
}
