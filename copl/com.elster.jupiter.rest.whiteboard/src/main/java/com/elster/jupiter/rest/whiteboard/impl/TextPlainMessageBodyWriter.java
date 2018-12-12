/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.whiteboard.impl;

import com.elster.jupiter.util.json.JsonService;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * This writer will convert our Info objects to JSON, yet return them as being "text/plain". This construction turned out
 * to be required for the response to a MultiPart-file upload. Cfr CXO-2451
 */
@Produces(MediaType.TEXT_PLAIN)
public class TextPlainMessageBodyWriter implements MessageBodyWriter<Object> {
    private final JsonService jsonService;

    @Inject
    public TextPlainMessageBodyWriter(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return MediaType.TEXT_PLAIN_TYPE.equals(mediaType);
    }

    @Override
    public long getSize(Object o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return 0;
    }

    @Override
    public void writeTo(Object o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws
            IOException,
            WebApplicationException {
        entityStream.write(jsonService.serialize(o).getBytes());
    }
}
