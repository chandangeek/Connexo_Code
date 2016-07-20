package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import javax.inject.Inject;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * This feature will add a logger to the affected Configurable (ResourceConfig or Client) for rest web service endpoints.
 * The logger will log basic information about calls to the REST web service end point
 */
public class RestAccessLogFeature implements Feature {

    private EndPointConfiguration endPointConfiguration;
    private final TransactionService transactionService;
    private final RequestEventListener requestEventListener = new LoggingRequestEventListener();

    @Inject
    public RestAccessLogFeature(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public RestAccessLogFeature init(EndPointConfiguration endPointConfiguration) {
        this.endPointConfiguration = endPointConfiguration;
        return this;
    }

    @Override
    public boolean configure(FeatureContext featureContext) {
        featureContext.register(new AccessLogger());
        return false;
    }

    private class AccessLogger implements ApplicationEventListener {
        @Override
        public void onEvent(ApplicationEvent applicationEvent) {

        }

        @Override
        public RequestEventListener onRequest(RequestEvent requestEvent) {
            return requestEventListener;
        }

    }

    /**
     * This event listener logger will logs basic access information about a REST endpoint
     */
    class LoggingRequestEventListener implements RequestEventListener {

        @Override
        public void onEvent(RequestEvent requestEvent) {
            switch (requestEvent.getType()) {
                case REQUEST_MATCHED:
                    logInTransaction(LogLevel.INFO, "Request received");
                    break;
                case FINISHED:
                    if (requestEvent.isSuccess()) {
                        logInTransaction(LogLevel.INFO, "Request completed successfully");
                    } else {
                        logInTransaction("Request failed", new Exception(requestEvent.getException()));
                    }
                    break;
            }
        }

        private void logInTransaction(LogLevel logLevel, String message) {
            if (transactionService.isInTransaction()) {
                endPointConfiguration.log(logLevel, message);
            } else {
                try (TransactionContext context = transactionService.getContext()) {
                    endPointConfiguration.log(logLevel, message);
                    context.commit();
                }
            }
        }

        private void logInTransaction(String message, Exception exception) {
            if (transactionService.isInTransaction()) {
                endPointConfiguration.log(message, exception);
            } else {
                try (TransactionContext context = transactionService.getContext()) {
                    endPointConfiguration.log(message, exception);
                    context.commit();
                }
            }
        }

    }
}
