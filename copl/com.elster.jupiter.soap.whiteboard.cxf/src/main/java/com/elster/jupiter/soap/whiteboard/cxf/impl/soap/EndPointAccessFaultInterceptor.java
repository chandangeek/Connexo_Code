/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.soap;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.transaction.TransactionService;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

/**
 * This is a response-fault interceptor, however, depending on the direction of the webservice, must be connected as
 * Out or In interceptor in the appropriate stream
 * Created by bvn on 6/24/16.
 */
public class EndPointAccessFaultInterceptor extends AbstractEndPointInterceptor {

    public EndPointAccessFaultInterceptor(EndPointConfiguration endPointConfiguration, TransactionService transactionService) {
        super(endPointConfiguration, endPointConfiguration.isInbound() ? Phase.RECEIVE : Phase.PRE_STREAM, transactionService);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        Fault fault = (Fault) message.getContent(Exception.class);
        Throwable stackTrace = fault.getCause();
        logInTransaction("Request failed", new Exception(stackTrace));
    }

}
