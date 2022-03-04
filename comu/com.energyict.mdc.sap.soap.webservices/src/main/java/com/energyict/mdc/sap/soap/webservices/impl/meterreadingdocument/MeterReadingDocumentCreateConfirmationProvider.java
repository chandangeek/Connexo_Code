/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.MeterReadingDocumentRequestConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.MeterReadingDocumentID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.SmartMeterMeterReadingDocumentERPCreateConfirmationCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.SmartMeterMeterReadingDocumentERPCreateConfirmationCOutService;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.SmrtMtrMtrRdngDocERPCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.SmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.Map;
import java.util.Optional;

@Component(name = "com.energyict.mdc.sap.meterreadingdocumentrequest.outbound.provider",
        service = {MeterReadingDocumentRequestConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + MeterReadingDocumentRequestConfirmation.SAP_METER_READING_DOCUMENT_REQUEST_CONFIRMATION})
public class MeterReadingDocumentCreateConfirmationProvider extends AbstractOutboundEndPointProvider<SmartMeterMeterReadingDocumentERPCreateConfirmationCOut> implements MeterReadingDocumentRequestConfirmation, OutboundSoapEndPointProvider, ApplicationSpecific {

    public MeterReadingDocumentCreateConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addRequestConfirmationPort(SmartMeterMeterReadingDocumentERPCreateConfirmationCOut port,
                                           Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeRequestConfirmationPort(SmartMeterMeterReadingDocumentERPCreateConfirmationCOut port) {
        super.doRemoveEndpoint(port);
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        // No action, just for binding WebServiceActivator
    }

    @Reference
    public void setWebServicesService(WebServicesService webServicesService){
        // No action, just for binding WebServicesService
    }

    @Override
    public Service get() {
        return new SmartMeterMeterReadingDocumentERPCreateConfirmationCOutService();
    }

    @Override
    public Class getService() {
        return SmartMeterMeterReadingDocumentERPCreateConfirmationCOut.class;
    }

    @Override
    protected String getName() {
        return MeterReadingDocumentRequestConfirmation.SAP_METER_READING_DOCUMENT_REQUEST_CONFIRMATION;
    }

    @Override
    public void call(MeterReadingDocumentRequestConfirmationMessage confirmationMessage) {
        SetMultimap<String, String> values = HashMultimap.create();
        SmrtMtrMtrRdngDocERPCrteConfMsg message = confirmationMessage.getConfirmationMessage()
                .orElseThrow(() -> new IllegalStateException("Unable to get confirmation message"));

        getMeterReadingDocumentId(message).ifPresent(value -> values.put(SapAttributeNames.SAP_METER_READING_DOCUMENT_ID.getAttributeName(), value));

        using("smartMeterMeterReadingDocumentERPCreateConfirmationCOut")
                .withRelatedAttributes(values)
                .send(message);
    }

    private static Optional<String> getMeterReadingDocumentId(SmrtMtrMtrRdngDocERPCrteConfMsg msg) {
        return Optional.ofNullable(msg)
                .map(SmrtMtrMtrRdngDocERPCrteConfMsg::getMeterReadingDocument)
                .map(SmrtMtrMtrRdngDocERPCrteConfMtrRdngDoc::getID)
                .map(MeterReadingDocumentID::getValue);
    }

    @Override
    public String getApplication(){
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}