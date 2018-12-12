/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.whiteboard.impl;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.Optional;

/**
 * This filter rewrites some parts of the base uri. It will by default overwrite the standard values for scheme, host and
 * port obtained from the HttpRequest and replace them with values provided through Connexo parameters.
 * Currently the following parameters are supported:
 *  - com.elster.jupiter.url.rewrite.host
 *  - com.elster.jupiter.url.rewrite.port
 *  - com.elster.jupiter.url.rewrite.scheme
 *
 * This functionality is intended to counter lost http request information due to URL rewrites by Apache webserver.
 */
@PreMatching
public class UrlRewriteFilter implements ContainerRequestFilter {

    private volatile Optional<String> host = Optional.empty();
    private volatile Optional<Integer> port = Optional.empty();
    private volatile Optional<String> scheme = Optional.empty();

    public void setHost(String host) {
        this.host = Optional.ofNullable(host);
    }

    public void setPort(Integer port) {
        this.port = Optional.ofNullable(port);
    }

    public void setScheme(String scheme) {
        this.scheme = Optional.ofNullable(scheme);
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        UriBuilder baseUriBuilder = containerRequestContext.getUriInfo().getBaseUriBuilder();
        UriBuilder requestUriBuilder = containerRequestContext.getUriInfo().getRequestUriBuilder();
        applyPropertiesToURL(baseUriBuilder);
        applyPropertiesToURL(requestUriBuilder);
        containerRequestContext.setRequestUri(baseUriBuilder.build(),requestUriBuilder.build());
    }

    protected void applyPropertiesToURL(UriBuilder baseUriBuilder) {
        host.ifPresent(baseUriBuilder::host);
        port.ifPresent(baseUriBuilder::port);
        scheme.ifPresent(baseUriBuilder::scheme);
    }

}
