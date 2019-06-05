/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.soap;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

class MessageUtils {
    private static final String SOAP_ACTION_HEADER = "SOAPAction";
    private static final Logger LOGGER = Logger.getLogger(MessageUtils.class.getSimpleName());

    private MessageUtils() {
        // not instantiable
    }

    static String getOperationName(Message message) {
        Map<String, List<String>> headers = (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);
        if (headers != null) {
            List<String> actions = headers.get(SOAP_ACTION_HEADER);
            if (actions != null && !actions.isEmpty()) {
                String operation = actions.get(0);
                if (operation != null) {
                    operation = operation.substring(operation.lastIndexOf('/') + 1).replace("\"", "").trim();
                    if (!operation.isEmpty()) {
                        return Character.toLowerCase(operation.charAt(0)) + operation.substring(1);
                    }
                }
            }
        }
        return null;
    }

    static String getIncomingPayload(Message message) {
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

    static void executeOnOutgoingPayloadAvailable(Message message, Consumer<String> consumer) {
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
