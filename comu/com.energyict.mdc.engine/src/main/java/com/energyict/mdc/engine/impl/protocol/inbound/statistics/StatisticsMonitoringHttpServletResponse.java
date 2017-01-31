/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.protocol.inbound.statistics;

import com.elster.jupiter.util.Counter;
import com.elster.jupiter.util.Counters;
import com.elster.jupiter.util.time.StopWatch;
import com.energyict.mdc.engine.impl.tools.Strings;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * Provides an implementation for the HttpServletResponse interface
 * that will monitor the usage of the request
 * in terms of performance but also in terms of
 * the number of bytes that are transferred.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-25 (14:31)
 */
public class StatisticsMonitoringHttpServletResponse implements HttpServletResponse {

    private static final int NUMBER_OF_BYTES_IN_LONG = 8;
    private static final int NUMBER_OF_BYTES_IN_INT = 4;
    private static final long NANOS_IN_MILLI = 1000000L;

    private HttpServletResponse response;
    private StopWatch talking;
    private Counter bytesSent = Counters.newStrictCounter();

    public StatisticsMonitoringHttpServletResponse (HttpServletResponse response) {
        super();
        this.talking = new StopWatch(false);    // No need to measure cpu
        this.talking.stop();    // Do not start the StopWatch until we really start talking
        this.response = response;
    }

    /**
     * Gets the total number of milli seconds that were
     * spent on calls to this HttpServletResponse.
     *
     * @return The total number of milli seconds
     */
    public long getTalkTime () {
        return this.talking.getElapsed() / NANOS_IN_MILLI;
    }

    /**
     * Gets the total number of bytes that were sent with
     * this HttpServletResponse.
     * Note that only bytes are counted that will actually
     * have gone over the wire between the client and the server process.
     * As an example of bytes that are not counted:
     * {@link #getBufferSize()} is not counted as that only affects internals
     * {@link #addCookie(Cookie)} is counted as cookies are sent between client and server.
     *
     * @return The total number of bytes
     */
    public int getBytesSent () {
        return this.bytesSent.getValue();
    }

    @Override
    public void addCookie (Cookie cookie) {
        this.talking.start();
        this.response.addCookie(cookie);
        this.bytesSent.add(this.calculateCookieSize(cookie));
        this.talking.stop();
    }

    private int calculateCookieSize (Cookie cookie) {
        return Strings.length(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), cookie.getComment());
    }

    @Override
    public boolean containsHeader (String name) {
        return this.response.containsHeader(name);
    }

    @Override
    public String encodeURL (String url) {
        return this.response.encodeURL(url);
    }

    @Override
    public String encodeRedirectURL (String url) {
        return this.response.encodeRedirectURL(url);
    }

    @Override
    public String encodeUrl (String url) {
        return this.response.encodeURL(url);
    }

    @Override
    public String encodeRedirectUrl (String url) {
        return this.response.encodeRedirectURL(url);
    }

    @Override
    public void sendError (int sc, String msg) throws IOException {
        try {
            this.talking.start();
            this.bytesSent.add(Strings.length(msg));
            this.response.sendError(sc, msg);
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public void sendError (int sc) throws IOException {
        try {
            this.talking.start();
            this.response.sendError(sc);
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public void sendRedirect (String location) throws IOException {
        try {
            this.talking.start();
            this.bytesSent.add(Strings.length(location));
            this.response.sendRedirect(location);
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public void setDateHeader (String name, long date) {
        try {
            this.talking.start();
            this.bytesSent.add(Strings.length(name));
            this.bytesSent.add(NUMBER_OF_BYTES_IN_LONG);  // Assuming that long is sent as the strict 8 bytes it requires
            this.response.setDateHeader(name, date);
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public void addDateHeader (String name, long date) {
        try {
            this.talking.start();
            this.bytesSent.add(Strings.length(name));
            this.bytesSent.add(NUMBER_OF_BYTES_IN_LONG);  // Assuming that long is sent as the strict 8 bytes it requires
            this.response.addDateHeader(name, date);
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public void setHeader (String name, String value) {
        try {
            this.talking.start();
            this.bytesSent.add(Strings.length(name, value));
            this.response.setHeader(name, value);
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public void addHeader (String name, String value) {
        try {
            this.talking.start();
            this.bytesSent.add(Strings.length(name, value));
            this.response.addHeader(name, value);
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public void setIntHeader (String name, int value) {
        try {
            this.talking.start();
            this.bytesSent.add(Strings.length(name));
            this.bytesSent.add(NUMBER_OF_BYTES_IN_INT);  // Assuming that int is sent as the strict 4 bytes it requires
            this.response.setIntHeader(name, value);
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public void addIntHeader (String name, int value) {
        try {
            this.talking.start();
            this.bytesSent.add(Strings.length(name));
            this.bytesSent.add(NUMBER_OF_BYTES_IN_INT);  // Assuming that int is sent as the strict 4 bytes it requires
            this.response.addIntHeader(name, value);
        }
        finally {
            this.talking.stop();
        }
    }

    @Override
    public void setStatus (int sc) {
        this.response.setStatus(sc);
    }

    @Override
    public void setStatus (int sc, String sm) {
        this.response.setStatus(sc, sm);
    }

    @Override
    public String getCharacterEncoding () {
        return this.response.getCharacterEncoding();
    }

    @Override
    public String getContentType () {
        return this.response.getContentType();
    }

    @Override
    public ServletOutputStream getOutputStream () throws IOException {
        return new StatisticsMonitoringServletOutputStream(this.response.getOutputStream());
    }

    @Override
    public PrintWriter getWriter () throws IOException {
        throw new UnsupportedOperationException("Use getOutputStream instead");
    }

    @Override
    public void setCharacterEncoding (String charset) {
        this.response.setCharacterEncoding(charset);
    }

    @Override
    public void setContentLength (int len) {
        this.response.setContentLength(len);
    }

    @Override
    public void setContentType (String type) {
        this.bytesSent.add(Strings.length(type));
        this.response.setContentType(type);
    }

    @Override
    public void setBufferSize (int size) {
        this.response.setBufferSize(size);
    }

    @Override
    public int getBufferSize () {
        return this.response.getBufferSize();
    }

    @Override
    public void flushBuffer () throws IOException {
        this.response.flushBuffer();
    }

    @Override
    public void resetBuffer () {
        this.response.resetBuffer();
    }

    @Override
    public boolean isCommitted () {
        return this.response.isCommitted();
    }

    @Override
    public void reset () {
        this.response.reset();
    }

    @Override
    public void setLocale (Locale loc) {
        this.response.setLocale(loc);
    }

    @Override
    public Locale getLocale () {
        return this.response.getLocale();
    }

    private final class StatisticsMonitoringServletOutputStream extends ServletOutputStream {

        private ServletOutputStream stream;

        private StatisticsMonitoringServletOutputStream (ServletOutputStream stream) {
            super();
            this.stream = stream;
        }

        @Override
        public void write (int b) throws IOException {
            try {
                talking.start();
                bytesSent.increment();
                this.stream.write(b);
            }
            finally {
                talking.stop();
            }
        }

    }

}