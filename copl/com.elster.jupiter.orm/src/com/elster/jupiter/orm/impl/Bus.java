package com.elster.jupiter.orm.impl;

import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;

class Bus {
	final static String COMPONENTNAME = "ORM";
	final static int CATALOGNAMELIMIT = 30;
	
	private static volatile ServiceLocator locator;
	
	static ServiceLocator getServiceLocator() {
		return locator;
	}
	
	static void setServiceLocator(ServiceLocator locator) {
		Bus.locator = locator;
	}
	
	static OrmClient getOrmClient() {
		return getServiceLocator().getOrmClient();
	}
	
	static Connection getConnection(boolean transactionRequired) throws SQLException {
		return getServiceLocator().getConnection(transactionRequired);
	}
	
	static Principal getPrincipal() {
		return getServiceLocator().getPrincipal();
	}
	
	// helper methods
	
	private final static String[] trueStrings = { "1" , "y" ,"yes" , "on" };
	
	static boolean toBoolean(String in) {
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
	
}
