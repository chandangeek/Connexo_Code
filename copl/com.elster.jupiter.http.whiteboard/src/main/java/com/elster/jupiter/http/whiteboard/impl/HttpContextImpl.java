package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.Resolver;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.google.common.collect.ImmutableMap;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpContext;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

    // TODO: add a hard-coded 32 bytes key here
    static final byte[] sharedSecret = new byte[32];

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

    //TODO: this is handleSecurity with double sumbit for security reasons
    // not functional because we cannot double submit authorization header on static resources

    /*@Override
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
        if(authentication.startsWith("Bearer ")){
            // Get the bearer token
            String token = authentication.split(" ")[1];
            Optional<Cookie> xsrf = Arrays.asList(request.getCookies()).stream().filter(cookie -> cookie.getName().equals("X-CONNEXO-XSRF")).findFirst();

            // Compare it
            if(xsrf.isPresent() && xsrf.get().getValue().equals(token)){
                // Authenticated
                user = verifyToken(token);
            }
            else{
                return deny(response);
            }
        }
        else {
            try (TransactionContext context = transactionService.getContext()) {
                user = userService.authenticateBase64(authentication.split(" ")[1]);
                context.commit();
            }
        }

        return user.isPresent() ? allow(request, response, user.get()) : deny(response);
    }*/

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
            if(xsrf.isPresent()){
                // Authenticated
                user = verifyToken(xsrf.get().getValue());
            }

            if(!xsrf.isPresent() || !user.isPresent()){
                if (login(request, response)) {
                    return false;
                }

                if (isCachedResource(request.getRequestURL().toString())) {
                    response.setHeader("Cache-Control", "max-age=86400");
                } else {
                    response.setHeader("Cache-Control", "no-cache");
                }
                return true;
            }
        }
        else {
            try (TransactionContext context = transactionService.getContext()) {
                user = userService.authenticateBase64(authentication.split(" ")[1]);
                context.commit();
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
        String token = createToken(user, whiteboard.getSessionTimeout());
        if(token != null) {
            request.setAttribute(HttpContext.AUTHENTICATION_TYPE, HttpServletRequest.BASIC_AUTH);
            request.setAttribute(USERPRINCIPAL, user);
            request.setAttribute(HttpContext.REMOTE_USER, user.getName());
            request.getSession(true).setMaxInactiveInterval(whiteboard.getSessionTimeout());
            request.getSession(false).setAttribute("user", user);
            response.setHeader("Cache-Control", "max-age=86400");

            // Send both as header and httponly cookie
            // Static resources will be accessed based on the cookie
            // REST calls will be accessed based on the Authorization header

            //response.setHeader("Authorization", "Bearer " + token);
            response.setHeader("X-AUTH-TOKEN", token);

            Cookie tokenCookie = new Cookie("X-CONNEXO-TOKEN", token);
            tokenCookie.setPath("/");
            tokenCookie.setHttpOnly(true);
            response.addCookie(tokenCookie);

            userService.addLoggedInUser(user);

            return true;
        }
        else {
            return deny(response);
        }
    }

    private String createToken(User user, int timeout) {
        // Generate random 256-bit (32-byte) shared secret
        SecureRandom random = new SecureRandom();
        random.nextBytes(sharedSecret);

        // Create HMAC signer
        JWSSigner signer = new MACSigner(sharedSecret);

        // Prepare JWT with claims set
        JWTClaimsSet claimsSet = new JWTClaimsSet();
        claimsSet.setSubject(Long.toString(user.getId()));
        claimsSet.setIssuer("Elster Connexo");
        claimsSet.setExpirationTime(new Date(new Date().getTime() + timeout * 1000));

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

        // Apply the HMAC protection
        try {
            signedJWT.sign(signer);
        } catch (JOSEException e) {
            e.printStackTrace();
            return null;
        }

        // Serialize to compact form, produces something like
        return signedJWT.serialize();
    }

    private Optional<User> verifyToken(String token){
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(sharedSecret);
            if(signedJWT.verify(verifier)
                    && signedJWT.getJWTClaimsSet().getIssuer().equals("Elster Connexo")
                    && new Date().before(signedJWT.getJWTClaimsSet().getExpirationTime())){
                long userId = Long.valueOf(signedJWT.getJWTClaimsSet().getSubject());

                return userService.getLoggedInUser(userId);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JOSEException e) {
            e.printStackTrace();
        }

        return Optional.empty();
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
