/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.MeterReadingDocumentERPResultBulkCreateConfirmationCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.MtrRdngDocERPRsltBulkCrteConfMsg;

import javax.inject.Inject;
import java.util.Optional;

public class MeterReadingDocumentResultBulkCreateConfirmationEndpoint implements MeterReadingDocumentERPResultBulkCreateConfirmationCIn {

    private final ServiceCallCommands serviceCallCommands;

    @Inject
    MeterReadingDocumentResultBulkCreateConfirmationEndpoint(ServiceCallCommands serviceCallCommands) {
        this.serviceCallCommands = serviceCallCommands;
    }

    @Override
    public void meterReadingDocumentERPResultBulkCreateConfirmationCIn(MtrRdngDocERPRsltBulkCrteConfMsg request) {
        Optional.ofNullable(request)
                .ifPresent(requestMessage ->
                        serviceCallCommands
                                .updateServiceCallTransition(MeterReadingDocumentResultCreateConfirmationRequestMessage
                                        .builder()
                                        .from(requestMessage)
                                        .build()));
    }
}