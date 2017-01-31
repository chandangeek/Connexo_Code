/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.redknee;

import org.glassfish.jersey.jackson.JacksonFeature;

import javax.inject.Inject;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Created by bvn on 9/17/15.
 */
@Path("/public/api/rkn/v1.0/")
public class RknProxyResource {

    private final ConsumptionExportGenerator generator;
    private final Configuration configuration;

    @Inject
    public RknProxyResource(ConsumptionExportGenerator generator,
                            Configuration configuration) {
        this.generator = generator;
        this.configuration = configuration;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getName() {
        return Response.ok().entity("{\"status\":\"ok\"}").build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("usagepoints/{mrid}/contactor")
    public Response updateContactor(@PathParam("mrid") String mRID, ContactorInfo contactorInfo, @Context SecurityContext context, @Context UriInfo uriInfo) {
        SecurityEnvelope securityEnvelope = (SecurityEnvelope) context.getUserPrincipal();
        UsagePoint usagePoint = generator.getUsagePoint(mRID).orElseThrow(() -> new WebApplicationException("No such usage point in the simulator's config. Add it to the config or correct the mRID in the URL.", Response.Status.NOT_FOUND));
        Response response = configuration.getConnexoUrl().isPresent()?
                proxyRequest(mRID, contactorInfo, securityEnvelope):
                Response.accepted().location(URI.create("http://noproxy/noproxy")).build();
        if (response.getStatus()== Response.Status.ACCEPTED.getStatusCode()) {
            switch (contactorInfo.status) {
                case connected:
                case armed:
                    usagePoint.connect();
                    break;
                case disconnected:
                    usagePoint.disconnect();
                    break;
            }
            return Response
                    .accepted()
                    .location(UriBuilder.fromUri(uriInfo.getBaseUri()).uri(response.getHeaderString("location")).build())
                    .build();
        }
        return response;
    }

    private Response proxyRequest(String mrid, ContactorInfo contactorInfo, SecurityEnvelope securityEnvelope) {
        Client jerseyClient = ClientBuilder.newClient().
                register(new JacksonFeature());
        securityEnvelope.authenticate(jerseyClient);
        try {
            return jerseyClient.
                    target(configuration.getConnexoUrl().get()+"/"+mrid+"/contactor").
                    request(MediaType.APPLICATION_JSON).
                    put(Entity.json(contactorInfo));
        }
        catch (ClientErrorException | ProcessingException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Something happened in Connexo: "+e.getLocalizedMessage()+"\"}").build();
        }
    }
}
