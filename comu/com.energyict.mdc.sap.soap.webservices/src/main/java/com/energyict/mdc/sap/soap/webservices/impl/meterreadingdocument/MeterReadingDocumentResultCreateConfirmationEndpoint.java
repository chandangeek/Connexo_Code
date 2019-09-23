/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceRequestAttributesNames;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreateconfirmation.MeterReadingDocumentERPResultCreateConfirmationCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreateconfirmation.MtrRdngDocERPRsltCrteConfMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.util.Optional;

public class MeterReadingDocumentResultCreateConfirmationEndpoint extends AbstractInboundEndPoint implements MeterReadingDocumentERPResultCreateConfirmationCIn, ApplicationSpecific {

    private final ServiceCallCommands serviceCallCommands;

    @Inject
    MeterReadingDocumentResultCreateConfirmationEndpoint(ServiceCallCommands serviceCallCommands) {
        this.serviceCallCommands = serviceCallCommands;
    }

    @Override
    public void meterReadingDocumentERPResultCreateConfirmationCIn(MtrRdngDocERPRsltCrteConfMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> {
                        createRelatedObject(WebServiceRequestAttributesNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(),
                                request.getMeterReadingDocument().getUtiltiesMeasurementTask().getUtiltiesDevice().getUtilitiesDeviceID().getValue());

                        serviceCallCommands
                                .updateServiceCallTransition(MeterReadingDocumentResultCreateConfirmationRequestMessage
                                        .builder()
                                        .from(requestMessage)
                                        .build());
                    });

            return null;
        });
    }

    @Override
    public String getApplication(){
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}