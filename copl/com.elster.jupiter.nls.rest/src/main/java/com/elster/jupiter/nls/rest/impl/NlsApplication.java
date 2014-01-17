package com.elster.jupiter.nls.rest.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.core.Application;
import java.util.Set;

@Component(name = "com.elster.jupiter.nls.rest", service = Application.class, immediate = true, property = {"alias=/nls"})
public class NlsApplication extends Application implements BinderProvider {

    private final Set<Class<?>> classes = ImmutableSet.<Class<?>>of(ThesaurusResource.class);
    private volatile NlsService nlsService;
    private volatile TransactionService transactionService;
    private volatile RestQueryService restQueryService;

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    @Activate
    public void activate() {

    }

    @Deactivate
    public void deactivate() {

    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setRestQueryService(RestQueryService restQueryService) {
        this.restQueryService = restQueryService;
    }

    @Override
    public Binder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(nlsService).to(NlsService.class);
                bind(transactionService).to(TransactionService.class);
                bind(restQueryService).to(RestQueryService.class);
            }
        };
    }
}
