package com.energyict.mdc.rest.impl;

import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

/**
 * This listener automatically closes DB connection after EVERY REST call
 */
public class ExceptionLogger implements ApplicationEventListener {
    private static final Logger LOGGER = Logger.getLogger("CoreRest");
    private final ExceptionLoggingRequestEventListener requestEventListener = new ExceptionLoggingRequestEventListener();

    @Override
    public void onEvent(ApplicationEvent event) {
        // Nothing to do
    }

    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {
        return requestEventListener;
    }

    private class ExceptionLoggingRequestEventListener implements RequestEventListener {

        @Override
        public void onEvent(RequestEvent event) {
            switch (event.getType()) {
            case ON_EXCEPTION:
                Throwable exception = event.getException();
                if (exception !=null) {
                    LOGGER.severe(exception.getMessage());
                    LOGGER.log(Level.FINE, exception.getMessage(), exception);
                    if (!(exception instanceof WebApplicationException)) {
                        throw new WebApplicationException(exception.getLocalizedMessage(),
                                exception,
                                Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exception.getLocalizedMessage()).build());
                    } else {
                        throw (WebApplicationException)exception;
                    }
                }
                break;
            }
        }
    }

}
