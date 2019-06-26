/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.energyict.mdc.sap.soap.webservices.impl.MeterReadingDocumentRequestConfirmation;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.SmartMeterMeterReadingDocumentERPCreateConfirmationEOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreateconfirmation.SmartMeterMeterReadingDocumentERPCreateConfirmationEOutService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.util.HashMap;
import java.util.Map;

@Component(name = "com.energyict.mdc.sap.meterreadingdocumentrequest.outbound.provider",
        service = {MeterReadingDocumentRequestConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + MeterReadingDocumentRequestConfirmation.SAP_METER_READING_DOCUMENT_REQUEST_CONFIRMATION})
public class MeterReadingDocumentCreateConfirmationProvider extends AbstractOutboundEndPointProvider<SmartMeterMeterReadingDocumentERPCreateConfirmationEOut> implements MeterReadingDocumentRequestConfirmation, OutboundSoapEndPointProvider, ApplicationSpecific {

    private final Map<String, SmartMeterMeterReadingDocumentERPCreateConfirmationEOut> ports = new HashMap<>();

    public MeterReadingDocumentCreateConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addRequestConfirmationPort(SmartMeterMeterReadingDocumentERPCreateConfirmationEOut port,
                                           Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeRequestConfirmationPort(SmartMeterMeterReadingDocumentERPCreateConfirmationEOut port) {
        super.doRemoveEndpoint(port);
    }

    @Override
    public Service get() {
        return new SmartMeterMeterReadingDocumentERPCreateConfirmationEOutService(
                getService().getClassLoader().getResource(RESOURCE), new QName(NAMESPACE_URI, LOCAL_PART)
        );
    }

    @Override
    public Class getService() {
        return SmartMeterMeterReadingDocumentERPCreateConfirmationEOut.class;
    }

    @Override
    protected String getName() {
        return MeterReadingDocumentRequestConfirmation.SAP_METER_READING_DOCUMENT_REQUEST_CONFIRMATION;
    }

    @Override
    public void call(MeterReadingDocumentRequestConfirmationMessage confirmationMessage) {
        using("smartMeterMeterReadingDocumentERPCreateConfirmationEOut")
                .send(confirmationMessage.getConfirmationMessage());
    }

    @Override
    public String getApplication(){
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}