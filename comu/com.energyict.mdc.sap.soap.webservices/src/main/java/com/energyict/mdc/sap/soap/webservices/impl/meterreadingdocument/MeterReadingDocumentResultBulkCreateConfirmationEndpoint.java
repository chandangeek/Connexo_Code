/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.MeterReadingDocumentERPResultBulkCreateConfirmationCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.MeterReadingDocumentID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.MtrRdngDocERPRsltBulkCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.MtrRdngDocERPRsltCrteConfMtrRdngDoc;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.MtrRdngDocERPRsltCrteConfUtilsDvce;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.MtrRdngDocERPRsltCrteConfUtilsMsmtTsk;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreateconfirmation.UtilitiesDeviceID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.util.Optional;

public class MeterReadingDocumentResultBulkCreateConfirmationEndpoint extends AbstractInboundEndPoint implements MeterReadingDocumentERPResultBulkCreateConfirmationCIn, ApplicationSpecific {

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
                        requestMessage.getMeterReadingDocumentERPResultCreateConfirmationMessage().forEach(msg -> {
                            getDeviceId(msg.getMeterReadingDocument())
                                    .ifPresent(value -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), value));
                            getMeterReadingDocumentId(msg.getMeterReadingDocument())
                                    .ifPresent(value -> values.put(SapAttributeNames.SAP_METER_READING_DOCUMENT_ID.getAttributeName(), value));
                        });
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

    private static Optional<String> getMeterReadingDocumentId(MtrRdngDocERPRsltCrteConfMtrRdngDoc doc) {
        return Optional.ofNullable(doc)
                .map(MtrRdngDocERPRsltCrteConfMtrRdngDoc::getID)
                .map(MeterReadingDocumentID::getValue);
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}