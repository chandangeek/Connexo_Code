package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.transaction.SqlEvent;

enum Bus {
    ;

	private static ServiceLocator locator;
	
	static void setServiceLocator(ServiceLocator locator) {
		Bus.locator = locator;
	}
	
	static ServiceLocator getServiceLocator() {
		return Bus.locator;
	}
	
	static void publish(SqlEvent event) {
		locator.publish(event);
	}
}
