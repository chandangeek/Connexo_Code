package com.energyict.mdc.common.rest;

import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.StreamingOutput;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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
            switch (event.getType()) {
                case REQUEST_MATCHED:
                    if (!event.getUriInfo().getMatchedResourceMethod().getInvocable().getHandlingMethod().isAnnotationPresent(Untransactional.class)) {
                        contextThreadLocal.set(transactionService.getContext());
                    }
                    break;
                case FINISHED:
                    TransactionContext context = contextThreadLocal.get();
                    if (context!=null) { // context will be null if METHOD was never started, e.g. in case of 404
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
