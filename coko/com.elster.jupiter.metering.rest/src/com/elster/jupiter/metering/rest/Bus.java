package com.elster.jupiter.metering.rest;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.rest.util.RestQueryService;

public class Bus {
	private static volatile ServiceLocator locator;
	
	static void setServiceLocator(ServiceLocator serviceLocator) {
		locator = serviceLocator;
	}
	
	static ServiceLocator getServiceLocator() {
		return locator;
	}
	
	static MeteringService getMeteringService() {
		return locator.getMeteringService();
	}
	
	static RestQueryService getQueryService() {
		return locator.getQueryService();
	}
	
	private Bus() {
		throw new UnsupportedOperationException();
	}
	
}
