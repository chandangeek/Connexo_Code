package com.elster.jupiter.users.impl;

class Bus {	
	public static final String COMPONENTNAME = "USR";
	
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
		
	// pure static class;
	private Bus() {
		throw new UnsupportedOperationException();
	}	

}
