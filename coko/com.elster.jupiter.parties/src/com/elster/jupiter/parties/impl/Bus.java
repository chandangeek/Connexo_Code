package com.elster.jupiter.parties.impl;

import com.elster.jupiter.orm.cache.ComponentCache;

class Bus {
	
	static final String COMPONENTNAME = "PRT";
	
	private static ServiceLocator locator;
	
	public static void setServiceLocator(ServiceLocator serviceLocator) {
		locator = serviceLocator;
	}
	
	public static OrmClient getOrmClient() {
		return locator.getOrmClient();
	}
	
	public static ComponentCache getCache() {
		return locator.getCache();
	}
}
