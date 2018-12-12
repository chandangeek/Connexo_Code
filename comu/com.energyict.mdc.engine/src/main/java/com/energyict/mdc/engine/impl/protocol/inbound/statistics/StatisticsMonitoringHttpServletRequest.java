/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.protocol.inbound.statistics;

import com.elster.jupiter.util.Counter;
import com.elster.jupiter.util.Counters;
import com.elster.jupiter.util.time.StopWatch;
import com.energyict.mdc.engine.impl.tools.Strings;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * Provides an implementation for the HttpServletRequest interface
 * that will monitor the usage of the request
 * in terms of performance but also in terms of
 * the number of bytes that are transferred.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-25 (13:15)
 */
public class StatisticsMonitoringHttpServletRequest implements HttpServletRequest {

    private static final long NANOS_IN_MILLI = 1000000L;

    private HttpServletRequest request;
    private StopWatch talking;
    private Counter bytesRead = Counters.newStrictCounter();

    public StatisticsMonitoringHttpServletRequest (HttpServletRequest request) {
        super();
        this.talking = new StopWatch(false);    // No need to measure cpu
        this.talking.stop();    // Do not start the StopWatch until we really start talking
        this.request = request;
    }

    /**
     * Gets the total number of milli seconds that were
     * spent on calls to this HttpServletRequest.
     *
     * @return The total number of milli seconds
     */
    public long getTalkTime () {
        return this.talking.getElapsed() / NANOS_IN_MILLI ;
    }

    /**
     * Gets the total number of bytes that were read from
     * this HttpServletRequest.
     * Note that only bytes are counted that will actually
     * have gone over the wire between the client and the server process.
     * As an example of bytes that are not counted:
     * {@link #getProtocol()} is not counted as that is determined from the URL sent.
     * {@link #getCookies()} is counted as cookies are sent between client and server.
     *
     * @return The total number of bytes
     */
    public int getBytesRead () {
        return this.bytesRead.getValue();
    }

    @Override
    public String getAuthType () {
        try {
            this.talking.start();
            return this.request.getAuthType();
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public Cookie[] getCookies () {
        this.talking.start();
        Cookie[] cookies = this.request.getCookies();
        for (Cookie cookie : cookies) {
            this.bytesRead.add(this.calculateCookieSize(cookie));
        }
        return cookies;
    }

    private int calculateCookieSize (Cookie cookie) {
        return Strings.length(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), cookie.getComment());
    }

    @Override
    public long getDateHeader (String name) {
        try {
            this.talking.start();
            return this.request.getDateHeader(name);
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public String getHeader (String name) {
        this.talking.start();
        String header = this.request.getHeader(name);
        this.bytesRead.add(header.length());
        return header;
    }

    @Override
    public Enumeration getHeaders (String name) {
        try {
            this.talking.start();
            return this.request.getHeaders(name);
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public Enumeration getHeaderNames () {
        try {
            /* Do not count bytes read as we are assuming
             * that getHeader(String) is going to be called
             * for each header name soon after. */
            this.talking.start();
            return this.request.getHeaderNames();
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public int getIntHeader (String name) {
        try {
            this.talking.start();
            return this.request.getIntHeader(name);
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public String getMethod () {
        try {
            this.talking.start();
            return this.request.getMethod();
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public String getPathInfo () {
        this.talking.start();
        String pathInfo = this.request.getPathInfo();
        this.bytesRead.add(Strings.length(pathInfo));
        return pathInfo;
    }

    @Override
    public String getPathTranslated () {
        try {
            this.talking.start();
            return this.request.getPathTranslated();
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public String getContextPath () {
        try {
            this.talking.start();
            return this.request.getContextPath();
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public String getQueryString () {
        this.talking.start();
        String queryString = this.request.getQueryString();
        this.bytesRead.add(Strings.length(queryString));
        return queryString;
    }

    @Override
    public String getRemoteUser () {
        try {
            this.talking.start();
            return this.request.getRemoteUser();
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public boolean isUserInRole (String role) {
        try {
            this.talking.start();
            return this.request.isUserInRole(role);
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public Principal getUserPrincipal () {
        try {
            this.talking.start();
            return this.request.getUserPrincipal();
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public String getRequestedSessionId () {
        try {
            this.talking.start();
            return this.request.getRequestedSessionId();
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public String getRequestURI () {
        try {
            this.talking.start();
            return this.request.getRequestURI();
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public StringBuffer getRequestURL () {
        try {
            this.talking.start();
            return this.request.getRequestURL();
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public String getServletPath () {
        try {
            this.talking.start();
            return this.request.getServletPath();
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public HttpSession getSession (boolean create) {
        return this.request.getSession(create);
    }

    @Override
    public HttpSession getSession () {
        return this.request.getSession();
    }

    @Override
    public boolean isRequestedSessionIdValid () {
        return this.request.isRequestedSessionIdValid();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie () {
        return this.request.isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromURL () {
        return this.request.isRequestedSessionIdFromURL();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl () {
        return this.request.isRequestedSessionIdFromURL();
    }

    @Override
    public Object getAttribute (String name) {
        return this.request.getAttribute(name);
    }

    @Override
    public Enumeration getAttributeNames () {
        return this.request.getAttributeNames();
    }

    @Override
    public String getCharacterEncoding () {
        return this.request.getCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding (String env) throws UnsupportedEncodingException {
        this.request.setCharacterEncoding(env);
    }

    @Override
    public int getContentLength () {
        return this.request.getContentLength();
    }

    @Override
    public String getContentType () {
        return this.request.getContentType();
    }

    @Override
    public ServletInputStream getInputStream () throws IOException {
        return new StatisticsMonitoringServletInputStream(this.request.getInputStream());
    }

    @Override
    public String getParameter (String name) {
        this.talking.start();
        String header = this.request.getParameter(name);
        this.bytesRead.add(header.length());
        return header;
    }

    @Override
    public Enumeration getParameterNames () {
        try {
            /* Do not count bytes read as we are assuming
             * that getParameter(String) is going to be called
             * for each header name soon after. */
            this.talking.start();
            return this.request.getParameterNames();
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public String[] getParameterValues (String name) {
        this.talking.start();
        String[] parameterValues = this.request.getParameterValues(name);
        this.bytesRead.add(Strings.length(parameterValues));
        this.talking.stop();
        return parameterValues;
    }

    @Override
    public Map getParameterMap () {
        this.talking.start();
        Map parameterMap = this.request.getParameterMap();
        for (Object key : parameterMap.keySet()) {
            String parameterName = (String) key;
            String[] parameterValues = (String[]) parameterMap.get(key);
            this.bytesRead.add(parameterName.length());
            this.bytesRead.add(Strings.length(parameterValues));
        }
        this.talking.stop();
        return parameterMap;
    }

    @Override
    public String getProtocol () {
        return this.request.getProtocol();
    }

    @Override
    public String getScheme () {
        return this.request.getScheme();
    }

    @Override
    public String getServerName () {
        return this.request.getServerName();
    }

    @Override
    public int getServerPort () {
        return this.request.getServerPort();
    }

    @Override
    public BufferedReader getReader () throws IOException {
        throw new UnsupportedOperationException("Use getInputStream instead");
    }

    @Override
    public String getRemoteAddr () {
        return this.request.getRemoteAddr();
    }

    @Override
    public String getRemoteHost () {
        return this.request.getRemoteHost();
    }

    @Override
    public void setAttribute (String name, Object value) {
        this.request.setAttribute(name, value);
    }

    @Override
    public void removeAttribute (String name) {
        this.request.removeAttribute(name);
    }

    @Override
    public Locale getLocale () {
        return this.request.getLocale();
    }

    @Override
    public Enumeration getLocales () {
        return this.request.getLocales();
    }

    @Override
    public boolean isSecure () {
        return this.request.isSecure();
    }

    @Override
    public RequestDispatcher getRequestDispatcher (String path) {
        return this.request.getRequestDispatcher(path);
    }

    @Override
    public String getRealPath (String path) {
        return this.request.getRealPath(path);
    }

    @Override
    public int getRemotePort () {
        return this.request.getRemotePort();
    }

    @Override
    public String getLocalName () {
        return this.request.getLocalName();
    }

    @Override
    public String getLocalAddr () {
        return this.request.getLocalAddr();
    }

    @Override
    public int getLocalPort () {
        return this.request.getLocalPort();
    }

    private final class StatisticsMonitoringServletInputStream extends ServletInputStream {

        private ServletInputStream stream;

        private StatisticsMonitoringServletInputStream (ServletInputStream stream) {
            super();
            this.stream = stream;
        }

        @Override
        public int read () throws IOException {
            try {
                talking.start();
                int returnValue = this.stream.read();
                bytesRead.increment();
                return returnValue;
            }
            finally {
                talking.stop();
            }
        }


    }

}