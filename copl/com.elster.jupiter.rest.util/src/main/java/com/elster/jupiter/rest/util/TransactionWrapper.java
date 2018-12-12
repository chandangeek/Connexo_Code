/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

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
    private final TransactionManagingEventListener transactionManagingEventListener = new TransactionManagingEventListener();

    @Inject
    public TransactionWrapper(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        // Nothing to do
    }

    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {
        return transactionManagingEventListener;
    }

    private class TransactionManagingEventListener implements RequestEventListener {

        @Override
        public void onEvent(final RequestEvent event) {
            if (event.getUriInfo().getMatchedResourceMethod() != null
                    && event.getUriInfo().getMatchedResourceMethod().getInvocable().getHandlingMethod().isAnnotationPresent(Transactional.class)) {
                switch (event.getType()) {
                    case REQUEST_MATCHED:
                        contextThreadLocal.set(transactionService.getContext());
                        break;
                    case FINISHED:
                        TransactionContext context = contextThreadLocal.get();
                        if (context != null) { // context will be null if METHOD was never started, e.g. in case of 404
                            if (event.isSuccess()) {
                                context.commit();
                            }
                            context.close(); // will rollback if called without commit()
                        }
                        break;
                }
            }
        }
    }
}
