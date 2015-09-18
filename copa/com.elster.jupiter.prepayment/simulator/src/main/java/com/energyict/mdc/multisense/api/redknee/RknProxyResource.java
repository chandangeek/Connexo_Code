package com.energyict.mdc.multisense.api.redknee;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.inject.Inject;
import javax.inject.Named;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by bvn on 9/17/15.
 */
@Path("usagepoints/{mrid}/contactor")
public class RknProxyResource {

    private final ConsumptionExportGenerator generator;
    private final String connexoUrl;

    @Inject
    public RknProxyResource(ConsumptionExportGenerator generator, @Named("connexoUrl") String connexoUrl) {
        this.generator = generator;
        this.connexoUrl = connexoUrl;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getName() {
        return Response.ok().entity("{\"status\":\"ok\"}").build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateContactor(@PathParam("mrid") String mRID, ContactorInfo contactorInfo) {
        UsagePoint usagePoint = generator.getUsagePoint(mRID).orElseThrow(()->new WebApplicationException("No such usagepoint", Response.Status.NOT_FOUND));
        Response response = proxyRequest(mRID, contactorInfo);
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
        }
        return response;
    }

    private Response proxyRequest(String mrid, ContactorInfo contactorInfo) {
        HttpAuthenticationFeature basicAuthentication = HttpAuthenticationFeature.basic("admin", "D3moAdmin");
        Client jerseyClient = ClientBuilder.newClient().
                register(new JacksonFeature()).
                register(basicAuthentication);
        try {
            return jerseyClient.
                    target(connexoUrl+"/"+mrid+"/contactor").
                    request(MediaType.APPLICATION_JSON).
                    put(Entity.json(contactorInfo));
        }
        catch (ClientErrorException | ProcessingException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\""+e.getLocalizedMessage()+"\"}").build();
        }
    }

}
