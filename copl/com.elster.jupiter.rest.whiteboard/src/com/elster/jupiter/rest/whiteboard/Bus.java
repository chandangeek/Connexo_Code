package com.elster.jupiter.rest.whiteboard;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;

class Bus {
	private static ServiceLocator locator;
	
	static ServiceLocator getServiceLocator() {
		return locator;
	}
	
	static void setServiceLocator(ServiceLocator serviceLocator) {
		locator =  serviceLocator;
	}
	
	static UserService getUserService() {
		return locator.getUserService();
	}
	
	static ThreadPrincipalService getThreadPrincipalService() {
		return locator.getThreadPrincipalService();
	}
	
	private Bus() {
		throw new UnsupportedOperationException();
	}
	
}

	