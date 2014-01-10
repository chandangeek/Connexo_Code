package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@Component(name = "com.elster.jupiter.metering.rest" , service=Application.class , immediate = true , property = {"alias=/mtr"} )
public class MeteringApplication extends Application implements BinderProvider {
	
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
	}
	
	@Deactivate
	public void deactivate() {
	}

    @Override
    public Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(restQueryService).to(RestQueryService.class);
                bind(transactionService).to(TransactionService.class);
                bind(meteringService).to(MeteringService.class);
            }
        };
    }
}
