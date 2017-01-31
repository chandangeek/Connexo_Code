/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.rest.util.Transactional;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Bogus resource to test TransactionWrapper
 */
@Path("/wrapper")
public class TransactionResource {

    @Inject
    public TransactionResource() {
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/transactional")
    public Response getTransactional() {
        return Response.noContent().build();
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/exception")
    public Response getWithError() {
        throw new WebApplicationException();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/notransaction")
    public Response getWithoutTransaction() {
        return Response.noContent().build();
    }
}
