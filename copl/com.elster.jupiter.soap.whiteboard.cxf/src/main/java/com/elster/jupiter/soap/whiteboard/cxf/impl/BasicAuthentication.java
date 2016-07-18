package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.InboundEndPointConfiguration;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import org.osgi.service.http.HttpContext;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

/**
 * Performs basic username/password authentication with extra group membership check if the end point config says so.
 */
public class BasicAuthentication implements HttpContext {

    private InboundEndPointConfiguration endPointConfiguration;
    private final UserService userService;

    @Inject
    public BasicAuthentication(UserService userService) {
        this.userService = userService;
    }

    public BasicAuthentication init(InboundEndPointConfiguration endPointConfiguration) {
        this.endPointConfiguration = endPointConfiguration;
        return this;
    }

    @Override
    public boolean handleSecurity(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws
            IOException {
        String authentication = httpServletRequest.getHeader("Authorization");
        if (authentication != null && authentication.startsWith("Basic ")) {
            Optional<User> user = userService.authenticateBase64(authentication.substring(6), httpServletRequest.getRemoteAddr());
            return user.isPresent() && (!endPointConfiguration.getGroup().isPresent() || user.get()
                    .getGroups()
                    .contains(endPointConfiguration.getGroup().get()));
        }
        return false;
    }


    @Override
    public URL getResource(String s) {
        return InboundRestEndPoint.class.getResource(s);
    }

    @Override
    public String getMimeType(String s) {
        return null;
    }
}
