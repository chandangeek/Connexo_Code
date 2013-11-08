package com.elster.jupiter.ids.plumbing;

import com.elster.jupiter.util.time.Clock;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class Bus {
	
	public static final String COMPONENTNAME = "IDS";

    private static AtomicReference<ServiceLocator> locatorHolder = new AtomicReference<>();

    public static void setServiceLocator(ServiceLocator locator) {
        Bus.locatorHolder.set(Objects.requireNonNull(locator));
    }

    public static void clearServiceLocator(ServiceLocator old) {
        locatorHolder.compareAndSet(Objects.requireNonNull(old), null);
    }

    public static OrmClient getOrmClient() {
		return getLocator().getOrmClient();
	}
	
	public static Connection getConnection(boolean transactionRequired) throws SQLException {
		return getOrmClient().getConnection(transactionRequired);
	}

    public static Clock getClock() {
        return getLocator().getClock();
    }

    private static ServiceLocator getLocator() {
        return locatorHolder.get();
    }

    // pure static class;
	private Bus() {
		throw new UnsupportedOperationException();
	}	

}
