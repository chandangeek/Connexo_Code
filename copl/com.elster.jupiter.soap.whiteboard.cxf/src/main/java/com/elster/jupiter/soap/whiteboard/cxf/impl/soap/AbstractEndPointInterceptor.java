/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.soap;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.transaction.TransactionService;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

/**
 * Abstract class intercepting calls on (Apache CXF) endpoints and facilitating logging on the (Connexo) end point configuration
 */
public abstract class AbstractEndPointInterceptor extends AbstractPhaseInterceptor<Message> {

    private final EndPointConfiguration endPointConfiguration;
    private final TransactionService transactionService;

    public AbstractEndPointInterceptor(EndPointConfiguration endPointConfiguration, String phase, TransactionService transactionService) {
        super(phase);
        this.endPointConfiguration = endPointConfiguration;
        this.transactionService = transactionService;
    }

    void logInTransaction(LogLevel logLevel, String message) {
        endPointConfiguration.log(logLevel, message);
    }

    void logInTransaction(String message, Exception exception) {
        endPointConfiguration.log(message, exception);
    }

    boolean isForInboundService() {
        return endPointConfiguration.isInbound();
    }
}
