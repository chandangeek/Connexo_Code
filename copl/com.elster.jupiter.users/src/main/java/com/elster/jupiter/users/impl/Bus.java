package com.elster.jupiter.users.impl;

import com.elster.jupiter.transaction.TransactionService;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

enum Bus {
    ;

	static final String COMPONENTNAME = "USR";
	static final String REALM = "Jupiter";

    private static AtomicReference<ServiceLocator> locatorHolder = new AtomicReference<>();

    public static void setServiceLocator(ServiceLocator locator) {
        Bus.locatorHolder.set(Objects.requireNonNull(locator));
    }

    public static void clearServiceLocator(ServiceLocator old) {
        locatorHolder.compareAndSet(Objects.requireNonNull(old), null);
    }

    static OrmClient getOrmClient() {
		return getLocator().getOrmClient();
	}

    static TransactionService getTransactionService() {
        return getLocator().getTransactionService();
    }


    private static ServiceLocator getLocator() {
        return locatorHolder.get();
    }

}
