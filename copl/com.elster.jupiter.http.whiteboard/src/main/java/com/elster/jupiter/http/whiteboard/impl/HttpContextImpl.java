package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.Resolver;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

public class HttpContextImpl implements HttpContext {

    static final String USERPRINCIPAL = "com.elster.jupiter.userprincipal";
    static final String LOGIN_URI = "/apps/usr/login.html";

    // Resources used by the login page so access is required before authenticating
    static final String[] RESOURCES_NOT_SECURED = {
            "/apps/usr/login.html",
            "/apps/usr/login.js",
            "/apps/ext/ext-all-dev.js",
            "/apps/ext/ext-all.js",
            "/apps/uni/uni-dev.js",
            "/apps/usr/app/controller/Login.js",
            "/apps/usr/app/controller/Base64.js",
            "/apps/usr/app/view/Login.js",
            "/apps/ext/packages/uni-theme-skyline/build/resources",
            "/apps/ext/packages/uni-theme-skyline/build/uni-theme-skyline.js",
            "/apps/uni/resources/css/etc/all.css",
            "/apps/usr/resources/images/connexo.png"};

    // No caching for index.html files, so that authentication will be verified first;
    // Note that resources used in these files are still cached
    static final String[] RESOURCES_NOT_CACHED = {
            "index.html",
            "index-dev.html"};

    private final WhiteBoard whiteboard;
    private final Resolver resolver;
    private final UserService userService;
    private final TransactionService transactionService;
    private final AtomicReference<EventAdmin> eventAdminHolder;

    HttpContextImpl(WhiteBoard whiteboard, Resolver resolver, UserService userService, TransactionService transactionService, AtomicReference<EventAdmin> eventAdminHolder) {
        this.resolver = resolver;
        this.userService = userService;
        this.transactionService = transactionService;
        this.eventAdminHolder = eventAdminHolder;
        this.whiteboard = whiteboard;
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
        EventAdmin eventAdmin = eventAdminHolder.get();
        if (eventAdmin != null) {
            StringBuffer requestUrl = request.getRequestURL();
            String queryString = request.getQueryString();
            if (queryString != null) {
                requestUrl.append("?").append(queryString);
            }
            Event event = new Event("com/elster/jupiter/http/GET", ImmutableMap.of("resource", requestUrl.toString()));
            eventAdmin.postEvent(event);
        }
        if (logoutRequested(request)) {
            clearSession(request);
            return true;
        }

        String authentication = request.getHeader("Authorization");
        if (authentication == null) {
            // Not logged in or session expired, so authentication is required
            if (request.getSession(true).getAttribute("user") == null) {
                login(request, response);
            }
            if (isCachedResource(request.getRequestURL().toString())) {
                response.setHeader("Cache-Control", "max-age=86400");
            } else {
                response.setHeader("Cache-Control", "no-cache");
            }
            return true;
        }

        Optional<User> user = Optional.absent();
        try (TransactionContext context = transactionService.getContext()) {
            user = userService.authenticateBase64(authentication.split(" ")[1]);
            context.commit();
        }
        return user.isPresent() ? allow(request, response, user.get()) : deny(response);
    }

    private void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String server = request.getRequestURL().substring(0, request.getRequestURL().indexOf(request.getRequestURI()));

        if (unsecureAllowed(request.getRequestURI())) {
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
        } else {
            response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
            response.sendRedirect(server + LOGIN_URI + "?" + "page=" + request.getRequestURI());
        }
    }

    private boolean allow(HttpServletRequest request, HttpServletResponse response, User user) {
        request.setAttribute(HttpContext.AUTHENTICATION_TYPE, HttpServletRequest.BASIC_AUTH);
        request.setAttribute(USERPRINCIPAL, user);
        request.setAttribute(HttpContext.REMOTE_USER, user.getName());
        request.getSession(true).setMaxInactiveInterval(whiteboard.getSessionTimeout());
        request.getSession(true).setAttribute("user", user);
        response.setHeader("Cache-Control", "max-age=86400");
        return true;
    }

    private boolean deny(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    private boolean logoutRequested(HttpServletRequest request) {
        if (request.getParameter("logout") != null && request.getParameter("logout").equals("true")) {
            return true;
        }
        return false;
    }

    private void clearSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private boolean unsecureAllowed(String uri) {
        for (String resource : RESOURCES_NOT_SECURED) {
            if (uri.contains(resource)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCachedResource(String uri) {
        for (String resource : RESOURCES_NOT_CACHED) {
            if (uri.endsWith(resource)) {
                return false;
            }
        }
        return true;
    }
}
