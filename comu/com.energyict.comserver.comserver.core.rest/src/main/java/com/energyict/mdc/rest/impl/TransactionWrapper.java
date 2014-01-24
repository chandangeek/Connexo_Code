package com.energyict.mdc.rest.impl;

import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import javax.inject.Inject;

/**
 * This listener automatically closes DB connection after EVERY REST call
 */
public class TransactionWrapper implements ApplicationEventListener {

    private final TransactionService transactionService;
    private final ThreadLocal<TransactionContext> contextThreadLocal = new ThreadLocal<>();

    @Inject
    public TransactionWrapper(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        // Nothing to do
    }

    @Override
    public RequestEventListener onRequest(final RequestEvent requestEvent) {
        return new RequestEventListener() {

            @Override
            public void onEvent(final RequestEvent event) {
                switch (event.getType()) {
                case RESOURCE_METHOD_START:
                    contextThreadLocal.set(transactionService.getContext());
                    break;
                case FINISHED:
                    if (requestEvent.isSuccess()) {
                        contextThreadLocal.get().commit();
                    }
                    contextThreadLocal.get().close();
                    break;
                case ON_EXCEPTION:
                    if (requestEvent.getException()!=null) {
                        requestEvent.getException().printStackTrace();
                    }
                }
            }
        };
    }
}
