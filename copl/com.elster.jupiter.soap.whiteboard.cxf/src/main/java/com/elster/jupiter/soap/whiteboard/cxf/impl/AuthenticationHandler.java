package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import java.io.IOException;

public class AuthenticationHandler implements ContainerRequestFilter {

    private final UserService userService;

    @Inject
    public AuthenticationHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authorization = requestContext.getHeaderString("Authorization");
        String[] parts = authorization.split(" ");
        if (parts.length != 2 || !"Basic".equals(parts[0])) {
            requestContext.abortWith(createFaultResponse());
            return;
        }

        if (!userService.authenticateBase64(parts[1]).isPresent()) {
            // authentication failed, request the authetication, add the realm name if needed to the value of WWW-Authenticate
            requestContext.abortWith(Response.status(401).header("WWW-Authenticate", "Basic").build());
        }

    }

    private Response createFaultResponse() {
        return Response.status(401).header("WWW-Authenticate", "Basic realm=\"service.com\"").build();
    }
}