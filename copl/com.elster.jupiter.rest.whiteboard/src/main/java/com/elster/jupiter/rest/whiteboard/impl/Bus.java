package com.elster.jupiter.rest.whiteboard.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;

class Bus {
	
	static final String PID = "com.elster.jupiter.rest.whiteboard";
	
	private static volatile ServiceLocator locator;
	
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

    static void fire(Object event) {
        locator.getPublisher().publish(event);
    }

	private Bus() {
		throw new UnsupportedOperationException();
	}
	
}

	