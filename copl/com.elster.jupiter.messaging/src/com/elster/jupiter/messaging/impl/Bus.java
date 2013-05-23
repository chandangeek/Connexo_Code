package com.elster.jupiter.messaging.impl;

import java.sql.Connection;
import java.sql.SQLException;

public class Bus {
	
	private static ServiceLocator locator;
	
	static void setServiceLocator(ServiceLocator serviceLocator) {
		locator = serviceLocator;
	}
	
	static Connection getConnection() throws SQLException {
		return locator.getConnection();
	}
}
