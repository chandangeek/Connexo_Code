package com.elster.jupiter.metering.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

class MeteringApplication extends Application {
	private final Set<Class<?>> classes = new HashSet<>();
	
	MeteringApplication() {
		classes.add(MeteringResource.class);
	}

	public Set<Class<?>> getClasses() {
		return classes;
	}
	
}
