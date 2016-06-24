package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.transaction.TransactionService;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

/**
 * Created by bvn on 6/24/16.
 */
public class EndPointAccessInInterceptor extends EndPointInterceptor {

    public EndPointAccessInInterceptor(EndPointConfiguration endPointConfiguration, TransactionService transactionService) {
        super(endPointConfiguration, Phase.RECEIVE, transactionService);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        logInTransaction(LogLevel.INFO, "Request received");
    }
}
