package com.elster.jupiter.orm.internal;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.util.time.Clock;

import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;

public class Bus {
	public final static String COMPONENTNAME = "ORM";
	public final static int CATALOGNAMELIMIT = 30;
	
	private static volatile ServiceLocator locator;
	
	public static void setServiceLocator(ServiceLocator locator) {
		Bus.locator = locator;
	}
	
	public static OrmClient getOrmClient() {
		return locator.getOrmClient();
	}
	
	public static Connection getConnection(boolean transactionRequired) throws SQLException {
		return locator.getConnection(transactionRequired);
	}
	
	public static Principal getPrincipal() {
		return locator.getPrincipal();
	}

    public static Clock getClock() {
        return locator.getClock();
    }
    // helper methods
	
	private final static String[] trueStrings = { "1" , "y" ,"yes" , "on" };
	
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
		return locator.getTable(component,tableName);
	}	
	
}
