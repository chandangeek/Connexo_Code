package com.elster.jupiter.events.rest.impl;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;

@Component(name = "com.elster.jupiter.event.rest" , service=Application.class , immediate = true , property = {"alias=/evt"} )
public class EventApplication extends Application implements ServiceLocator {
	
	private final Set<Class<?>> classes = new HashSet<>();
	private volatile EventService eventService;
	private volatile TransactionService transactionService;
	private volatile RestQueryService restQueryService;
	
	public EventApplication() {
		classes.add(EventTypeResource.class);		
	}

	public Set<Class<?>> getClasses() {
		return classes;
	}

	@Override
	public EventService getEventService() {
		return eventService;
	}

	@Override
	public TransactionService getTransactionService() {
		return transactionService;
	}

	@Override
	public RestQueryService getRestQueryService() {
		return restQueryService;
	}

	@Reference
	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}
	
	@Reference
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@Reference
	public void setRestQueryService(RestQueryService restQueryService) {
		this.restQueryService = restQueryService;
	}
	
	@Activate
	public void activate() {
		Bus.setServiceLocator(this);
	}
	
	@Deactivate
	public void deactivate() {
		Bus.setServiceLocator(null);
	}
}
