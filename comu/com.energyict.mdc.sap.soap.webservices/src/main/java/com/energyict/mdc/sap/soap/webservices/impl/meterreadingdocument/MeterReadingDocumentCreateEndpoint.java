/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceRequestAttributesNames;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmartMeterMeterReadingDocumentERPCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmrtMtrMtrRdngDocERPCrteReqMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.util.Optional;

public class MeterReadingDocumentCreateEndpoint extends AbstractInboundEndPoint implements SmartMeterMeterReadingDocumentERPCreateRequestCIn, ApplicationSpecific {

    private final ServiceCallCommands serviceCallCommands;

    @Inject
    MeterReadingDocumentCreateEndpoint(ServiceCallCommands serviceCallCommands) {
        this.serviceCallCommands = serviceCallCommands;
    }

    @Override
    public void smartMeterMeterReadingDocumentERPCreateRequestCIn(SmrtMtrMtrRdngDocERPCrteReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request).ifPresent(requestMessage -> {

                SetMultimap<String, String> values = HashMultimap.create();

                values.put(WebServiceRequestAttributesNames.SAP_UTILITIES_MEASUREMENT_TASK_ID.getAttributeName(),
                        request.getMeterReadingDocument().getUtiltiesMeasurementTask().getUtilitiesMeasurementTaskID().getValue());
                values.put(WebServiceRequestAttributesNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(),
                        request.getMeterReadingDocument().getUtiltiesMeasurementTask().getUtiltiesDevice().getUtilitiesDeviceID().getValue());

                createRelatedObjects(values);
                serviceCallCommands.createServiceCallAndTransition(MeterReadingDocumentCreateRequestMessage.builder()
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