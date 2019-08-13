/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.energyict.mdc.sap.soap.webservices.impl.MeterReadingDocumentBulkRequestConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.SmartMeterMeterReadingDocumentERPBulkCreateConfirmationEOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.SmartMeterMeterReadingDocumentERPBulkCreateConfirmationEOutService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.util.Map;

@Component(name = "com.energyict.mdc.sap.meterreadingdocumentbulkrequest.outbound.provider",
        service = {MeterReadingDocumentBulkRequestConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + MeterReadingDocumentBulkRequestConfirmation.SAP_METER_READING_DOCUMENT_BULK_REQUEST_CONFIRMATION})
public class MeterReadingDocumentBulkRequestConfirmationProvider extends AbstractOutboundEndPointProvider<SmartMeterMeterReadingDocumentERPBulkCreateConfirmationEOut> implements MeterReadingDocumentBulkRequestConfirmation, OutboundSoapEndPointProvider, ApplicationSpecific {

    public MeterReadingDocumentBulkRequestConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addConfirmationPort(SmartMeterMeterReadingDocumentERPBulkCreateConfirmationEOut port,
                                    Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeConfirmationPort(SmartMeterMeterReadingDocumentERPBulkCreateConfirmationEOut port) {
        super.doRemoveEndpoint(port);
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        // No action, just for binding WebServiceActivator
    }

    @Override
    public Service get() {
        return new SmartMeterMeterReadingDocumentERPBulkCreateConfirmationEOutService(
                getService().getClassLoader().getResource(RESOURCE), new QName(NAMESPACE_URI, LOCAL_PART)
        );
    }

    @Override
    public Class getService() {
        return SmartMeterMeterReadingDocumentERPBulkCreateConfirmationEOut.class;
    }

    @Override
    protected String getName() {
        return MeterReadingDocumentBulkRequestConfirmation.SAP_METER_READING_DOCUMENT_BULK_REQUEST_CONFIRMATION;
    }

    @Override
    public void call(MeterReadingDocumentRequestConfirmationMessage confirmationMessage) {
        using("smartMeterMeterReadingDocumentERPBulkCreateConfirmationEOut")
                .send(confirmationMessage.getBulkConfirmationMessage());
    }

    @Override
    public String getApplication(){
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

}