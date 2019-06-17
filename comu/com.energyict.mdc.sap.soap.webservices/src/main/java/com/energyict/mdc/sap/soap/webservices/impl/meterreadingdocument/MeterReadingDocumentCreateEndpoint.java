/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmartMeterMeterReadingDocumentERPCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmrtMtrMtrRdngDocERPCrteReqMsg;

import javax.inject.Inject;
import java.util.Optional;

public class MeterReadingDocumentCreateEndpoint implements SmartMeterMeterReadingDocumentERPCreateRequestCIn {

    private final ServiceCallCommands serviceCallCommands;

    @Inject
    MeterReadingDocumentCreateEndpoint(ServiceCallCommands serviceCallCommands) {
        this.serviceCallCommands = serviceCallCommands;
    }

    @Override
    public void smartMeterMeterReadingDocumentERPCreateRequestCIn(SmrtMtrMtrRdngDocERPCrteReqMsg request) {
        Optional.ofNullable(request).ifPresent(requestMessage ->
                serviceCallCommands.createServiceCallAndTransition(MeterReadingDocumentCreateRequestMessage.builder()
                        .from(requestMessage)
                        .build()));
    }
}