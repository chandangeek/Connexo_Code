/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallCommands;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmartMeterMeterReadingDocumentERPCreateRequestCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmrtMtrMtrRdngDocERPCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmrtMtrMtrRdngDocERPCrteReqMtrRdngDoc;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmrtMtrMtrRdngDocERPCrteReqUtilsDvce;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmrtMtrMtrRdngDocERPCrteReqUtilsMsmtTsk;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.UtilitiesMeasurementTaskID;
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
                getTaskId(requestMessage.getMeterReadingDocument())
                        .ifPresent(value -> values.put(SapAttributeNames.SAP_UTILITIES_MEASUREMENT_TASK_ID.getAttributeName(), value));
                getDeviceId(requestMessage.getMeterReadingDocument())
                        .ifPresent(value -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), value));
                saveRelatedAttributes(values);

                serviceCallCommands.createServiceCallAndTransition(MeterReadingDocumentCreateRequestMessage.builder()
                        .from(requestMessage)
                        .build());
            });

            return null;
        });
    }

    private static Optional<String> getTaskId(SmrtMtrMtrRdngDocERPCrteReqMtrRdngDoc doc) {
        return Optional.ofNullable(doc)
                .map(SmrtMtrMtrRdngDocERPCrteReqMtrRdngDoc::getUtiltiesMeasurementTask)
                .map(SmrtMtrMtrRdngDocERPCrteReqUtilsMsmtTsk::getUtilitiesMeasurementTaskID)
                .map(UtilitiesMeasurementTaskID::getValue);
    }

    private static Optional<String> getDeviceId(SmrtMtrMtrRdngDocERPCrteReqMtrRdngDoc doc) {
        return Optional.ofNullable(doc)
                .map(SmrtMtrMtrRdngDocERPCrteReqMtrRdngDoc::getUtiltiesMeasurementTask)
                .map(SmrtMtrMtrRdngDocERPCrteReqUtilsMsmtTsk::getUtiltiesDevice)
                .map(SmrtMtrMtrRdngDocERPCrteReqUtilsDvce::getUtilitiesDeviceID)
                .map(UtilitiesDeviceID::getValue);
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}