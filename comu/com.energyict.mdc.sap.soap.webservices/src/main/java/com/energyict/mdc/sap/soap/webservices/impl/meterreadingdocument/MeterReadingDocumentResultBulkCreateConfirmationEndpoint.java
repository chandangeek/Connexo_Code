/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.soap.whiteboard.cxf.WebServiceAplication;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.MeterReadingDocumentERPResultBulkCreateConfirmationEIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.MtrRdngDocERPRsltBulkCrteConfMsg;

import javax.inject.Inject;
import java.util.Optional;

public class MeterReadingDocumentResultBulkCreateConfirmationEndpoint implements MeterReadingDocumentERPResultBulkCreateConfirmationEIn , WebServiceAplication {

    private final ServiceCallCommands serviceCallCommands;

    @Inject
    MeterReadingDocumentResultBulkCreateConfirmationEndpoint(ServiceCallCommands serviceCallCommands) {
        this.serviceCallCommands = serviceCallCommands;
    }

    @Override
    public void meterReadingDocumentERPResultBulkCreateConfirmationEIn(MtrRdngDocERPRsltBulkCrteConfMsg request) {
        Optional.ofNullable(request)
                .ifPresent(requestMessage ->
                        serviceCallCommands
                                .updateServiceCallTransition(MeterReadingDocumentResultCreateConfirmationRequestMessage
                                        .builder()
                                        .from(requestMessage)
                                        .build()));
    }

    @Override
    public String getApplication(){
        return WebServiceAplication.WebServiceApplicationName.MULTISENSE.getName();
    }
}