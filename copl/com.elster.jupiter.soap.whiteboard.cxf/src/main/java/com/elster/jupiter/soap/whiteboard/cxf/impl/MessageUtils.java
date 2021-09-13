/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Message;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {
    private static final Pattern REQUEST_ELEMENT_PATTERN = Pattern.compile("<\\w*:?Body[^>]*>[^<]*<([^>]+)>");
    private static final Logger LOGGER = Logger.getLogger(MessageUtils.class.getSimpleName());

    private MessageUtils() {
        // not instantiable
    }

    public static long getOccurrenceId(WebServiceContext webServiceContext) {
        return getOccurrenceId(webServiceContext.getMessageContext());
    }

    public static Optional<Long> findOccurrenceId(Map<?, ?> map) {
        return Optional.ofNullable(map.get(WebServiceCallOccurrence.MESSAGE_CONTEXT_OCCURRENCE_ID))
                .map(Long.class::cast);
    }

    public static long getOccurrenceId(Map<?, ?> map) {
        return findOccurrenceId(map)
                .orElseThrow(() -> new IllegalStateException("Web service call occurrence id isn't present in context"));
    }

    public static void setOccurrenceId(Message message, long id) {
        message.put(WebServiceCallOccurrence.MESSAGE_CONTEXT_OCCURRENCE_ID, id);
    }

    public static void setOccurrenceId(BindingProvider port, long id) {
        port.getRequestContext().put(WebServiceCallOccurrence.MESSAGE_CONTEXT_OCCURRENCE_ID, id);
    }

    public static String getRequestName(String payload) {
        Matcher matcher = REQUEST_ELEMENT_PATTERN.matcher(payload);
        if (matcher.find()) {
            String requestName = matcher.group(1).replace("/", "").trim();
            requestName = requestName.split("\\s+")[0];
            requestName = requestName.substring(requestName.indexOf(':') + 1);
            if (!requestName.isEmpty()) {
                return Character.toLowerCase(requestName.charAt(0)) + requestName.substring(1);
            }
        }
        return null;
    }

    public static String getIncomingPayload(Message message) {
        String payload = null;
        InputStream is = message.getContent(InputStream.class);
        try (CachedOutputStream cachedOutputStream = new CachedOutputStream()) {
            if (IOUtils.copyAndCloseInput(is, cachedOutputStream) > 0) {
                payload = new String(cachedOutputStream.getBytes());
            }
            message.setContent(InputStream.class, new ByteArrayInputStream(cachedOutputStream.getBytes()));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Couldn't retrieve payload from inbound request: " + e.getLocalizedMessage(), e);
        }
        return payload;
    }

    public static void executeOnOutgoingPayloadAvailable(Message message, Consumer<String> consumer) {
        OutputStream os = message.getContent(OutputStream.class);
        CacheAndWriteOutputStream newOut = new CacheAndWriteOutputStream(os);
        message.setContent(OutputStream.class, newOut);
        newOut.registerCallback(new CachedOutputStreamCallback() {
            @Override
            public void onClose(CachedOutputStream cachedOutputStream) {
                try {
                    consumer.accept(new String(cachedOutputStream.getBytes()));
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Couldn't retrieve payload from outbound request: " + e.getLocalizedMessage(), e);
                }
            }

            @Override
            public void onFlush(CachedOutputStream cachedOutputStream) {
                // nothing to do
            }
        });
    }
}
