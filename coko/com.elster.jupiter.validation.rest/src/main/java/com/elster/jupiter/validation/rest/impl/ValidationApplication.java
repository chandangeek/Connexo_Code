package com.elster.jupiter.validation.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Set;

@Component(name = "com.elster.jupiter.validation.rest" , service=Application.class , immediate = true , property = {"alias=/val"} )
public class ValidationApplication extends Application implements ServiceLocator, BinderProvider {

	private volatile ValidationService validationService;
	private volatile TransactionService transactionService;
	private volatile RestQueryService restQueryService;
    private volatile MeteringService meteringService;

	public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(ValidationResource.class);
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

    @Override
    public MeteringService getMeteringService() {
        return meteringService;
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

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }
	
	@Activate
	public void activate() {
		Bus.setServiceLocator(this);
	}
	
	@Deactivate
	public void deactivate() {
		Bus.setServiceLocator(null);
	}

    @Override
    public Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(restQueryService).to(RestQueryService.class);
            }
        };
    }
}
