package com.elster.jupiter.ids.impl;

import java.sql.Connection;
import java.sql.SQLException;

class Bus {
	
	static final String COMPONENTNAME = "IDS";
	
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
		return getOrmClient().getConnection(transactionRequired);
	}

	// pure static class;
	private Bus() {
		throw new UnsupportedOperationException();
	}	

}
