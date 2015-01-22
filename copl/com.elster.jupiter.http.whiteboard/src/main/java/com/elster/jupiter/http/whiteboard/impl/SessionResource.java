package com.elster.jupiter.http.whiteboard.impl;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/session")
public class SessionResource {
    @Inject
    private WhiteBoard whiteBoard;

    @GET
    @Path("/timeout")
    @Produces(MediaType.APPLICATION_JSON)
    public SessionTimeOutInfo getSessionTimeOut() {
        return new SessionTimeOutInfo(whiteBoard.getSessionTimeout());
    }

    @GET
    @Path("/timeoutp")
    @Produces("application/x-javascript")
    public String getSessionTimeOutP(@QueryParam("callback") String callback) {
        return String.format("%s({sessionTimeout:%d})",callback, whiteBoard.getSessionTimeout());
    }

}
