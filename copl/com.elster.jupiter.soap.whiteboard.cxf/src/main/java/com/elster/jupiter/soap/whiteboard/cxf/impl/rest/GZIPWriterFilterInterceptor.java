/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.rest;

import org.apache.commons.io.output.TeeOutputStream;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * http://stackoverflow.com/questions/28091959/jersey-2-logging-and-gzip
 * This interceptor will added a compressor to the output stream and at the same time keep a local copy of the output
 * stream to keep track (for logging purposes) of what data was compressed.
 */
@Provider
@Priority(Priorities.ENTITY_CODER)
public class GZIPWriterFilterInterceptor implements ContainerResponseFilter, WriterInterceptor {
    /**
     * http header value for Accept-Encoding
     */
    public static final String GZIP = "gzip";
    public static final String ENTITY_LOGGER_PROPERTY = GZIPWriterFilterInterceptor.class.getName() + ".entityLogger";

    protected GZIPOutputStream getGZIPOutputStream(final OutputStream outputStream) throws IOException {
        return new GZIPOutputStream(outputStream);
    }

    @Override
    public void filter(final ContainerRequestContext requestContext,
                       final ContainerResponseContext responseContext) throws IOException {
        final MultivaluedMap<String, String> headers = requestContext.getHeaders();
        //if client has requested with HttpHeader of Accept-Encoding: gzip,
        //then do gzip encoding and put Content-Coding: gzip as part of response
        if (headers != null && headers.get(HttpHeaders.ACCEPT_ENCODING) != null) {
            for (final String header : headers.get(HttpHeaders.ACCEPT_ENCODING)) {
                if (header.contains(GZIP)) {
                    responseContext.getHeaders().add(HttpHeaders.CONTENT_ENCODING, GZIP);
                    break;
                }
            }
        }
    }

    @Override
    public void aroundWriteTo(final WriterInterceptorContext context)
            throws IOException, WebApplicationException {
        if (context.getHeaders() != null && context.getHeaders().containsKey(HttpHeaders.CONTENT_ENCODING)
                && context.getHeaders().get(HttpHeaders.CONTENT_ENCODING).contains(GZIP)) {
            // for the response, there is only one output stream i.e. written to by jersey after all the
            // interceptors have invoked context.proceed() in their aroundWriteTo method.
            // so when we wrap the base output stream in a GZIPOutputStream, the response will be GZIP encoded.
            // but we also want to log the payload. Hence we use TeeOutputStream to "clone" the base outputstream.
            // As a result, jersey will write directly to TeeOutputStream, which in turn will forward the writes to
            // both GZIPOutputStream and ByteArrayOutputStream. We also store the ByteArrayOutputStream in context
            // so that it can be accessed by APILoggingFilter.
            final ByteArrayOutputStream boas = new ByteArrayOutputStream();

            final OutputStream outputStream = context.getOutputStream();
            context.setOutputStream(new TeeOutputStream(getGZIPOutputStream(outputStream), boas));
            context.setProperty(ENTITY_LOGGER_PROPERTY, boas);

        }
        context.proceed();
    }
}