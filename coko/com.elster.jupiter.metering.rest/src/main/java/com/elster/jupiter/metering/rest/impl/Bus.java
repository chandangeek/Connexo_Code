package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public enum Bus {
	;

    private static AtomicReference<ServiceLocator> locatorHolder = new AtomicReference<>();

    public static void setServiceLocator(ServiceLocator locator) {
        Bus.locatorHolder.set(Objects.requireNonNull(locator));
    }

    public static void clearServiceLocator(ServiceLocator old) {
        locatorHolder.compareAndSet(Objects.requireNonNull(old), null);
    }

    public static TransactionService getTransactionService() {
        return getLocator().getTransactionService();
    }

	static MeteringService getMeteringService() {
		return getLocator().getMeteringService();
	}

    static RestQueryService getQueryService() {
		return getLocator().getQueryService();
	}

    private static ServiceLocator getLocator() {
        return locatorHolder.get();
    }
}
