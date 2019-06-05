/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.soap;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionService;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

/**
 * This is an interceptor, however, depending on the direction of the webservice, must be connected as Out or In interceptor in the appropriate stream
 * Created by bvn on 6/24/16.
 */
public class EndPointAccessRequestInterceptor extends AbstractEndPointInterceptor {
    private final TransactionService transactionService;
    private final WebServicesService webServicesService;

    public EndPointAccessRequestInterceptor(EndPointConfiguration endPointConfiguration,
                                            TransactionService transactionService,
                                            WebServicesService webServicesService) {
        super(endPointConfiguration, endPointConfiguration.isInbound() ? Phase.RECEIVE : Phase.PRE_STREAM);
        this.transactionService = transactionService;
        this.webServicesService = webServicesService;
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        if (message != null) {
            if (isForInboundService()) {
                webServicesService.startOccurrence(endPointConfiguration,
                        MessageUtils.getOperationName(message),
                        getApplicationName(endPointConfiguration),
                        MessageUtils.getIncomingPayload(message));
            } else {
                MessageUtils.executeOnOutgoingPayloadAvailable(message, payload -> {
                    WebServiceCallOccurrence occurrence = webServicesService.getOccurrence();
                    occurrence.setPayload(payload);
                    transactionService.runInIndependentTransaction(occurrence::save);
                });
            }
        }
    }

    private String getApplicationName(EndPointConfiguration endPointConfiguration) {
        return webServicesService.getWebService(endPointConfiguration.getWebServiceName())
                .map(WebService::getApplicationName)
                .orElse(null);
    }
}
