package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

/**
 * Created by bvn on 6/24/16.
 */
public class EndPointInterceptor extends AbstractPhaseInterceptor<Message> {

    private final EndPointConfiguration endPointConfiguration;
    private final TransactionService transactionService;

    public EndPointInterceptor(EndPointConfiguration endPointConfiguration, String phase, TransactionService transactionService) {
        super(phase);
        this.endPointConfiguration = endPointConfiguration;
        this.transactionService = transactionService;
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        endPointConfiguration.log(LogLevel.INFO, "Request received");
    }

    void logInTransaction(LogLevel logLevel, String message) {
        try (TransactionContext context = transactionService.getContext()) {
            endPointConfiguration.log(logLevel, message);
            context.commit();
        }
    }

    void logInTransaction(String message, Exception exception) {
        try (TransactionContext context = transactionService.getContext()) {
            endPointConfiguration.log(message, exception);
            context.commit();
        }
    }


}
