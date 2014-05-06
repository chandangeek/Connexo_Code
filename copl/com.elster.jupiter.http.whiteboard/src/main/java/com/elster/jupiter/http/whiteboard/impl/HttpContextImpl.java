package com.elster.jupiter.http.whiteboard.impl;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpContext;

import com.elster.jupiter.http.whiteboard.Resolver;

public class HttpContextImpl implements HttpContext {

    static final String USERPRINCIPAL = "com.elster.jupiter.userprincipal";

	private final Resolver resolver;
    private final UserService userService;
    private final TransactionService transactionService;
    private final AtomicReference<EventAdmin> eventAdminHolder;

    HttpContextImpl(Resolver resolver, UserService userService, TransactionService transactionService, AtomicReference<EventAdmin> eventAdminHolder) {
        this.resolver = resolver;
        this.userService = userService;
        this.transactionService = transactionService;
        this.eventAdminHolder = eventAdminHolder;
    }

    @Override
    public String getMimeType(String arg0) {
        return null;
    }

    @Override
    public URL getResource(String name) {
    	EventAdmin eventAdmin = eventAdminHolder.get();
    	if (eventAdmin != null) {
    		Event event = new Event("com/elster/jupiter/http/GET",ImmutableMap.of("resource",name));
    		eventAdmin.postEvent(event);
    	}
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
