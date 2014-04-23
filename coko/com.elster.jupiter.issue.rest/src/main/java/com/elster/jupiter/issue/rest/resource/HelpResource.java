package com.elster.jupiter.issue.rest.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/issue")
public class HelpResource extends BaseResource {

    //TODO delete when events will be produced by MDC
    @GET
    @Path("/event")
    @Produces(MediaType.APPLICATION_JSON)
    public Object getEvent(@QueryParam("topic") String topic, @QueryParam("eventIdentifier") String eventIdentifier) {
        getIssueHelpService().postEvent(topic, eventIdentifier);
        return "Send event with topic: " + topic + " eventIdentifier: " + eventIdentifier;
    }
}
