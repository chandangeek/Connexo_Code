/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.MeterReadingDocumentBulkResult;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MeterReadingDocumentERPResultBulkCreateRequestCOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MeterReadingDocumentERPResultBulkCreateRequestCOutService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component(name = "com.energyict.mdc.sap.meterreadingdocumentbulkresult.outbound.provider",
        service = {MeterReadingDocumentBulkResult.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + MeterReadingDocumentBulkResult.SAP_METER_READING_DOCUMENT_BULK_RESULT})
public class MeterReadingDocumentBulkResultCreateRequestProvider implements MeterReadingDocumentBulkResult, OutboundSoapEndPointProvider {

    private final Map<String, MeterReadingDocumentERPResultBulkCreateRequestCOut> ports = new HashMap<>();

    private volatile Thesaurus thesaurus;

    public MeterReadingDocumentBulkResultCreateRequestProvider() {
        // for OSGI purposes
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addBulkResultsPort(MeterReadingDocumentERPResultBulkCreateRequestCOut port,
                                   Map<String, Object> properties) {
        Optional.ofNullable(properties)
                .map(property -> property.get(WebServiceActivator.URL_PROPERTY))
                .map(String.class::cast)
                .ifPresent(url -> ports.put(url, port));
    }

    public void removeBulkResultsPort(MeterReadingDocumentERPResultBulkCreateRequestCOut port) {
        ports.values().removeIf(entryPort -> port == entryPort);
    }

    @Reference
    public void setThesaurus(WebServiceActivator webServiceActivator) {
        this.thesaurus = webServiceActivator.getThesaurus();
    }

    @Override
    public Service get() {
        return new MeterReadingDocumentERPResultBulkCreateRequestCOutService();
    }

    @Override
    public Class getService() {
        return MeterReadingDocumentERPResultBulkCreateRequestCOut.class;
    }

    @Override
    public void call(MeterReadingDocumentCreateResultMessage resultMessage) {
        if (ports.isEmpty()) {
            throw new SAPWebServiceException(thesaurus, MessageSeeds.NO_WEB_SERVICE_ENDPOINTS);
        }
        ports.values().stream().findFirst().get().meterReadingDocumentERPResultBulkCreateRequestCOut(resultMessage.getBulkResultMessage());
    }
}