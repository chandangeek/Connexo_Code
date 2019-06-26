/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.energyict.mdc.sap.soap.webservices.impl.MeterReadingDocumentBulkResult;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MeterReadingDocumentERPResultBulkCreateRequestEOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MeterReadingDocumentERPResultBulkCreateRequestEOutService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.util.HashMap;
import java.util.Map;

@Component(name = "com.energyict.mdc.sap.meterreadingdocumentbulkresult.outbound.provider",
        service = {MeterReadingDocumentBulkResult.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + MeterReadingDocumentBulkResult.SAP_METER_READING_DOCUMENT_BULK_RESULT})
public class MeterReadingDocumentBulkResultCreateRequestProvider extends AbstractOutboundEndPointProvider<MeterReadingDocumentERPResultBulkCreateRequestEOut> implements MeterReadingDocumentBulkResult, OutboundSoapEndPointProvider, ApplicationSpecific {

    private final Map<String, MeterReadingDocumentERPResultBulkCreateRequestEOut> ports = new HashMap<>();

    public MeterReadingDocumentBulkResultCreateRequestProvider() {
        // for OSGI purposes
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addBulkResultsPort(MeterReadingDocumentERPResultBulkCreateRequestEOut port,
                                   Map<String, Object> properties) {
        super.doAddEndpoint(port, properties);
    }

    public void removeBulkResultsPort(MeterReadingDocumentERPResultBulkCreateRequestEOut port) {
        super.doRemoveEndpoint(port);
    }

    @Override
    public Service get() {
        return new MeterReadingDocumentERPResultBulkCreateRequestEOutService(
                getService().getClassLoader().getResource(RESOURCE), new QName(NAMESPACE_URI, LOCAL_PART)
        );
    }

    @Override
    public Class getService() {
        return MeterReadingDocumentERPResultBulkCreateRequestEOut.class;
    }

    @Override
    protected String getName() {
        return MeterReadingDocumentBulkResult.SAP_METER_READING_DOCUMENT_BULK_RESULT;
    }

    @Override
    public void call(MeterReadingDocumentCreateResultMessage resultMessage) {
        using("meterReadingDocumentERPResultBulkCreateRequestEOut")
                .send(resultMessage.getBulkResultMessage());
    }

    @Override
    public String getApplication(){
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}