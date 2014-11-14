package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.Resolver;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import java.rmi.RemoteException;
import java.util.Optional;

import com.elster.jupiter.yellowfin.YellowfinService;
import com.google.common.collect.ImmutableMap;
import com.hof.mi.web.service.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.net.URL;
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
    private final YellowfinService yellowfinService;
    private final TransactionService transactionService;
    private final AtomicReference<EventAdmin> eventAdminHolder;

    HttpContextImpl(WhiteBoard whiteboard, Resolver resolver, UserService userService, TransactionService transactionService, YellowfinService yellowfinService, AtomicReference<EventAdmin> eventAdminHolder) {
        this.resolver = resolver;
        this.userService = userService;
        this.transactionService = transactionService;
        this.eventAdminHolder = eventAdminHolder;
        this.whiteboard = whiteboard;
        this.yellowfinService = yellowfinService;
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

        whiteboard.checkLicense();

        String authentication = request.getHeader("Authorization");
        if (authentication == null) {
            // Not logged in or session expired, so authentication is required
            if (request.getSession(true).getAttribute("user") == null) {
                if (login(request, response)) {
                    return false;
                }
            }
            if (isCachedResource(request.getRequestURL().toString())) {
                response.setHeader("Cache-Control", "max-age=86400");
            } else {
                response.setHeader("Cache-Control", "no-cache");
            }
            return true;
        }

        Optional<User> user = Optional.empty();
        try (TransactionContext context = transactionService.getContext()) {
            user = userService.authenticateBase64(authentication.split(" ")[1]);
            context.commit();
        }

        if(user.isPresent()){
            loginYellowfin(response);
            return allow(request, response, user.get());
        }

        return deny(response);
    }

    private void loginYellowfin(HttpServletResponse response) {
        //if(whiteboard.getApps().stream().filter(application -> application.getKey().equals("YFN")).findFirst().isPresent()){
            String yellowfinSession = yellowfinService.login("");
            if(yellowfinSession != null){
                Cookie cookie = new Cookie("JSESSIONID_YELLOWFIN", yellowfinSession);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        //}
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
        request.getSession(true).setMaxInactiveInterval(whiteboard.getSessionTimeout());
        request.getSession(false).setAttribute("user", user);
        response.setHeader("Cache-Control", "max-age=86400");
        response.addCookie(new Cookie("JSESSIONID_YELLOWFIN", "test"));
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
