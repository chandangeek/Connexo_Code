package com.elster.jupiter.orm.internal;

import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;

import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class Bus {
	public static final String COMPONENTNAME = "ORM";
	public static final int CATALOGNAMELIMIT = 30;

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
		return getLocator().getConnection(transactionRequired);
	}
	
	public static Principal getPrincipal() {
		return getLocator().getPrincipal();
	}

    public static Clock getClock() {
        return getLocator().getClock();
    }

    public static JsonService getJsonService() {
        return getLocator().getJsonService();
    }

    // helper methods
	
	private static final String[] trueStrings = { "1" , "y" ,"yes" , "on" };
	
	public static boolean toBoolean(String in) {
		for (String each : trueStrings) {
			if (each.equalsIgnoreCase(in)) 
				return true; 
		}
		return Boolean.valueOf(in);
	}
	
	// pure static class;
	private Bus() {
		throw new UnsupportedOperationException();
	}

	public static Table getTable(String component, String tableName) {
		return getLocator().getTable(component, tableName);
	}

    private static ServiceLocator getLocator() {
        return locatorHolder.get();
    }

}
