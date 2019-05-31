/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.soap;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.transaction.TransactionService;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * This is an interceptor, however, depending on the direction of the webservice, must be connected as Out or In interceptor in the appropriate stream
 * Created by bvn on 6/24/16.
 */
public class EndPointAccessRequestInterceptor extends AbstractEndPointInterceptor {
    private static final Logger LOGGER = Logger.getLogger(EndPointAccessRequestInterceptor.class.getSimpleName());

    public EndPointAccessRequestInterceptor(EndPointConfiguration endPointConfiguration, TransactionService transactionService) {
        super(endPointConfiguration, endPointConfiguration.isInbound() ? Phase.RECEIVE : Phase.PRE_STREAM, transactionService);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        logInTransaction(LogLevel.INFO, isForInboundService() ? "Request received." : "Request sent.");
        try {
            if (message != null) {
                // TODO: finalize code & save payload
                if (isForInboundService()) {
                    InputStream is = message.getContent(InputStream.class);
                    CachedOutputStream cachedOutputStream = new CachedOutputStream();
                    IOUtils.copy(is, cachedOutputStream);
                    is.close();
                    String payload = new String(cachedOutputStream.getBytes());
                    cachedOutputStream.flush();
                    message.setContent(InputStream.class, new ByteArrayInputStream(cachedOutputStream.getBytes()));
                    cachedOutputStream.close();
                } else {
                    OutputStream os = message.getContent(OutputStream.class);
                    CacheAndWriteOutputStream newOut = new CacheAndWriteOutputStream(os);
                    message.setContent(OutputStream.class, newOut);
                    newOut.registerCallback(new CachedOutputStreamCallback() {
                        @Override
                        public void onClose(CachedOutputStream cachedOutputStream) {
                            try {
                                String payload = new String(cachedOutputStream.getBytes());
                            } catch (IOException e) {
                                LOGGER.severe(e.getLocalizedMessage());
                            }
                        }

                        @Override
                        public void onFlush(CachedOutputStream cachedOutputStream) {
                            // nothing to do
                        }
                    });
                }
            }
        } catch (IOException e) {
            LOGGER.severe(e.getLocalizedMessage());
        }
    }
}
