package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;

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

    public static RestQueryService getRestQueryService() {
        return getLocator().getRestQueryService();
    }

    public static TransactionService getTransactionService() {
        return getLocator().getTransactionService();
    }

    public static UserService getUserService() {
        return getLocator().getUserService();
    }

    private static ServiceLocator getLocator() {
        return locatorHolder.get();
    }

}
