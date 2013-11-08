package com.elster.jupiter.messaging.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public enum Bus {
    ;

	static final String COMPONENTNAME = "MSG";

    private static AtomicReference<ServiceLocator> locatorHolder = new AtomicReference<>();

    public static void setServiceLocator(ServiceLocator locator) {
        Bus.locatorHolder.set(Objects.requireNonNull(locator));
    }

    public static void clearServiceLocator(ServiceLocator old) {
        locatorHolder.compareAndSet(Objects.requireNonNull(old), null);
    }

    static Connection getConnection() throws SQLException {
		return locatorHolder.get().getConnection();
	}
	
	static OrmClient getOrmClient() {
		return locatorHolder.get().getOrmClient();
	}

	public static TransactionService getTransactionService() {
		return locatorHolder.get().getTransactionService();
	}

    static void fire(Object event) {
        locatorHolder.get().getPublisher().publish(event);
    }

    static AQFacade getAQFacade() {
        return locatorHolder.get().getAQFacade();
    }

    public static ThreadPrincipalService getThreadPrincipalService() {
        return locatorHolder.get().getThreadPrincipalService();
    }
}
