package com.elster.jupiter.issue.rest.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/issue")
public class HelpController extends BaseController{

    public HelpController() {
        super();
    }

    //TODO delete when events will be produced by MDC
    @GET
    @Path("/event")
    @Produces(MediaType.APPLICATION_JSON)
    public Object getEvent() {
        getIssueService().getEvent();
        return true;
    }
    // END delete when events will be produced by MDC
}
