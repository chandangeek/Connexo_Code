package com.elster.jupiter.messaging.impl;

import com.elster.jupiter.transaction.TransactionService;

import java.sql.Connection;
import java.sql.SQLException;

public class Bus {
	static final String COMPONENTNAME = "MSG";
	
	private static ServiceLocator locator;
	
	static void setServiceLocator(ServiceLocator serviceLocator) {
		locator = serviceLocator;
	}
	
	static Connection getConnection() throws SQLException {
		return locator.getConnection();
	}
	
	static OrmClient getOrmClient() {
		return locator.getOrmClient();
	}

	public static TransactionService getTransactionService() {
		return locator.getTransactionService();
	}

    static void fire(Object event) {
        locator.getPublisher().publish(event);
    }

    static AQFacade getAQFacade() {
        return locator.getAQFacade();
    }
}
