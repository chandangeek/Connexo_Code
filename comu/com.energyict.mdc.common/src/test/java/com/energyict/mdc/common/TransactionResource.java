package com.energyict.mdc.common;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.rest.Untransactional;

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

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/transactional")
    public Response getTransactional() {
        return Response.noContent().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/exception")
    public Response getWithError() {
        throw new WebApplicationException();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Untransactional
    @Path("/notransaction")
    public Response getWithoutTransaction() {
        return Response.noContent().build();
    }
}
