package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.Resolver;

import com.elster.jupiter.http.whiteboard.SecurityToken;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.google.common.collect.ImmutableMap;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class HttpContextImpl implements HttpContext {

    static final String USERPRINCIPAL = "com.elster.jupiter.userprincipal";
    static final String LOGIN_URI = "/apps/login/index.html";

    // Resources used by the login page so access is required before authenticating
    static final String[] RESOURCES_NOT_SECURED = {
            "/apps/login/",
            // Anything below will only be used in development.
            "/apps/sky/",
            "/apps/uni/",
            "/apps/ext/"
    };

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



    // TODO: this is handleSecurity based on JWT encrypted cookie - not secure
    // either session tracking on the server side, or double sumbit is required to secure this
    // both options are not feasible at this point

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

        Optional<User> user = Optional.empty();

        String authentication = request.getHeader("Authorization");
        Optional<Cookie> xsrf = Arrays.asList(request.getCookies()).stream().filter(cookie -> cookie.getName().equals("X-CONNEXO-TOKEN")).findFirst();

        if (authentication == null) {
            if (xsrf.isPresent()) {
                user = SecurityToken.verifyToken(xsrf.get().getValue(), request, response, userService);
            }

            if (!xsrf.isPresent() || !user.isPresent()) {
                if (login(request, response)) {
                    return false;
                }

                if (isCachedResource(request.getRequestURL().toString()) && user.isPresent()) {
                    response.setHeader("Cache-Control", "max-age=86400");
                } else {
                    response.setHeader("Cache-Control", "no-cache");
                }
                return true;
            }
        } else {
            if (authentication.startsWith("Basic ")) {
                user = userService.authenticateBase64(authentication.split(" ")[1]);
            } else if (authentication.startsWith("Bearer ")){
                    //&& !authentication.equals("Bearer undefined")) {
                String token = authentication.substring(7);
                user = SecurityToken.verifyToken(token, request, response, userService);
            }
        }
            return user.isPresent() ? allow(request, response, user.get()) : deny(response);
        }


    private boolean login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String server = request.getRequestURL().substring(0, request.getRequestURL().indexOf(request.getRequestURI()));

        if (unsecureAllowed(request.getRequestURI())) {
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            return false;
        } else {
            response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
            response.sendRedirect(server + LOGIN_URI + "?" + "page=" + request.getRequestURL());
            return true;
        }
    }

    private boolean allow(HttpServletRequest request, HttpServletResponse response, User user) {

        request.setAttribute(HttpContext.AUTHENTICATION_TYPE, HttpServletRequest.BASIC_AUTH);
        request.setAttribute(USERPRINCIPAL, user);
        request.setAttribute(HttpContext.REMOTE_USER, user.getName());
        response.setHeader("Cache-Control", "max-age=86400");

        // Send both as header and httponly cookie
        // Static resources will be accessed based on the cookie
        // REST calls will be accessed based on the Authorization header


        Optional<Cookie> xsrf = Arrays.asList(request.getCookies()).stream().filter(cookie -> cookie.getName().equals("X-CONNEXO-TOKEN")).findFirst();
        if (!xsrf.isPresent()) {
            String token = SecurityToken.createToken(user);
            response.setHeader("X-AUTH-TOKEN", token);
            response.setHeader("Authorization", "Bearer " + token);
            SecurityToken.createCookie("X-CONNEXO-TOKEN", token, "/", WhiteBoard.getTokenExpTime()+WhiteBoard.getTimeout(), true, response);
            userService.addLoggedInUser(user);
        } else{
            response.setHeader("X-AUTH-TOKEN",xsrf.get().getValue());
            response.setHeader("Authorization", "Bearer " + xsrf.get().getValue());
        }
        return true;
    }


    private boolean deny(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
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
