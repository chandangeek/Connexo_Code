package com.elster.jupiter.webservice.inbound.rest.scim.impl.jaxrs.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;

@TokenResourceOnlyFilter
public class TokenEndPointResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        responseContext.getHeaders().add(HttpHeaders.CACHE_CONTROL, "no-store");
        responseContext.getHeaders().add("Pragma", "no-cache");
    }

}
