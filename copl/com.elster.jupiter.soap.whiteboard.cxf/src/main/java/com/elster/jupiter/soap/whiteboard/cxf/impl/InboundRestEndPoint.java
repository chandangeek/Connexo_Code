package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.InboundRestEndPointProvider;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Application;

/**
 * This endpoint manager knows how to set up and tear down an inbound REST endpoint. Features are added as configured on the endpoint configuration.
 * The actually created application is cached to allow tear-down.
 */
public final class InboundRestEndPoint implements ManagedEndpoint {
    private InboundRestEndPointProvider endPointProvider;
    private InboundEndPointConfiguration endPointConfiguration;
    private final String logDirectory;

    private Application application;

    @Inject
    public InboundRestEndPoint(@Named("LogDirectory") String logDirectory) {
        this.logDirectory = logDirectory;
    }

    InboundRestEndPoint init(InboundRestEndPointProvider endPointProvider, InboundEndPointConfiguration endPointConfiguration) {
        this.endPointProvider = endPointProvider;
        this.endPointConfiguration = endPointConfiguration;
        return this;
    }

    @Override
    public void publish() {
        if (this.isPublished()) {
            throw new IllegalStateException("Service already published");
        }
    }

    @Override
    public void stop() {
        if (this.isPublished()) {
//            application.stop();
            application = null;
        } else {
            throw new IllegalStateException("Service already stopped");
        }
    }

    @Override
    public boolean isInbound() {
        return true;
    }

    @Override
    public boolean isPublished() {
        return application != null;
    }
}
