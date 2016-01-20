package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.Resolver;
import com.elster.jupiter.http.whiteboard.SecurityToken;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.google.common.collect.ImmutableMap;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpContext;
import com.elster.jupiter.http.whiteboard.*;
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
            "index-dev.html"
    };


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
        fireHttpEvent(request);

        String authentication = request.getHeader("Authorization");
        if (authentication != null && authentication.startsWith("Basic ")) {
            return doBasicAuthentication(request, response, authentication.split(" ")[1]);
        } else if (authentication != null && authentication.startsWith("Bearer ")) {
            return doBearerAuthorization(request, response, authentication);
        } else {
            return doCookieAuthorization(request, response);
        }
    }

    private void fireHttpEvent(HttpServletRequest request) {
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
    }

    private boolean doCookieAuthorization(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Optional<User> user = Optional.empty();
        Optional<Cookie> xsrf = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals("X-CONNEXO-TOKEN")).findFirst();
        if (xsrf.isPresent()) {
            user = SecurityToken.getInstance().verifyToken(xsrf.get().getValue(), request, response, userService);
        }

        if (!user.isPresent() && login(request, response)) {
            return false;
        }
        if (user.isPresent() && isCachedResource(request.getRequestURL().toString())) {
            response.setHeader("Cache-Control", "max-age=86400");
        } else {
            response.setHeader("Cache-Control", "no-cache");
        }

        return true;
    }

    private boolean doBearerAuthorization(HttpServletRequest request, HttpServletResponse response, String authentication) {
        Optional<Cookie> xsrf = Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals("X-CONNEXO-TOKEN")).findFirst();
        if (xsrf.isPresent() && !SecurityToken.getInstance().doComparison(xsrf.get(), authentication.substring(authentication.lastIndexOf(" ") + 1))) {
            return deny(request, response);
        }

        Optional<User> user = SecurityToken.getInstance().verifyToken(authentication.substring(authentication.lastIndexOf(" ") + 1), request, response, userService);
        return isAuthenticated(user) ? allow(request, response, user.get()) : deny(request, response);
    }

    private boolean doBasicAuthentication(HttpServletRequest request, HttpServletResponse response, String authentication) {
        Optional<User> user = userService.authenticateBase64(authentication);
        SecurityToken.getInstance().removeCookie(request, response);
        if(isAuthenticated(user)){
            SecurityToken.getInstance().createToken(request, response, user.get(), 0);
            return allow(request, response, user.get());
        }
        else {
            return deny(request, response);
        }
    }

    private boolean isAuthenticated(Optional<User> user) {
        return user.isPresent() && !user.get().getPrivileges().isEmpty();
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
        userService.addLoggedInUser(user);
        return true;
    }


    private boolean deny(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        SecurityToken.getInstance().removeCookie(request, response);
        SecurityToken.getInstance().invalidateSession(request);
        return false;
    }

    private boolean unsecureAllowed(String uri) {
        for (String resource : RESOURCES_NOT_SECURED) {
            if (uri.startsWith(resource)) {
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
