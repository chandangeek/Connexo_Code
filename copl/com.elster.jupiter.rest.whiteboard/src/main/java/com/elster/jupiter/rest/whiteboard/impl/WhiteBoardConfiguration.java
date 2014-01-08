package com.elster.jupiter.rest.whiteboard.impl;

import java.util.Map;

class WhiteBoardConfiguration {
	private static final String DEBUG = "debug";
	private static final String LOG = "log";
	private static final String EVENT = "event";
	private static final String AUTHENTICATION = "authentication";
	static final String USERPRINCIPAL = "com.elster.jupiter.userprincipal";
	
	private final boolean debug;
	private final boolean log;
	private final boolean throwEvents;
	private final String authenticationMethod;
	
	private WhiteBoardConfiguration() {
		debug = false;
		log = false;
		throwEvents = false;
		authenticationMethod = null;
	}
	
	private WhiteBoardConfiguration(Map<String,Object> properties) {
		debug = Boolean.TRUE.equals(properties.get(DEBUG));
		log = Boolean.TRUE.equals(properties.get(LOG));
		throwEvents = Boolean.TRUE.equals(properties.get(EVENT));
		authenticationMethod = (String) properties.get(AUTHENTICATION);			
	}		
	
	boolean debug() {
		return debug;
	}
	
	boolean log() {
		return log;
	}
	
	boolean throwEvents() {
		return throwEvents;
	}
	
	String authenticationMethod() {
		return authenticationMethod;
	}
	
	static WhiteBoardConfiguration of(Map<String,Object> props) {
		return props == null ? new WhiteBoardConfiguration() : new WhiteBoardConfiguration(props);
	}


}
