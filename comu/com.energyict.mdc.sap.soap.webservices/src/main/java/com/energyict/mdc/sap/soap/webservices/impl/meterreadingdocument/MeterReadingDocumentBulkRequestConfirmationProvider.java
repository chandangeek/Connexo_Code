/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.MeterReadingDocumentBulkRequestConfirmation;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.SmartMeterMeterReadingDocumentERPBulkCreateConfirmationEOut;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreateconfirmation.SmartMeterMeterReadingDocumentERPBulkCreateConfirmationEOutService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component(name = "com.energyict.mdc.sap.meterreadingdocumentbulkrequest.outbound.provider",
        service = {MeterReadingDocumentBulkRequestConfirmation.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + MeterReadingDocumentBulkRequestConfirmation.SAP_METER_READING_DOCUMENT_BULK_REQUEST_CONFIRMATION})
public class MeterReadingDocumentBulkRequestConfirmationProvider implements MeterReadingDocumentBulkRequestConfirmation, OutboundSoapEndPointProvider {

    private final Map<String, SmartMeterMeterReadingDocumentERPBulkCreateConfirmationEOut> ports = new HashMap<>();

    private volatile Thesaurus thesaurus;

    public MeterReadingDocumentBulkRequestConfirmationProvider() {
        // for OSGI purposes
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addConfirmationPort(SmartMeterMeterReadingDocumentERPBulkCreateConfirmationEOut port,
                                    Map<String, Object> properties) {
        Optional.ofNullable(properties)
                .map(property -> property.get(WebServiceActivator.URL_PROPERTY))
                .map(String.class::cast)
                .ifPresent(url -> ports.put(url, port));
    }

    public void removeConfirmationPort(SmartMeterMeterReadingDocumentERPBulkCreateConfirmationEOut port) {
        ports.values().removeIf(entryPort -> port == entryPort);
    }

    @Reference
    public void setThesaurus(WebServiceActivator webServiceActivator) {
        this.thesaurus = webServiceActivator.getThesaurus();
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
    public void call(MeterReadingDocumentRequestConfirmationMessage confirmationMessage) {
        Optional.ofNullable(ports.get(confirmationMessage.getUrl()))
                .orElseThrow(() -> new SAPWebServiceException(thesaurus, MessageSeeds.NO_WEB_SERVICE_ENDPOINTS))
                .smartMeterMeterReadingDocumentERPBulkCreateConfirmationEOut(confirmationMessage.getBulkConfirmationMessage());
    }
}