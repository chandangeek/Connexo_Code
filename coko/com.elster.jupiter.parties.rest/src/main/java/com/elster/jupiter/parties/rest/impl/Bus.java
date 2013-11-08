package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

enum Bus {
    ;
    private static AtomicReference<ServiceLocator> locatorHolder = new AtomicReference<>();

    public static void setServiceLocator(ServiceLocator locator) {
        Bus.locatorHolder.set(Objects.requireNonNull(locator));
    }

    public static void clearServiceLocator(ServiceLocator old) {
        locatorHolder.compareAndSet(Objects.requireNonNull(old), null);
    }

    public static RestQueryService getQueryService() {
        return getLocator().getQueryService();
    }

    public static TransactionService getTransactionService() {
        return getLocator().getTransactionService();
    }

    public static PartyService getPartyService() {
        return getLocator().getPartyService();
    }

    public static UserService getUserService() {
        return getLocator().getUserService();
    }

    public static Clock getClock() {
        return getLocator().getClock();
    }

    private static ServiceLocator getLocator() {
        return locatorHolder.get();
    }
}
