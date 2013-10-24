package com.elster.jupiter.events.rest.impl;

import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.rest.impl.Bus;
import com.elster.jupiter.events.rest.impl.ServiceLocator;

enum Bus {
    ;

    private static volatile ServiceLocator locator;

    static void setServiceLocator(ServiceLocator locator) {
        Bus.locator = locator;
    }

    public static RestQueryService getRestQueryService() {
        return locator.getRestQueryService();
    }

    public static TransactionService getTransactionService() {
        return locator.getTransactionService();
    }

    public static EventService getEventService() {
        return locator.getEventService();
    }

}
