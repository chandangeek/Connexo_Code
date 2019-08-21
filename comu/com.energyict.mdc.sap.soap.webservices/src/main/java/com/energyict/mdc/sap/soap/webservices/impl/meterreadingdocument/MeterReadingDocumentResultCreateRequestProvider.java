/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.energyict.mdc.sap.soap.webservices.impl.MeterReadingDocumentResult;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MeterReadingDocumentERPResultCreateRequestCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MeterReadingDocumentERPResultCreateRequestCOutService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.Map;

@Component(name = "com.energyict.mdc.sap.meterreadingdocumentresult.outbound.provider",
        service = {MeterReadingDocumentResult.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + MeterReadingDocumentResult.SAP_METER_READING_DOCUMENT_RESULT})
public class MeterReadingDocumentResultCreateRequestProvider extends AbstractOutboundEndPointProvider<MeterReadingDocumentERPResultCreateRequestCOut> implements MeterReadingDocumentResult, OutboundSoapEndPointProvider, ApplicationSpecific {

    public MeterReadingDocumentResultCreateRequestProvider() {
        // for OSGI purposes
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addResultPort(MeterReadingDocumentERPResultCreateRequestCOut port,
                              Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeResultPort(MeterReadingDocumentERPResultCreateRequestCOut port) {
        super.doRemoveEndpoint(port);
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        // No action, just for binding WebServiceActivator
    }

    @Override
    public Service get() {
        return new MeterReadingDocumentERPResultCreateRequestCOutService();
    }

    @Override
    public Class getService() {
        return MeterReadingDocumentERPResultCreateRequestCOut.class;
    }

    @Override
    protected String getName() {
        return MeterReadingDocumentResult.SAP_METER_READING_DOCUMENT_RESULT;
    }

    @Override
    public void call(MeterReadingDocumentCreateResultMessage resultMessage) {
        using("meterReadingDocumentERPResultCreateRequestCOut")
                .send(resultMessage.getResultMessage());
    }

    @Override
    public String getApplication(){
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}