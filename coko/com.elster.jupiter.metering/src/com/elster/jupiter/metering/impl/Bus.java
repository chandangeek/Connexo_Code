package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.IdsService;

class Bus {
	
	static final String COMPONENTNAME = "MTR";
	
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

	static IdsService getIdsService() {
		return getServiceLocator().getIdsService();
	}
	
	// pure static class;
	private Bus() {
		throw new UnsupportedOperationException();
	}	

}
