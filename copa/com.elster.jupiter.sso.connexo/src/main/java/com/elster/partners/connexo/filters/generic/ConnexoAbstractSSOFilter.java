/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.connexo.filters.generic;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Created by dragos on 11/19/2015.
 */
public abstract class ConnexoAbstractSSOFilter implements Filter {
    private FilterConfig filterConfig;

    protected final String CONNEXO_CONFIG = System.getProperty("connexo.configuration");

    protected List<String> excludedUrls = new ArrayList<>();
    protected List<String> unauthorizedUrls = new ArrayList<>();

    protected Properties properties = new Properties();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        String excludePatterns = this.filterConfig.getInitParameter("excludePatterns");
        if(excludePatterns != null) {
            String[] exclude = excludePatterns.split(";");
            for (String url : exclude) {
                excludedUrls.add(url.replace("*", ".*?").trim());
            }
        }

        String unauthorizedPatterns = this.filterConfig.getInitParameter("unauthorizedPatterns");
        if(unauthorizedPatterns != null) {
            String[] unauthorize = unauthorizedPatterns.split(";");
            for (String url : unauthorize) {
                unauthorizedUrls.add(url.replace("*", ".*?").trim());
            }
        }

        loadProperties();
    }

    @Override
    public void destroy() {

    }

    protected void redirectToLogin(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        response.sendRedirect(getConnexoExternalUrl() + "/apps/login/index.html?page=" + request.getRequestURL());
    }

    protected void redirectToLogout(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        updateToken(response, null, 0);
        String page = request.getRequestURL().substring(0, request.getRequestURL().indexOf(request.getServletPath())+1);
        response.sendRedirect(getConnexoExternalUrl() + "/apps/login/index.html?logout&page=" + page);
    }

    protected String getConnexoInternalUrl() {
        String url = properties.getProperty("com.elster.jupiter.url");
        return (url != null) ? url : "http://localhost:8080";
    }

    protected String getConnexoExternalUrl() {
        String url = properties.getProperty("com.elster.jupiter.externalurl");
        return (url != null) ? url : getConnexoInternalUrl();
    }

    protected boolean shouldExcludUrl(final HttpServletRequest request) {
        String requestUrl = request.getRequestURI();
        for(String url : excludedUrls) {
            if(requestUrl.matches(request.getContextPath() + url)) {
                return true;
            }
        }

        return false;
    }

    protected boolean shouldRefreshToken(final HttpServletRequest request){
        return !(shouldUnauthorize(request) && request.getParameterMap().containsKey("wait"));
    }

    protected boolean shouldUnauthorize(final HttpServletRequest request) {
        String requestUrl = request.getRequestURI();
        for(String url : unauthorizedUrls) {
            if(requestUrl.matches(request.getContextPath() + url)) {
                return true;
            }
        }

        return false;
    }

    protected String getTokenFromCookie(HttpServletRequest request) {
        String authorizationToken = null;
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals("X-CONNEXO-TOKEN")) {
                    authorizationToken = cookies[i].getValue();
                    break;
                }
            }
        }
        return authorizationToken;
    }

    protected String getTokenFromAuthorizationHeader(HttpServletRequest request) {
        String authorizationToken = null;
        String authorization = request.getHeader("Authorization");
        if(authorization != null){
            if(authorization.startsWith("Bearer ")) {
                authorizationToken = authorization.split(" ")[1];
            }
            else if (authorization.startsWith("Basic ")){
                ConnexoRestProxyManager restManager = ConnexoRestProxyManager.getInstance();
                authorizationToken = restManager.getConnexoAuthorizationToken(authorization);
            }
        }
        return authorizationToken;
    }

    protected void updateToken(HttpServletResponse response, String newValue, int maxAge) {
        response.setHeader("X-AUTH-TOKEN", newValue);

        DateFormat dateFormatter = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss 'GMT'", Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, maxAge);
        StringBuilder cookie = new StringBuilder("X-CONNEXO-TOKEN=" + newValue + "; ");
        cookie.append("Path=/; ");
        if(maxAge > 0){
            cookie.append("Expires=" + dateFormatter.format(calendar.getTime()) + "; ");
        }
        else{
            cookie.append("Expires=Thu, 01 Jan 1970 00:00:01 GMT; ");
        }
        cookie.append("Max-Age=" + maxAge + "; ");
        cookie.append("HttpOnly");
        response.setHeader("Set-Cookie", cookie.toString());
    }

    private void loadProperties() {
        if(CONNEXO_CONFIG != null){
            try {
                FileInputStream inputStream = new FileInputStream(CONNEXO_CONFIG);
                properties.load(inputStream);
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
