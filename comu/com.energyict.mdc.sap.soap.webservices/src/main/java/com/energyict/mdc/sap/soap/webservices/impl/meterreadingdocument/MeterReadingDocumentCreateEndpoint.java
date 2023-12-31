/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmartMeterMeterReadingDocumentERPCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmrtMtrMtrRdngDocERPCrteReqMsg;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.util.Optional;

public class MeterReadingDocumentCreateEndpoint extends AbstractInboundEndPoint implements SmartMeterMeterReadingDocumentERPCreateRequestCIn, ApplicationSpecific {

    private final ServiceCallCommands serviceCallCommands;
    private final WebServiceActivator webServiceActivator;
    private final Thesaurus thesaurus;

    @Inject
    MeterReadingDocumentCreateEndpoint(ServiceCallCommands serviceCallCommands,
                                       WebServiceActivator webServiceActivator, Thesaurus thesaurus) {
        this.serviceCallCommands = serviceCallCommands;
        this.webServiceActivator = webServiceActivator;
        this.thesaurus = thesaurus;
    }

    @Override
    public void smartMeterMeterReadingDocumentERPCreateRequestCIn(SmrtMtrMtrRdngDocERPCrteReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request).ifPresent(requestMessage -> {
                MeterReadingDocumentCreateRequestMessage message = MeterReadingDocumentCreateRequestMessage.builder(thesaurus)
                        .from(requestMessage)
                        .build();
                SetMultimap<String, String> values = HashMultimap.create();
                message.getMeterReadingDocumentCreateMessages().forEach(msg -> {
                    Optional.ofNullable(msg.getLrn())
                            .ifPresent(value -> values.put(SapAttributeNames.SAP_UTILITIES_MEASUREMENT_TASK_ID.getAttributeName(), value));
                    Optional.ofNullable(msg.getDeviceId())
                            .ifPresent(value -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), value));
                    Optional.ofNullable(msg.getId())
                            .ifPresent(value -> values.put(SapAttributeNames.SAP_METER_READING_DOCUMENT_ID.getAttributeName(), value));
                });
                saveRelatedAttributes(values);
                serviceCallCommands.createServiceCallAndTransition(message);
            });

            return null;
        });
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}