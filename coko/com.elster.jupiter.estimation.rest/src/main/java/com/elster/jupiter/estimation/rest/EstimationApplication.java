package com.elster.jupiter.estimation.rest;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.rest.impl.ServiceLocator;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.estimation.rest" , service=Application.class , immediate = true , property = {"alias=/est", "app=SYS", "name=" + EstimationApplication.COMPONENT_NAME} )
public class EstimationApplication extends Application implements ServiceLocator, BinderProvider {
    public static final String COMPONENT_NAME = "EST";

	private volatile EstimationService estimationService;
	private volatile TransactionService transactionService;
	private volatile RestQueryService restQueryService;
    private volatile MeteringService meteringService;

    private NlsService nlsService;
    private volatile Thesaurus thesaurus;
    
	public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(
                EstimationResource.class,
                LocalizedExceptionMapper.class,
                LocalizedFieldValidationExceptionMapper.class,
                ConstraintViolationExceptionMapper.class);
	}

	@Override
	public EstimationService getEstimationService() {
		return estimationService;
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
	public void setEstimationService(EstimationService estimationService) {
		this.estimationService = estimationService;
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
        this.thesaurus = nlsService.getThesaurus(estimationService.COMPONENTNAME, Layer.REST);
   }
	
    @Override
    public Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(restQueryService).to(RestQueryService.class);
                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
                bind(nlsService).to(NlsService.class);
                bind(estimationService).to(EstimationService.class);
                bind(transactionService).to(TransactionService.class);
                bind(thesaurus).to(Thesaurus.class);
            }
        };
    }
}
