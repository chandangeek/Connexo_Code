package com.elster.jupiter.http.whiteboard.impl;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.google.common.base.Optional;
import org.osgi.service.http.HttpContext;

import com.elster.jupiter.http.whiteboard.Resolver;

public class HttpContextImpl implements HttpContext {

    static final String USERPRINCIPAL = "com.elster.jupiter.userprincipal";

	private final Resolver resolver;
    private final UserService userService;

    HttpContextImpl(Resolver resolver, UserService userService) {
        this.resolver = resolver;
        this.userService = userService;
    }

    @Override
    public String getMimeType(String arg0) {
        return null;
    }

    @Override
    public URL getResource(String name) {
        return resolver.getResource(name);
    }

    @Override
    public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isClearSessionRequested(request)){
            return true;
        }
        String authentication = request.getHeader("Authorization");
        if (authentication == null) {
            if(request.getSession(true).getAttribute("user") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            return true;
        }
        Optional<User> user = userService.authenticateBase64(authentication.split(" ")[1]);
        return user.isPresent() ? allow(request, user.get()) : deny(response);
    }

    private boolean allow(HttpServletRequest request, User user) {
        request.setAttribute(HttpContext.AUTHENTICATION_TYPE, HttpServletRequest.BASIC_AUTH);
        request.setAttribute(USERPRINCIPAL, user);
        request.setAttribute(HttpContext.REMOTE_USER, user.getName());
        request.getSession(true).setAttribute("user", user);
        return true;
    }

    private boolean deny(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    private boolean isClearSessionRequested(HttpServletRequest request) {
        boolean result = false;
        if (request.getParameter("logout") != null && request.getParameter("logout").equals("true")){
            HttpSession session = request.getSession();
            if (session != null) {
                session.invalidate();
                result = true;
            }
        }
        return result;
    }

}
