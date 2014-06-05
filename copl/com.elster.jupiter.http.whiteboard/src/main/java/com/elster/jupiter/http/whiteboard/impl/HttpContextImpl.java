package com.elster.jupiter.http.whiteboard.impl;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.google.common.base.Optional;

import org.osgi.service.http.HttpContext;

import com.elster.jupiter.http.whiteboard.Resolver;

public class HttpContextImpl implements HttpContext {

    static final String USERPRINCIPAL = "com.elster.jupiter.userprincipal";
    static final String LOGIN_URI = "/apps/usr/login.html";

	private final Resolver resolver;
    private final UserService userService;
    private final TransactionService transactionService;

    HttpContextImpl(Resolver resolver, UserService userService, TransactionService transactionService) {
        this.resolver = resolver;
        this.userService = userService;
        this.transactionService = transactionService;
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
                String referer = request.getHeader("referer");
                String server = request.getRequestURL().substring(0, request.getRequestURL().indexOf(request.getRequestURI()));

                if(request.getRequestURI().startsWith(LOGIN_URI) || (referer != null && referer.startsWith(server + LOGIN_URI))){
                    // Allow access to resources used by the login page
                    response.setStatus(HttpServletResponse.SC_ACCEPTED);
                }
                else{
                    if(!request.getRequestURI().startsWith(LOGIN_URI)){
                        response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
                        response.sendRedirect(server + LOGIN_URI + "?" + "page=" + request.getRequestURI());
                    }
                }
            }
            return true;
        }
        Optional<User> user = Optional.absent();
        try (TransactionContext context = transactionService.getContext()) {

            user = userService.authenticateBase64(authentication.split(" ")[1]);
            context.commit();
        }
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
