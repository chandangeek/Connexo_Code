package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.transaction.TransactionService;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

/**
 * Created by bvn on 6/24/16.
 */
public class EndPointAccessFaultOutInterceptor extends EndPointInterceptor {

    public EndPointAccessFaultOutInterceptor(EndPointConfiguration endPointConfiguration, TransactionService transactionService) {
        super(endPointConfiguration, Phase.PRE_STREAM, transactionService);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        Fault fault = (Fault) message.getContent(Exception.class);
        Throwable stackTrace = fault.getCause();
        logInTransaction("Request failed", new Exception(stackTrace));
    }

}
