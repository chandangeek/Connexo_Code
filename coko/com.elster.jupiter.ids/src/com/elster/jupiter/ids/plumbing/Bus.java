package com.elster.jupiter.ids.plumbing;

import java.sql.Connection;
import java.sql.SQLException;

public class Bus {
	
	public static final String COMPONENTNAME = "IDS";
	
	private static volatile ServiceLocator locator;
	
	public static void setServiceLocator(ServiceLocator locator) {
		Bus.locator = locator;
	}
	
	public static OrmClient getOrmClient() {
		return locator.getOrmClient();
	}
	
	public static Connection getConnection(boolean transactionRequired) throws SQLException {
		return getOrmClient().getConnection(transactionRequired);
	}

	// pure static class;
	private Bus() {
		throw new UnsupportedOperationException();
	}	

}
