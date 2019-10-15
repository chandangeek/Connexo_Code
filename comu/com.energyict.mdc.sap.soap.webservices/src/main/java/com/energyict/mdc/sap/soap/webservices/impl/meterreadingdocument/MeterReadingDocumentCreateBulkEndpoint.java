/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.SmartMeterMeterReadingDocumentERPBulkCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.SmrtMtrMtrRdngDocERPBulkCrteReqMsg;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.util.Optional;

public class MeterReadingDocumentCreateBulkEndpoint extends AbstractInboundEndPoint implements SmartMeterMeterReadingDocumentERPBulkCreateRequestCIn, ApplicationSpecific {

    private final ServiceCallCommands serviceCallCommands;

    @Inject
    MeterReadingDocumentCreateBulkEndpoint(ServiceCallCommands serviceCallCommands) {
        this.serviceCallCommands = serviceCallCommands;
    }

    @Override
    public void smartMeterMeterReadingDocumentERPBulkCreateRequestCIn(SmrtMtrMtrRdngDocERPBulkCrteReqMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request).ifPresent(requestMessage -> {

                SetMultimap<String, String> values = HashMultimap.create();

                requestMessage.getSmartMeterMeterReadingDocumentERPCreateRequestMessage().forEach(req->{
                    values.put(SapAttributeNames.SAP_UTILITIES_MEASUREMENT_TASK_ID.getAttributeName(),
                            req.getMeterReadingDocument().getUtiltiesMeasurementTask().getUtilitiesMeasurementTaskID().getValue());
                    values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(),
                            req.getMeterReadingDocument().getUtiltiesMeasurementTask().getUtiltiesDevice().getUtilitiesDeviceID().getValue());
                        }

                );
                saveRelatedAttributes(values);

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