package com.elster.jupiter.validation.rest;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.*;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.Bus;
import com.elster.jupiter.validation.rest.ValidationResource;
import com.elster.jupiter.validation.rest.impl.ServiceLocator;
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

    private NlsService nlsService;
    private volatile Thesaurus thesaurus;

	public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(
                ValidationResource.class,
                LocalizedExceptionMapper.class,
                LocalizedFieldValidationExceptionMapper.class,
                ConstraintViolationExceptionMapper.class);
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

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(validationService.COMPONENTNAME, Layer.REST);
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
                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
                bind(nlsService).to(NlsService.class);
                bind(validationService).to(ValidationService.class);
                bind(thesaurus).to(Thesaurus.class);
            }
        };
    }
}
