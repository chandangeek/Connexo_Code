/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.SmartMeterMeterReadingDocumentERPBulkCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.SmrtMtrMtrRdngDocERPBulkCrteReqMsg;

import javax.inject.Inject;
import java.util.Optional;

public class MeterReadingDocumentCreateBulkEndpoint implements SmartMeterMeterReadingDocumentERPBulkCreateRequestCIn {

    private final ServiceCallCommands serviceCallCommands;

    @Inject
    MeterReadingDocumentCreateBulkEndpoint(ServiceCallCommands serviceCallCommands) {
        this.serviceCallCommands = serviceCallCommands;
    }

    @Override
    public void smartMeterMeterReadingDocumentERPBulkCreateRequestCIn(SmrtMtrMtrRdngDocERPBulkCrteReqMsg request) {
        Optional.ofNullable(request).ifPresent(requestMessage ->
                serviceCallCommands.createServiceCallAndTransition(MeterReadingDocumentCreateRequestMessage.builder()
                        .from(requestMessage)
                        .build()));
    }
}