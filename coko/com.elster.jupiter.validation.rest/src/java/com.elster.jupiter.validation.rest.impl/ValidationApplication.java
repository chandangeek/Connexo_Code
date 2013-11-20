package com.elster.jupiter.validation.rest.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@Component(name = "com.elster.jupiter.event.rest" , service=Application.class , immediate = true , property = {"alias=/evt"} )
public class ValidationApplication extends Application implements ServiceLocator {

	private final Set<Class<?>> classes = new HashSet<>();
	private volatile ValidationService validationService;
	private volatile TransactionService transactionService;
	private volatile RestQueryService restQueryService;

	public ValidationApplication() {
		classes.add(ValidationResource.class);
	}

	public Set<Class<?>> getClasses() {
		return classes;
	}

	@Override
	public ValidationService getValidationService() {
		return validationService;
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
	public void setValidationService(ValidationService validationService) {
		this.validationService = validationService;
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
