/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceRequestAttributesNames;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.MeterReadingDocumentERPResultBulkCreateConfirmationCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.MtrRdngDocERPRsltBulkCrteConfMsg;


import com.google.common.collect.SetMultimap;
import com.google.common.collect.HashMultimap;

import javax.inject.Inject;
import java.util.Optional;

public class MeterReadingDocumentResultBulkCreateConfirmationEndpoint extends AbstractInboundEndPoint implements MeterReadingDocumentERPResultBulkCreateConfirmationCIn , ApplicationSpecific {

    private final ServiceCallCommands serviceCallCommands;

    @Inject
    MeterReadingDocumentResultBulkCreateConfirmationEndpoint(ServiceCallCommands serviceCallCommands) {
        this.serviceCallCommands = serviceCallCommands;
    }

    @Override
    public void meterReadingDocumentERPResultBulkCreateConfirmationCIn(MtrRdngDocERPRsltBulkCrteConfMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> {
                            SetMultimap<String, String> values = HashMultimap.create();

                            request.getMeterReadingDocumentERPResultCreateConfirmationMessage().forEach(message->{
                                Optional.ofNullable(message.getMeterReadingDocument()).ifPresent(meterDocument->{
                                    values.put(WebServiceRequestAttributesNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(),
                                            meterDocument.getUtiltiesMeasurementTask().getUtiltiesDevice().getUtilitiesDeviceID().getValue());
                                });

                            });
                            createRelatedObjects(values);

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