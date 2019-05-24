/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceAplication;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.MeterReadingDocumentResult;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MeterReadingDocumentERPResultCreateRequestEOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MeterReadingDocumentERPResultCreateRequestEOutService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component(name = "com.energyict.mdc.sap.meterreadingdocumentresult.outbound.provider",
        service = {MeterReadingDocumentResult.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + MeterReadingDocumentResult.SAP_METER_READING_DOCUMENT_RESULT})
public class MeterReadingDocumentResultCreateRequestProvider implements MeterReadingDocumentResult, OutboundSoapEndPointProvider, WebServiceAplication {

    private final Map<String, MeterReadingDocumentERPResultCreateRequestEOut> ports = new HashMap<>();

    public MeterReadingDocumentResultCreateRequestProvider() {
        // for OSGI purposes
    }

    private volatile Thesaurus thesaurus;

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addResultPort(MeterReadingDocumentERPResultCreateRequestEOut port,
                              Map<String, Object> properties) {
        Optional.ofNullable(properties)
                .map(property -> property.get(WebServiceActivator.URL_PROPERTY))
                .map(String.class::cast)
                .ifPresent(url -> ports.put(url, port));
    }

    public void removeResultPort(MeterReadingDocumentERPResultCreateRequestEOut port) {
        ports.values().removeIf(entryPort -> port == entryPort);
    }

    @Reference
    public void setThesaurus(WebServiceActivator webServiceActivator) {
        this.thesaurus = webServiceActivator.getThesaurus();
    }

    @Override
    public Service get() {
        return new MeterReadingDocumentERPResultCreateRequestEOutService(
                getService().getClassLoader().getResource(RESOURCE), new QName(NAMESPACE_URI, LOCAL_PART)
        );
    }

    @Override
    public Class getService() {
        return MeterReadingDocumentERPResultCreateRequestEOut.class;
    }

    @Override
    public void call(MeterReadingDocumentCreateResultMessage resultMessage) {
        Optional.ofNullable(ports.get(resultMessage.getUrl()))
                .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.NO_WEB_SERVICE_ENDPOINTS))
                .meterReadingDocumentERPResultCreateRequestEOut(resultMessage.getResultMessage());
    }

    @Override
    public String getApplication(){
        return WebServiceAplication.WebServiceApplicationName.MULTISENSE.getName();
    }
}