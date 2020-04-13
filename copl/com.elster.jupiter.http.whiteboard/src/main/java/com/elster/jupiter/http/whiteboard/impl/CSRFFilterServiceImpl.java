/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard.impl;


import com.elster.jupiter.http.whiteboard.CSRFFilterService;
import com.elster.jupiter.users.CSRFService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * CSRF Request Filter
 *
 * @author E492165 (M R)
 * @since 2/05/2020 (11:11)
 */

@Component(name = "com.elster.jupiter.http.whiteboard.impl.CSRFFilter",
        immediate = true, service = CSRFFilterService.class)
public final class CSRFFilterServiceImpl implements CSRFFilterService {

    private static final String X_CSRF_TOKEN = "X-CSRF-TOKEN";
    private static final String USER_SESSIONID = "X-SESSIONID";
    private static final String TOKEN_COOKIE_NAME = "X-CONNEXO-TOKEN";
    private static final String POST = "POST";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";

    private volatile CSRFService csrfService;

    public CSRFFilterServiceImpl(){
    }

    @Inject
    public CSRFFilterServiceImpl(CSRFService csrfService){
        super();
        setCSRFService(csrfService);
        activate();
    }

    @Activate
    public void activate(){ }

    @Reference
    public void setCSRFService(CSRFService csrfService){
        this.csrfService = csrfService;
    }

    @Override
    public void createCSRFToken(String sessionId) {
        String csrfToken = base64Encode(sessionId + System.currentTimeMillis());
        csrfService.addCSRFToken(sessionId, csrfToken);
    }

    @Override
    public void removeUserSession(String sessionId) {
        csrfService.removeToken(sessionId);
    }

    @Override
    public String getCSRFToken(String sessionId) {
        return csrfService.getCSRFToken(sessionId);
    }

    @Override
    public boolean handleCSRFSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isFormSubmitRequest(request) && !validCSRFRequest(request)) {
            denyRequest(request, response);
            return false;
        }
        return true;
    }

    private boolean isFormSubmitRequest(HttpServletRequest request){
        return Stream.of(POST, PUT, DELETE).anyMatch(request.getMethod()::equalsIgnoreCase);
    }

    private boolean validCSRFRequest(HttpServletRequest request) {
        Optional<Cookie> sessionId =  getCookie(request, USER_SESSIONID);
        if(sessionId.isPresent()){
            String csrfToken = request.getHeader(X_CSRF_TOKEN);
            if(null == csrfToken && request.getContentType().contains("multipart/form-data")) {
                csrfToken = request.getParameter(X_CSRF_TOKEN);
            }
            if(null != csrfToken){
                boolean valid =  csrfToken.equals(getCSRFToken(sessionId.get().getValue()));
                createCSRFToken(sessionId.get().getValue());
                return valid;
            }
        }
        return false;
    }

    private Optional<Cookie> getCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> cookieName.equals(cookie.getName()))
                    .findFirst();
        }
        return Optional.empty();
    }

    private String base64Encode(String input) {
        return Base64.getUrlEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    private boolean denyRequest(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Optional<Cookie> tokenCookie = getCookie(request, TOKEN_COOKIE_NAME);
        if (tokenCookie.isPresent()) {
            clearCookie(response, tokenCookie.get().getName());
            invalidateSessionCookie(request, response);
        }
        invalidateSession(request);
        return false;
    }

    private void invalidateSessionCookie(HttpServletRequest request, HttpServletResponse response) {
        Optional<Cookie> sessionCookie = getCookie(request,USER_SESSIONID);
        if(sessionCookie.isPresent()) {
            csrfService.removeToken(sessionCookie.get().getValue());
            clearCookie(response, sessionCookie.get().getName());
        }
    }

    private void clearCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    private void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}