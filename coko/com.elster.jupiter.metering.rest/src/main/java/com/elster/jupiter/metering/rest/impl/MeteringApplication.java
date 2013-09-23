package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import org.osgi.service.component.annotations.*;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@Component(name = "com.elster.jupiter.metering.rest" , service=Application.class , immediate = true , property = {"alias=/mtr"} )
public class MeteringApplication extends Application implements ServiceLocator {
	
	private final Set<Class<?>> classes = new HashSet<>();
	private volatile MeteringService meteringService;
	private volatile TransactionService transactionService;
	private volatile RestQueryService restQueryService;
	
	public MeteringApplication() {
		classes.add(UsagePointResource.class);		
	}

	public Set<Class<?>> getClasses() {
		return classes;
	}

	@Override
	public MeteringService getMeteringService() {
		return meteringService;
	}

	@Override
	public TransactionService getTransactionService() {
		return transactionService;
	}

	@Override
	public RestQueryService getQueryService() {
		return restQueryService;
	}

	@Reference
	public void setMeteringService(MeteringService meteringService) {
		this.meteringService = meteringService;
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
