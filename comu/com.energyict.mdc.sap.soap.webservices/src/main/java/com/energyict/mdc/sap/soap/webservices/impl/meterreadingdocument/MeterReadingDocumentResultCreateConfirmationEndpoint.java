/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreateconfirmation.MeterReadingDocumentERPResultCreateConfirmationCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreateconfirmation.MtrRdngDocERPRsltCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreateconfirmation.MtrRdngDocERPRsltCrteConfMtrRdngDoc;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreateconfirmation.MtrRdngDocERPRsltCrteConfUtilsDvce;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreateconfirmation.MtrRdngDocERPRsltCrteConfUtilsMsmtTsk;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreateconfirmation.UtilitiesDeviceID;
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
                        SetMultimap<String, String> values = HashMultimap.create();
                        getDeviceId(requestMessage.getMeterReadingDocument())
                                .ifPresent(value -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), value));
                        saveRelatedAttributes(values);
                        serviceCallCommands
                                .updateServiceCallTransition(MeterReadingDocumentResultCreateConfirmationRequestMessage
                                        .builder()
                                        .from(requestMessage)
                                        .build());
                    });

            return null;
        });
    }

    private static Optional<String> getDeviceId(MtrRdngDocERPRsltCrteConfMtrRdngDoc doc) {
        return Optional.ofNullable(doc)
                .map(MtrRdngDocERPRsltCrteConfMtrRdngDoc::getUtiltiesMeasurementTask)
                .map(MtrRdngDocERPRsltCrteConfUtilsMsmtTsk::getUtiltiesDevice)
                .map(MtrRdngDocERPRsltCrteConfUtilsDvce::getUtilitiesDeviceID)
                .map(UtilitiesDeviceID::getValue);
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}