package com.elster.jupiter.users.impl;

import com.elster.jupiter.transaction.TransactionService;

enum Bus {
    ;

	public static final String COMPONENTNAME = "USR";
	
	private static volatile ServiceLocator locator;
	
	static void setServiceLocator(ServiceLocator locator) {
		Bus.locator = locator;
	}
	
	static OrmClient getOrmClient() {
		return locator.getOrmClient();
	}

    static TransactionService getTransactionService() {
        return locator.getTransactionService();
    }

}
